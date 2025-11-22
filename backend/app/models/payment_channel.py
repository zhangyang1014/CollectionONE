# -*- coding: utf-8 -*-
"""
还款渠道配置模型
"""
from sqlalchemy import Column, BigInteger, String, Text, Enum, Integer, Boolean, TIMESTAMP, JSON
from sqlalchemy.sql import func
from app.core.database import Base


class PaymentChannel(Base):
    """还款渠道配置表"""
    __tablename__ = "payment_channels"

    id = Column(BigInteger, primary_key=True, index=True, comment="主键ID")
    party_id = Column(BigInteger, nullable=False, index=True, comment="甲方ID")
    channel_name = Column(String(100), nullable=False, comment="支付名称")
    channel_icon = Column(String(500), comment="图标URL")
    channel_type = Column(
        Enum('VA', 'H5', 'QR', name='channel_type_enum'),
        nullable=False,
        comment="支付类型：VA-虚拟账户，H5-H5链接，QR-二维码"
    )
    service_provider = Column(String(100), comment="服务公司")
    description = Column(Text, comment="渠道描述")
    api_url = Column(String(500), nullable=False, comment="API地址")
    api_method = Column(
        Enum('GET', 'POST', name='api_method_enum'),
        default='POST',
        comment="请求方法"
    )
    auth_type = Column(
        Enum('API_KEY', 'BEARER', 'BASIC', name='auth_type_enum'),
        nullable=False,
        comment="认证方式"
    )
    auth_config = Column(JSON, comment="认证配置（加密存储）")
    request_params = Column(JSON, comment="接口入参模板")
    is_enabled = Column(Boolean, default=True, comment="是否启用：1-启用，0-禁用")
    sort_order = Column(Integer, default=0, comment="排序权重，越小越靠前")
    created_by = Column(BigInteger, comment="创建人ID")
    updated_by = Column(BigInteger, comment="更新人ID")
    created_at = Column(TIMESTAMP, server_default=func.now(), comment="创建时间")
    updated_at = Column(
        TIMESTAMP,
        server_default=func.now(),
        onupdate=func.now(),
        comment="更新时间"
    )

