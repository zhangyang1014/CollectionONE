"""
空闲催员监控API
"""
from fastapi import APIRouter, Depends, HTTPException, Query, BackgroundTasks
from sqlalchemy.orm import Session
from sqlalchemy import and_, or_, func, desc, asc
from typing import List, Optional, Dict
from datetime import datetime, date, timedelta, time
from decimal import Decimal
from random import randint, choice, random as rand_random

from app.core.database import get_db
from app.models.idle_monitor_config import IdleMonitorConfig
from app.models.collector_idle_record import CollectorIdleRecord, CollectorIdleStats
from app.models.collector import Collector
from app.models.collection_agency import CollectionAgency
from app.models.collection_team import CollectionTeam
from app.models.communication_record import CommunicationRecord, ChannelEnum, DirectionEnum, ContactResultEnum
from app.models.case import Case
from app.models.case_contact import CaseContact
from app.schemas.dashboard import (
    IdleMonitorConfigCreate,
    IdleMonitorConfigUpdate,
    IdleMonitorConfigResponse,
    IdleMonitorSummary,
    IdleMonitorDetailsResponse,
    IdleMonitorDetailItem,
    CollectorIdleDetailResponse,
    IdleTrendResponse,
    ConfigHistoryResponse,
    IdlePeriod,
    ManagedCases,
    ManagedAmount,
    CollectorInfo,
    IdleSummary,
    CaseSummary,
    IdleDetail,
    ActionInfo
)

router = APIRouter()


@router.get("/config", response_model=IdleMonitorConfigResponse)
async def get_idle_monitor_config(
    tenant_id: int = Query(..., description="甲方ID"),
    db: Session = Depends(get_db)
):
    """获取当前空闲监控配置"""
    config = db.query(IdleMonitorConfig).filter(
        and_(
            IdleMonitorConfig.tenant_id == tenant_id,
            IdleMonitorConfig.is_active == True
        )
    ).first()
    
    if not config:
        # 如果没有配置，返回默认配置
        return {
            "id": 0,
            "tenant_id": tenant_id,
            "config_name": "默认配置",
            "work_time_slots": [
                {"start": "09:00", "end": "12:00"},
                {"start": "14:00", "end": "18:00"}
            ],
            "idle_threshold_minutes": 30,
            "monitored_actions": ["call", "whatsapp", "rcs", "sms", "email", "case_update", "login"],
            "exclude_holidays": True,
            "is_active": True,
            "created_by": None,
            "created_at": datetime.now(),
            "updated_at": datetime.now()
        }
    
    return config


@router.post("/config", response_model=IdleMonitorConfigResponse)
async def create_idle_monitor_config(
    config_data: IdleMonitorConfigCreate,
    db: Session = Depends(get_db)
):
    """创建空闲监控配置"""
    # 检查是否已存在配置
    existing_config = db.query(IdleMonitorConfig).filter(
        IdleMonitorConfig.tenant_id == config_data.tenant_id
    ).first()
    
    if existing_config:
        # 如果已存在，将旧配置设置为非活跃
        existing_config.is_active = False
        db.add(existing_config)
    
    # 创建新配置
    new_config = IdleMonitorConfig(
        tenant_id=config_data.tenant_id,
        config_name=config_data.config_name,
        work_time_slots=[slot.dict() for slot in config_data.work_time_slots],
        idle_threshold_minutes=config_data.idle_threshold_minutes,
        monitored_actions=config_data.monitored_actions,
        exclude_holidays=config_data.exclude_holidays,
        is_active=True,
        created_by=config_data.created_by
    )
    
    db.add(new_config)
    db.commit()
    db.refresh(new_config)
    
    return new_config


@router.put("/config/{config_id}", response_model=IdleMonitorConfigResponse)
async def update_idle_monitor_config(
    config_id: int,
    config_data: IdleMonitorConfigUpdate,
    db: Session = Depends(get_db)
):
    """更新空闲监控配置"""
    config = db.query(IdleMonitorConfig).filter(IdleMonitorConfig.id == config_id).first()
    
    if not config:
        raise HTTPException(status_code=404, detail="配置不存在")
    
    # 更新字段
    update_data = config_data.dict(exclude_unset=True)
    if "work_time_slots" in update_data:
        update_data["work_time_slots"] = [slot.dict() for slot in config_data.work_time_slots]
    
    for key, value in update_data.items():
        setattr(config, key, value)
    
    db.add(config)
    db.commit()
    db.refresh(config)
    
    return config


