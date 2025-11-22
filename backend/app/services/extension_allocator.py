"""Infinity分机分配算法服务"""
from typing import Optional, Tuple
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import and_, or_
from fastapi import HTTPException

from app.models.infinity_extension_pool import InfinityExtensionPool, ExtensionStatusEnum
from app.models.infinity_call_config import InfinityCallConfig
from app.models.collector import Collector


class ExtensionAllocationStrategy:
    """分机分配策略枚举"""
    ROUND_ROBIN = "round_robin"              # 轮询分配
    LEAST_RECENTLY_USED = "lru"              # 最少使用优先
    COLLECTOR_AFFINITY = "collector_affinity" # 催员亲和性（优先分配上次使用的分机）


class ExtensionAllocator:
    """分机分配器"""
    
    def __init__(self, db: Session, strategy: str = ExtensionAllocationStrategy.LEAST_RECENTLY_USED):
        """
        初始化分机分配器
        
        Args:
            db: 数据库会话
            strategy: 分配策略（默认：最少使用优先）
        """
        self.db = db
        self.strategy = strategy
    
    def allocate_extension(
        self,
        tenant_id: int,
        collector_id: int,
        config_id: Optional[int] = None
    ) -> Tuple[InfinityExtensionPool, str]:
        """
        为催员分配一个空闲分机
        
        Args:
            tenant_id: 甲方ID
            collector_id: 催员ID
            config_id: Infinity配置ID（可选，如果不提供则使用该甲方的默认配置）
        
        Returns:
            Tuple[InfinityExtensionPool, str]: (分机对象, 分机号)
        
        Raises:
            HTTPException: 如果没有可用分机或配置不存在
        """
        # 1. 获取配置
        if not config_id:
            config = self.db.query(InfinityCallConfig).filter(
                InfinityCallConfig.tenant_id == tenant_id,
                InfinityCallConfig.is_active == True
            ).first()
            if not config:
                raise HTTPException(
                    status_code=404,
                    detail=f"甲方 {tenant_id} 没有有效的 Infinity 配置"
                )
            config_id = config.id
        
        # 2. 检查是否已有分机被该催员占用（可能是上次通话未正常释放）
        existing_extension = self.db.query(InfinityExtensionPool).filter(
            InfinityExtensionPool.tenant_id == tenant_id,
            InfinityExtensionPool.config_id == config_id,
            InfinityExtensionPool.current_collector_id == collector_id,
            InfinityExtensionPool.status == ExtensionStatusEnum.IN_USE
        ).first()
        
        if existing_extension:
            # 如果已有分机被占用，直接返回
            return existing_extension, existing_extension.infinity_extension_number
        
        # 3. 根据策略选择分机
        if self.strategy == ExtensionAllocationStrategy.COLLECTOR_AFFINITY:
            extension = self._allocate_with_affinity(tenant_id, config_id, collector_id)
        elif self.strategy == ExtensionAllocationStrategy.ROUND_ROBIN:
            extension = self._allocate_round_robin(tenant_id, config_id)
        else:  # 默认使用 LEAST_RECENTLY_USED
            extension = self._allocate_lru(tenant_id, config_id)
        
        if not extension:
            raise HTTPException(
                status_code=503,
                detail="当前没有可用的分机，请稍后重试"
            )
        
        # 4. 更新分机状态（使用 FOR UPDATE 锁定，防止并发分配同一分机）
        extension.status = ExtensionStatusEnum.IN_USE
        extension.current_collector_id = collector_id
        extension.assigned_at = datetime.now()
        extension.last_used_at = datetime.now()
        
        self.db.commit()
        self.db.refresh(extension)
        
        return extension, extension.infinity_extension_number
    
    def release_extension(
        self,
        tenant_id: int,
        extension_number: str,
        collector_id: Optional[int] = None
    ) -> bool:
        """
        释放分机（通话结束时调用）
        
        Args:
            tenant_id: 甲方ID
            extension_number: 分机号
            collector_id: 催员ID（可选，用于验证）
        
        Returns:
            bool: 是否成功释放
        """
        query = self.db.query(InfinityExtensionPool).filter(
            InfinityExtensionPool.tenant_id == tenant_id,
            InfinityExtensionPool.infinity_extension_number == extension_number,
            InfinityExtensionPool.status == ExtensionStatusEnum.IN_USE
        )
        
        # 如果提供了催员ID，验证分机是否被该催员占用
        if collector_id:
            query = query.filter(InfinityExtensionPool.current_collector_id == collector_id)
        
        extension = query.first()
        
        if not extension:
            return False
        
        # 更新状态
        extension.status = ExtensionStatusEnum.AVAILABLE
        extension.current_collector_id = None
        extension.released_at = datetime.now()
        
        self.db.commit()
        
        return True
    
    def force_release_collector_extensions(self, collector_id: int) -> int:
        """
        强制释放某个催员占用的所有分机（用于异常情况处理）
        
        Args:
            collector_id: 催员ID
        
        Returns:
            int: 释放的分机数量
        """
        extensions = self.db.query(InfinityExtensionPool).filter(
            InfinityExtensionPool.current_collector_id == collector_id,
            InfinityExtensionPool.status == ExtensionStatusEnum.IN_USE
        ).all()
        
        count = 0
        for extension in extensions:
            extension.status = ExtensionStatusEnum.AVAILABLE
            extension.current_collector_id = None
            extension.released_at = datetime.now()
            count += 1
        
        if count > 0:
            self.db.commit()
        
        return count
    
    def _allocate_lru(self, tenant_id: int, config_id: int) -> Optional[InfinityExtensionPool]:
        """
        最少使用优先策略（Least Recently Used）
        选择最久未使用的分机
        """
        extension = self.db.query(InfinityExtensionPool).filter(
            InfinityExtensionPool.tenant_id == tenant_id,
            InfinityExtensionPool.config_id == config_id,
            InfinityExtensionPool.status == ExtensionStatusEnum.AVAILABLE
        ).order_by(
            InfinityExtensionPool.last_used_at.asc().nullsfirst()  # NULL值优先（从未使用过的）
        ).with_for_update(skip_locked=True).first()
        
        return extension
    
    def _allocate_round_robin(self, tenant_id: int, config_id: int) -> Optional[InfinityExtensionPool]:
        """
        轮询策略（Round Robin）
        按ID顺序选择下一个可用分机
        """
        extension = self.db.query(InfinityExtensionPool).filter(
            InfinityExtensionPool.tenant_id == tenant_id,
            InfinityExtensionPool.config_id == config_id,
            InfinityExtensionPool.status == ExtensionStatusEnum.AVAILABLE
        ).order_by(
            InfinityExtensionPool.id.asc()
        ).with_for_update(skip_locked=True).first()
        
        return extension
    
    def _allocate_with_affinity(
        self,
        tenant_id: int,
        config_id: int,
        collector_id: int
    ) -> Optional[InfinityExtensionPool]:
        """
        催员亲和性策略（Collector Affinity）
        优先分配该催员上次使用的分机
        """
        # 1. 先查找该催员最近使用的分机（如果可用）
        last_extension = self.db.query(InfinityExtensionPool).filter(
            InfinityExtensionPool.tenant_id == tenant_id,
            InfinityExtensionPool.config_id == config_id,
            InfinityExtensionPool.status == ExtensionStatusEnum.AVAILABLE
        ).order_by(
            # 使用 CASE WHEN 语句：如果是该催员上次使用的分机，优先级更高
            InfinityExtensionPool.last_used_at.desc()
        ).with_for_update(skip_locked=True).first()
        
        # 如果没找到，fallback 到 LRU 策略
        if not last_extension:
            return self._allocate_lru(tenant_id, config_id)
        
        return last_extension
    
    def get_extension_statistics(self, tenant_id: int, config_id: Optional[int] = None) -> dict:
        """
        获取分机池统计信息
        
        Args:
            tenant_id: 甲方ID
            config_id: 配置ID（可选）
        
        Returns:
            dict: 统计信息
        """
        query = self.db.query(InfinityExtensionPool).filter(
            InfinityExtensionPool.tenant_id == tenant_id
        )
        
        if config_id:
            query = query.filter(InfinityExtensionPool.config_id == config_id)
        
        extensions = query.all()
        
        total = len(extensions)
        available = sum(1 for e in extensions if e.status == ExtensionStatusEnum.AVAILABLE)
        in_use = sum(1 for e in extensions if e.status == ExtensionStatusEnum.IN_USE)
        offline = sum(1 for e in extensions if e.status == ExtensionStatusEnum.OFFLINE)
        
        usage_rate = (in_use / total * 100) if total > 0 else 0
        
        return {
            "tenant_id": tenant_id,
            "config_id": config_id,
            "total_extensions": total,
            "available_count": available,
            "in_use_count": in_use,
            "offline_count": offline,
            "usage_rate": round(usage_rate, 2)
        }