@router.get("/config/history", response_model=ConfigHistoryResponse)
async def get_config_history(
    tenant_id: int = Query(..., description="甲方ID"),
    page: int = Query(1, ge=1, description="页码"),
    page_size: int = Query(10, ge=1, le=100, description="每页数量"),
    db: Session = Depends(get_db)
):
    """获取配置历史"""
    query = db.query(IdleMonitorConfig).filter(
        IdleMonitorConfig.tenant_id == tenant_id
    ).order_by(desc(IdleMonitorConfig.created_at))
    
    total = query.count()
    items = query.offset((page - 1) * page_size).limit(page_size).all()
    
    return {
        "total": total,
        "page": page,
        "page_size": page_size,
        "items": items
    }


@router.get("/summary", response_model=IdleMonitorSummary)
async def get_idle_monitor_summary(
    tenant_id: int = Query(..., description="甲方ID"),
    agency_ids: Optional[str] = Query(None, description="机构ID列表，逗号分隔"),
    team_ids: Optional[str] = Query(None, description="小组ID列表，逗号分隔"),
    collector_ids: Optional[str] = Query(None, description="催员ID列表，逗号分隔"),
    start_date: str = Query(..., description="开始日期"),
    end_date: str = Query(..., description="结束日期"),
    db: Session = Depends(get_db)
):
    """获取空闲监控总览数据"""
    # 构建筛选条件
    filters = [CollectorIdleStats.tenant_id == tenant_id]
    
    if agency_ids:
        agency_id_list = [int(id) for id in agency_ids.split(',') if id]
        filters.append(CollectorIdleStats.agency_id.in_(agency_id_list))
    
    if team_ids:
        team_id_list = [int(id) for id in team_ids.split(',') if id]
        filters.append(CollectorIdleStats.team_id.in_(team_id_list))
    
    if collector_ids:
        collector_id_list = [int(id) for id in collector_ids.split(',') if id]
        filters.append(CollectorIdleStats.collector_id.in_(collector_id_list))
    
    filters.append(CollectorIdleStats.stat_date >= start_date)
    filters.append(CollectorIdleStats.stat_date <= end_date)
    
    # 查询统计数据
    stats = db.query(
        func.count(func.distinct(CollectorIdleStats.collector_id)).label('total_idle_collectors'),
        func.sum(CollectorIdleStats.idle_count).label('total_idle_count'),
        func.sum(CollectorIdleStats.total_idle_minutes).label('total_idle_minutes'),
        func.avg(CollectorIdleStats.avg_idle_minutes).label('avg_idle_minutes')
    ).filter(and_(*filters)).first()
    
    total_idle_collectors = stats.total_idle_collectors or 0
    total_idle_count = stats.total_idle_count or 0
    total_idle_minutes = stats.total_idle_minutes or 0
    avg_idle_minutes = float(stats.avg_idle_minutes or 0)
    
    # 计算环比数据（与上一个周期对比）
    date_diff = (datetime.strptime(end_date, '%Y-%m-%d') - datetime.strptime(start_date, '%Y-%m-%d')).days + 1
    prev_start = (datetime.strptime(start_date, '%Y-%m-%d') - timedelta(days=date_diff)).strftime('%Y-%m-%d')
    prev_end = (datetime.strptime(start_date, '%Y-%m-%d') - timedelta(days=1)).strftime('%Y-%m-%d')
    
    prev_filters = filters.copy()
    prev_filters[-2] = CollectorIdleStats.stat_date >= prev_start
    prev_filters[-1] = CollectorIdleStats.stat_date <= prev_end
    
    prev_stats = db.query(
        func.count(func.distinct(CollectorIdleStats.collector_id)).label('total_idle_collectors'),
        func.sum(CollectorIdleStats.idle_count).label('total_idle_count'),
        func.sum(CollectorIdleStats.total_idle_minutes).label('total_idle_minutes'),
        func.avg(CollectorIdleStats.avg_idle_minutes).label('avg_idle_minutes')
    ).filter(and_(*prev_filters)).first()
    
    prev_collectors = prev_stats.total_idle_collectors or 0
    prev_count = prev_stats.total_idle_count or 0
    prev_minutes = prev_stats.total_idle_minutes or 0
    prev_avg = float(prev_stats.avg_idle_minutes or 0)
    
    # 计算环比变化
    collectors_change = (total_idle_collectors - prev_collectors) / prev_collectors if prev_collectors > 0 else 0
    count_change = (total_idle_count - prev_count) / prev_count if prev_count > 0 else 0
    minutes_change = (total_idle_minutes - prev_minutes) / prev_minutes if prev_minutes > 0 else 0
    avg_change = (avg_idle_minutes - prev_avg) / prev_avg if prev_avg > 0 else 0
    
    return {
        "total_idle_collectors": total_idle_collectors,
        "total_idle_count": total_idle_count,
        "total_idle_minutes": total_idle_minutes,
        "total_idle_hours": Decimal(total_idle_minutes) / 60,
        "avg_idle_minutes": Decimal(str(avg_idle_minutes)),
        "comparison": {
            "collectors_change": Decimal(str(collectors_change)),
            "count_change": Decimal(str(count_change)),
            "minutes_change": Decimal(str(minutes_change)),
            "avg_change": Decimal(str(avg_change))
        }
    }


@router.get("/details", response_model=IdleMonitorDetailsResponse)
async def get_idle_monitor_details(
    tenant_id: int = Query(..., description="甲方ID"),
    agency_ids: Optional[str] = Query(None, description="机构ID列表"),
    team_ids: Optional[str] = Query(None, description="小组ID列表"),
    collector_ids: Optional[str] = Query(None, description="催员ID列表"),
    start_date: str = Query(..., description="开始日期"),
    end_date: str = Query(..., description="结束日期"),
    page: int = Query(1, ge=1, description="页码"),
    page_size: int = Query(20, ge=1, le=100, description="每页数量"),
    sort_by: str = Query("idle_count", description="排序字段"),
    sort_order: str = Query("desc", description="排序方向"),
    db: Session = Depends(get_db)
):
    """获取空闲催员详情列表"""
    # 构建筛选条件
    filters = [CollectorIdleStats.tenant_id == tenant_id]
    
    if agency_ids:
        agency_id_list = [int(id) for id in agency_ids.split(',') if id]
        filters.append(CollectorIdleStats.agency_id.in_(agency_id_list))
    
    if team_ids:
        team_id_list = [int(id) for id in team_ids.split(',') if id]
        filters.append(CollectorIdleStats.team_id.in_(team_id_list))
    
    if collector_ids:
        collector_id_list = [int(id) for id in collector_ids.split(',') if id]
        filters.append(CollectorIdleStats.collector_id.in_(collector_id_list))
    
    filters.append(CollectorIdleStats.stat_date >= start_date)
    filters.append(CollectorIdleStats.stat_date <= end_date)
    
    # 构建排序
    sort_column = getattr(CollectorIdleStats, sort_by, CollectorIdleStats.idle_count)
    if sort_order == "asc":
        order = asc(sort_column)
    else:
        order = desc(sort_column)
    
    # 查询数据
    query = db.query(CollectorIdleStats).filter(and_(*filters)).order_by(order)
    total = query.count()
    stats_list = query.offset((page - 1) * page_size).limit(page_size).all()
    
    # 构建响应数据
    items = []
    for stats in stats_list:
        # 获取催员信息
        collector = db.query(Collector).filter(Collector.id == stats.collector_id).first()
        agency = db.query(CollectionAgency).filter(CollectionAgency.id == stats.agency_id).first()
        team = db.query(CollectionTeam).filter(CollectionTeam.id == stats.team_id).first()
        
        # 获取空闲时段列表
        idle_records = db.query(CollectorIdleRecord).filter(
            and_(
                CollectorIdleRecord.collector_id == stats.collector_id,
                CollectorIdleRecord.idle_date == stats.stat_date
            )
        ).all()
        
        idle_periods = [
            IdlePeriod(
                start=record.idle_start_time.strftime('%H:%M:%S'),
                end=record.idle_end_time.strftime('%H:%M:%S'),
                duration=record.idle_duration_minutes
            )
            for record in idle_records
        ]
        
        items.append(IdleMonitorDetailItem(
            collector_id=stats.collector_id,
            collector_name=collector.name if collector else "未知",
            collector_code=collector.code if collector else "未知",
            agency_id=stats.agency_id,
            agency_name=agency.name if agency else "未知",
            team_id=stats.team_id,
            team_name=team.name if team else "未知",
            stat_date=stats.stat_date.strftime('%Y-%m-%d'),
            idle_count=stats.idle_count,
            total_idle_minutes=stats.total_idle_minutes,
            longest_idle_minutes=stats.longest_idle_minutes,
            avg_idle_minutes=stats.avg_idle_minutes,
            idle_rate=stats.idle_rate,
            managed_cases=ManagedCases(
                total=stats.managed_cases_total,
                collected=stats.managed_cases_collected,
                collection_rate=Decimal(stats.managed_cases_collected) / Decimal(stats.managed_cases_total) if stats.managed_cases_total > 0 else Decimal(0)
            ),
            managed_amount=ManagedAmount(
                total=stats.managed_amount_total,
                collected=stats.managed_amount_collected,
                collection_rate=stats.managed_amount_collected / stats.managed_amount_total if stats.managed_amount_total > 0 else Decimal(0)
            ),
            idle_periods=idle_periods
        ))
    
    return {
        "total": total,
        "page": page,
        "page_size": page_size,
        "items": items
    }


@router.get("/collector/{collector_id}/detail", response_model=CollectorIdleDetailResponse)
async def get_collector_idle_detail(
    collector_id: int,
    date: str = Query(..., description="日期，YYYY-MM-DD"),
    db: Session = Depends(get_db)
):
    """获取催员空闲详细信息"""
    # 获取催员信息
    collector = db.query(Collector).filter(Collector.id == collector_id).first()
    if not collector:
        raise HTTPException(status_code=404, detail="催员不存在")
    
    agency = db.query(CollectionAgency).filter(CollectionAgency.id == collector.agency_id).first()
    team = db.query(CollectionTeam).filter(CollectionTeam.id == collector.team_id).first()
    
    # 获取统计数据
    stats = db.query(CollectorIdleStats).filter(
        and_(
            CollectorIdleStats.collector_id == collector_id,
            CollectorIdleStats.stat_date == date
        )
    ).first()
    
    if not stats:
        raise HTTPException(status_code=404, detail="未找到该日期的空闲数据")
    
    # 获取空闲记录详情
    idle_records = db.query(CollectorIdleRecord).filter(
        and_(
            CollectorIdleRecord.collector_id == collector_id,
            CollectorIdleRecord.idle_date == date
        )
    ).order_by(CollectorIdleRecord.idle_start_time).all()
    
    idle_details = []
    for record in idle_records:
        before_action = None
        if record.before_action:
            before_action = ActionInfo(
                type=record.before_action.get('type', ''),
                time=record.before_action.get('time', ''),
                details=record.before_action.get('details', '')
            )
        
        after_action = None
        if record.after_action:
            after_action = ActionInfo(
                type=record.after_action.get('type', ''),
                time=record.after_action.get('time', ''),
                details=record.after_action.get('details', '')
            )
        
        idle_details.append(IdleDetail(
            start_time=record.idle_start_time.strftime('%Y-%m-%d %H:%M:%S'),
            end_time=record.idle_end_time.strftime('%Y-%m-%d %H:%M:%S'),
            duration_minutes=record.idle_duration_minutes,
            before_action=before_action,
            after_action=after_action
        ))
    
    return {
        "collector_info": CollectorInfo(
            id=collector.id,
            name=collector.name,
            code=collector.code,
            agency_name=agency.name if agency else "未知",
            team_name=team.name if team else "未知"
        ),
        "stat_date": date,
        "idle_summary": IdleSummary(
            idle_count=stats.idle_count,
            total_idle_minutes=stats.total_idle_minutes,
            avg_idle_minutes=stats.avg_idle_minutes,
            longest_idle_minutes=stats.longest_idle_minutes
        ),
        "case_summary": CaseSummary(
            total_cases=stats.managed_cases_total,
            collected_cases=stats.managed_cases_collected,
            collection_rate=Decimal(stats.managed_cases_collected) / Decimal(stats.managed_cases_total) if stats.managed_cases_total > 0 else Decimal(0),
            total_amount=stats.managed_amount_total,
            collected_amount=stats.managed_amount_collected,
            amount_collection_rate=stats.managed_amount_collected / stats.managed_amount_total if stats.managed_amount_total > 0 else Decimal(0)
        ),
        "idle_details": idle_details
    }


@router.get("/trend", response_model=IdleTrendResponse)
async def get_idle_monitor_trend(
    tenant_id: int = Query(..., description="甲方ID"),
    agency_ids: Optional[str] = Query(None, description="机构ID列表"),
    team_ids: Optional[str] = Query(None, description="小组ID列表"),
    start_date: str = Query(..., description="开始日期"),
    end_date: str = Query(..., description="结束日期"),
    metric: str = Query("collectors", description="指标类型：collectors/count/minutes/avg"),
    db: Session = Depends(get_db)
):
    """获取空闲趋势数据"""
    # 构建筛选条件
    filters = [CollectorIdleStats.tenant_id == tenant_id]
    
    if agency_ids:
        agency_id_list = [int(id) for id in agency_ids.split(',') if id]
        filters.append(CollectorIdleStats.agency_id.in_(agency_id_list))
    
    if team_ids:
        team_id_list = [int(id) for id in team_ids.split(',') if id]
        filters.append(CollectorIdleStats.team_id.in_(team_id_list))
    
    filters.append(CollectorIdleStats.stat_date >= start_date)
    filters.append(CollectorIdleStats.stat_date <= end_date)
    
    # 根据指标类型选择聚合字段
    if metric == "collectors":
        value_field = func.count(func.distinct(CollectorIdleStats.collector_id))
    elif metric == "count":
        value_field = func.sum(CollectorIdleStats.idle_count)
    elif metric == "minutes":
        value_field = func.sum(CollectorIdleStats.total_idle_minutes) / 60  # 转换为小时
    else:  # avg
        value_field = func.avg(CollectorIdleStats.avg_idle_minutes)
    
    # 按日期分组查询
    results = db.query(
        CollectorIdleStats.stat_date,
        value_field.label('value')
    ).filter(and_(*filters)).group_by(
        CollectorIdleStats.stat_date
    ).order_by(CollectorIdleStats.stat_date).all()
    
    dates = [result.stat_date.strftime('%Y-%m-%d') for result in results]
    values = [int(result.value or 0) for result in results]
    
    return {
        "metric": metric,
        "dates": dates,
        "values": values
    }


@router.get("/export")
async def export_idle_monitor_data(
    tenant_id: int = Query(..., description="甲方ID"),
    agency_ids: Optional[str] = Query(None, description="机构ID列表"),
    team_ids: Optional[str] = Query(None, description="小组ID列表"),
    collector_ids: Optional[str] = Query(None, description="催员ID列表"),
    start_date: str = Query(..., description="开始日期"),
    end_date: str = Query(..., description="结束日期"),
    db: Session = Depends(get_db)
):
    """导出空闲监控数据为Excel"""
    # TODO: 实现Excel导出功能
    # 这里需要使用openpyxl或xlsxwriter库来生成Excel文件
    raise HTTPException(status_code=501, detail="导出功能开发中")


# ============ 数据计算相关API ============

class IdleCalculator:
    """空闲数据计算器"""
    
    def __init__(self, db: Session, tenant_id: int, calc_date: date):
        self.db = db
        self.tenant_id = tenant_id
        self.calc_date = calc_date
        self.log = []  # 必须在_load_config()之前初始化
        self.config = self._load_config()
        
    def add_log(self, message: str):
        """添加日志"""
        self.log.append(f"[{datetime.now().strftime('%H:%M:%S')}] {message}")
    
    def _load_config(self) -> Dict:
        """加载空闲监控配置"""
        config = self.db.query(IdleMonitorConfig).filter(
            and_(
                IdleMonitorConfig.tenant_id == self.tenant_id,
                IdleMonitorConfig.is_active == True
            )
        ).first()
        
        if not config:
            self.add_log("⚠️  未找到配置，使用默认配置")
            return {
                'work_time_slots': [
                    {'start': '09:00', 'end': '12:00'},
                    {'start': '14:00', 'end': '18:00'}
                ],
                'idle_threshold_minutes': 30,
                'monitored_actions': ['call', 'whatsapp', 'rcs', 'sms', 'email'],
                'exclude_holidays': True,
                'config_id': 0
            }
        
        return {
            'work_time_slots': config.work_time_slots,
            'idle_threshold_minutes': config.idle_threshold_minutes,
            'monitored_actions': config.monitored_actions,
            'exclude_holidays': config.exclude_holidays,
            'config_id': config.id
        }
    
    def _get_collectors(self) -> List[Collector]:
        """获取需要计算的催员列表"""
        collectors = self.db.query(Collector).filter(
            and_(
                Collector.tenant_id == self.tenant_id,
                Collector.is_active == True
            )
        ).all()
        
        self.add_log(f"📊 找到 {len(collectors)} 个活跃催员")
        return collectors
    
    def _get_collector_actions(self, collector_id: int) -> List[Dict]:
        """获取催员的所有行为记录"""
        actions = []
        
        communications = self.db.query(CommunicationRecord).filter(
            and_(
                CommunicationRecord.collector_id == collector_id,
                func.date(CommunicationRecord.contacted_at) == self.calc_date
            )
        ).order_by(CommunicationRecord.contacted_at).all()
        
        for comm in communications:
            action_type = self._map_channel_to_action(comm.channel.value)
            if action_type in self.config['monitored_actions']:
                actions.append({
                    'type': action_type,
                    'timestamp': comm.contacted_at,
                    'details': f"{action_type} - {comm.channel.value}"
                })
        
        return sorted(actions, key=lambda x: x['timestamp'])
    
    def _map_channel_to_action(self, channel: str) -> str:
        """将通信渠道映射到行为类型"""
        mapping = {
            'phone': 'call',
            'whatsapp': 'whatsapp',
            'sms': 'sms',
            'rcs': 'rcs',
            'email': 'email'
        }
        return mapping.get(channel, channel)
    
    def _is_work_time(self, dt: datetime) -> bool:
        """判断时间是否在上班时间内"""
        current_time = dt.time()
        
        for slot in self.config['work_time_slots']:
            start_time = time.fromisoformat(slot['start'])
            end_time = time.fromisoformat(slot['end'])
            
            if start_time <= current_time <= end_time:
                return True
        
        return False
    
    def detect_idle_periods(self, collector_id: int, actions: List[Dict]) -> List[Dict]:
        """检测空闲时段"""
        idle_periods = []
        threshold = self.config['idle_threshold_minutes']
        
        if len(actions) < 2:
            return idle_periods
        
        for i in range(len(actions) - 1):
            current = actions[i]
            next_action = actions[i + 1]
            
            gap = (next_action['timestamp'] - current['timestamp']).total_seconds() / 60
            
            if gap >= threshold:
                if self._is_work_time(current['timestamp']) and self._is_work_time(next_action['timestamp']):
                    idle_periods.append({
                        'start_time': current['timestamp'],
                        'end_time': next_action['timestamp'],
                        'duration_minutes': int(gap),
                        'before_action': {
                            'type': current['type'],
                            'time': current['timestamp'].isoformat(),
                            'details': current.get('details', '')
                        },
                        'after_action': {
                            'type': next_action['type'],
                            'time': next_action['timestamp'].isoformat(),
                            'details': next_action.get('details', '')
                        }
                    })
        
        return idle_periods
    
    def _get_case_statistics(self, collector_id: int) -> Dict:
        """获取催员的案件管理情况"""
        cases = self.db.query(Case).filter(
            Case.collector_id == collector_id
        ).all()
        
        return {
            'total': len(cases),
            'collected': 0,
            'total_amount': Decimal('0'),
            'collected_amount': Decimal('0')
        }
    
    def save_idle_records(self, collector_id: int, idle_periods: List[Dict]):
        """保存空闲记录"""
        collector = self.db.query(Collector).filter(Collector.id == collector_id).first()
        if not collector:
            return
        
        self.db.query(CollectorIdleRecord).filter(
            and_(
                CollectorIdleRecord.collector_id == collector_id,
                CollectorIdleRecord.idle_date == self.calc_date
            )
        ).delete()
        
        for period in idle_periods:
            record = CollectorIdleRecord(
                tenant_id=self.tenant_id,
                collector_id=collector_id,
                agency_id=collector.agency_id,
                team_id=collector.team_id,
                idle_date=self.calc_date,
                idle_start_time=period['start_time'],
                idle_end_time=period['end_time'],
                idle_duration_minutes=period['duration_minutes'],
                before_action=period['before_action'],
                after_action=period['after_action'],
                config_id=self.config['config_id']
            )
            self.db.add(record)
    
    def save_idle_statistics(self, collector_id: int, idle_periods: List[Dict]):
        """保存空闲统计数据"""
        collector = self.db.query(Collector).filter(Collector.id == collector_id).first()
        if not collector:
            return
        
        self.db.query(CollectorIdleStats).filter(
            and_(
                CollectorIdleStats.collector_id == collector_id,
                CollectorIdleStats.stat_date == self.calc_date
            )
        ).delete()
        
        idle_count = len(idle_periods)
        total_idle_minutes = sum(p['duration_minutes'] for p in idle_periods)
        longest_idle = max([p['duration_minutes'] for p in idle_periods]) if idle_periods else 0
        avg_idle = total_idle_minutes / idle_count if idle_count > 0 else 0
        
        work_minutes = 0
        for slot in self.config['work_time_slots']:
            start = time.fromisoformat(slot['start'])
            end = time.fromisoformat(slot['end'])
            start_minutes = start.hour * 60 + start.minute
            end_minutes = end.hour * 60 + end.minute
            work_minutes += (end_minutes - start_minutes)
        
        idle_rate = total_idle_minutes / work_minutes if work_minutes > 0 else 0
        case_stats = self._get_case_statistics(collector_id)
        
        stats = CollectorIdleStats(
            tenant_id=self.tenant_id,
            collector_id=collector_id,
            agency_id=collector.agency_id,
            team_id=collector.team_id,
            stat_date=self.calc_date,
            idle_count=idle_count,
            total_idle_minutes=total_idle_minutes,
            longest_idle_minutes=longest_idle,
            avg_idle_minutes=Decimal(str(avg_idle)),
            work_minutes=work_minutes,
            idle_rate=Decimal(str(idle_rate)),
            managed_cases_total=case_stats['total'],
            managed_cases_collected=case_stats['collected'],
            managed_amount_total=case_stats['total_amount'],
            managed_amount_collected=case_stats['collected_amount']
        )
        self.db.add(stats)
    
    def calculate_for_collector(self, collector: Collector):
        """计算单个催员的空闲数据"""
        actions = self._get_collector_actions(collector.id)
        idle_periods = self.detect_idle_periods(collector.id, actions)
        
        if idle_periods:
            self.add_log(f"   催员 {collector.collector_name}: 发现 {len(idle_periods)} 个空闲时段")
            self.save_idle_records(collector.id, idle_periods)
            self.save_idle_statistics(collector.id, idle_periods)
        else:
            self.save_idle_statistics(collector.id, [])
    
    def run(self) -> Dict:
        """执行计算"""
        self.add_log(f"🚀 开始计算 - 甲方ID: {self.tenant_id}, 日期: {self.calc_date}")
        
        collectors = self._get_collectors()
        
        if not collectors:
            self.add_log("❌ 没有找到活跃催员")
            return {'success': False, 'message': '没有找到活跃催员', 'log': self.log}
        
        success_count = 0
        for collector in collectors:
            try:
                self.calculate_for_collector(collector)
                success_count += 1
            except Exception as e:
                self.add_log(f"   ❌ 催员 {collector.id} 计算失败: {str(e)}")
        
        try:
            self.db.commit()
            self.add_log(f"✅ 计算完成！成功: {success_count}/{len(collectors)} 个催员")
            return {
                'success': True,
                'message': f'计算完成，成功 {success_count}/{len(collectors)} 个催员',
                'log': self.log,
                'stats': {
                    'total_collectors': len(collectors),
                    'success_count': success_count,
                    'fail_count': len(collectors) - success_count
                }
            }
        except Exception as e:
            self.db.rollback()
            self.add_log(f"❌ 保存失败: {str(e)}")
            return {'success': False, 'message': f'保存失败: {str(e)}', 'log': self.log}


@router.post("/calculate")
async def calculate_idle_data(
    tenant_id: int = Query(..., description="甲方ID"),
    calc_date: Optional[str] = Query(None, description="计算日期 YYYY-MM-DD，默认为昨天"),
    db: Session = Depends(get_db)
):
    """
    计算空闲数据
    
    立即执行计算并返回结果
    """
    if calc_date:
        try:
            target_date = datetime.strptime(calc_date, '%Y-%m-%d').date()
        except ValueError:
            raise HTTPException(status_code=400, detail="日期格式错误，应为 YYYY-MM-DD")
    else:
        target_date = date.today() - timedelta(days=1)
    
    calculator = IdleCalculator(db, tenant_id, target_date)
    result = calculator.run()
    
    return result


@router.post("/calculate-async")
async def calculate_idle_data_async(
    background_tasks: BackgroundTasks,
    tenant_id: int = Query(..., description="甲方ID"),
    calc_date: Optional[str] = Query(None, description="计算日期 YYYY-MM-DD，默认为昨天"),
):
    """
    异步计算空闲数据
    
    将计算任务放入后台队列，立即返回
    """
    if calc_date:
        try:
            target_date = datetime.strptime(calc_date, '%Y-%m-%d').date()
        except ValueError:
            raise HTTPException(status_code=400, detail="日期格式错误，应为 YYYY-MM-DD")
    else:
        target_date = date.today() - timedelta(days=1)
    
    def calc_task():
        from app.core.database import get_db
        db = next(get_db())
        try:
            calculator = IdleCalculator(db, tenant_id, target_date)
            calculator.run()
        finally:
            db.close()
    
    background_tasks.add_task(calc_task)
    
    return {
        'success': True,
        'message': '计算任务已提交到后台队列',
        'tenant_id': tenant_id,
        'calc_date': target_date.isoformat()
    }


@router.post("/generate-test-data")
async def generate_test_data(
    tenant_id: int = Query(..., description="甲方ID"),
    test_date: Optional[str] = Query(None, description="测试日期 YYYY-MM-DD，默认为今天"),
    collector_count: int = Query(5, description="催员数量"),
    db: Session = Depends(get_db)
):
    """
    生成测试数据
    
    为指定日期生成模拟的通信记录
    """
    if test_date:
        try:
            target_date = datetime.strptime(test_date, '%Y-%m-%d').date()
        except ValueError:
            raise HTTPException(status_code=400, detail="日期格式错误，应为 YYYY-MM-DD")
    else:
        target_date = date.today()
    
    # 优先选择有案件的催员
    collectors = db.query(Collector).join(
        Case, Collector.id == Case.collector_id
    ).filter(
        and_(
            Collector.tenant_id == tenant_id,
            Collector.is_active == True
        )
    ).distinct().limit(collector_count).all()
    
    # 如果没有有案件的催员，则选择所有催员
    if not collectors:
        collectors = db.query(Collector).filter(
            and_(
                Collector.tenant_id == tenant_id,
                Collector.is_active == True
            )
        ).limit(collector_count).all()
    
    if not collectors:
        raise HTTPException(status_code=404, detail="没有找到催员")
    
    generated_count = 0
    log = []
    
    # 获取当前最大ID
    max_id_result = db.query(func.max(CommunicationRecord.id)).scalar()
    next_id = (max_id_result or 0) + 1
    
    for collector in collectors:
        cases = db.query(Case).filter(
            Case.collector_id == collector.id
        ).limit(5).all()
        
        if not cases:
            log.append(f"⚠️  催员 {collector.collector_name} 没有案件，跳过")
            continue
        
        test_case = choice(cases)
        
        contacts = db.query(CaseContact).filter(
            CaseContact.case_id == test_case.id
        ).limit(3).all()
        
        if not contacts:
            log.append(f"⚠️  案件 {test_case.id} 没有联系人，跳过")
            continue
        
        actions = []
        
        # 上午
        current_time = datetime.combine(target_date, datetime.strptime('09:00', '%H:%M').time())
        end_morning = datetime.combine(target_date, datetime.strptime('12:00', '%H:%M').time())
        
        while current_time < end_morning:
            if rand_random() < 0.8:
                channel = choice([ChannelEnum.PHONE, ChannelEnum.WHATSAPP, ChannelEnum.SMS])
                record = CommunicationRecord(
                    id=next_id,
                    case_id=test_case.id,
                    collector_id=collector.id,
                    contact_person_id=choice(contacts).id,
                    channel=channel,
                    direction=DirectionEnum.OUTBOUND,
                    contact_result=choice([ContactResultEnum.CONNECTED, ContactResultEnum.NOT_CONNECTED]),
                    contacted_at=current_time,
                    call_duration=randint(30, 300) if channel == ChannelEnum.PHONE else None
                )
                actions.append(record)
                next_id += 1
            current_time += timedelta(minutes=randint(10, 20))
        
        # 下午（包含空闲）
        current_time = datetime.combine(target_date, datetime.strptime('14:00', '%H:%M').time())
        end_afternoon = datetime.combine(target_date, datetime.strptime('18:00', '%H:%M').time())
        
        idle_start = None
        idle_duration = 0
        if rand_random() < 0.6:
            idle_start_minutes = randint(30, 120)
            idle_start = current_time + timedelta(minutes=idle_start_minutes)
            idle_duration = randint(30, 60)
        
        while current_time < end_afternoon:
            if idle_start and idle_start <= current_time < (idle_start + timedelta(minutes=idle_duration)):
                current_time += timedelta(minutes=5)
                continue
            
            if rand_random() < 0.8:
                channel = choice([ChannelEnum.PHONE, ChannelEnum.WHATSAPP, ChannelEnum.SMS])
                record = CommunicationRecord(
                    id=next_id,
                    case_id=test_case.id,
                    collector_id=collector.id,
                    contact_person_id=choice(contacts).id,
                    channel=channel,
                    direction=DirectionEnum.OUTBOUND,
                    contact_result=choice([ContactResultEnum.CONNECTED, ContactResultEnum.NOT_CONNECTED]),
                    contacted_at=current_time,
                    call_duration=randint(30, 300) if channel == ChannelEnum.PHONE else None
                )
                actions.append(record)
                next_id += 1
            current_time += timedelta(minutes=randint(8, 15))
        
        # 删除旧数据
        db.query(CommunicationRecord).filter(
            and_(
                CommunicationRecord.collector_id == collector.id,
                func.date(CommunicationRecord.contacted_at) == target_date
            )
        ).delete()
        
        # 保存新数据
        for action in actions:
            db.add(action)
        
        generated_count += 1
        log.append(f"✅ 催员 {collector.collector_name}: 生成 {len(actions)} 条记录")
    
    try:
        db.commit()
        return {
            'success': True,
            'message': f'成功为 {generated_count}/{len(collectors)} 个催员生成测试数据',
            'log': log,
            'stats': {
                'total_collectors': len(collectors),
                'generated_count': generated_count
            },
            'next_step': {
                'message': '下一步：运行计算',
                'api': f'/api/v1/idle-monitor/calculate?tenant_id={tenant_id}&calc_date={target_date.isoformat()}'
            }
        }
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"保存失败: {str(e)}")

