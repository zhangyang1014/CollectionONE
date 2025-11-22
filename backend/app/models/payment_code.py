# -*- coding: utf-8 -*-
"""
还款码记录模型
"""
from sqlalchemy import Column, BigInteger, String, Integer, DECIMAL, Enum, TIMESTAMP, JSON
from sqlalchemy.sql import func
from app.core.database import Base


class PaymentCode(Base):
    """还款码记录表"""
    __tablename__ = "payment_codes"

    id = Column(BigInteger, primary_key=True, index=True, comment="主键ID")
    code_no = Column(String(100), unique=True, nullable=False, index=True, comment="还款码编号")
    party_id = Column(BigInteger, nullable=False, comment="甲方ID")
    channel_id = Column(BigInteger, nullable=False, comment="渠道ID")
    case_id = Column(BigInteger, nullable=False, index=True, comment="案件ID")
    loan_id = Column(BigInteger, nullable=False, index=True, comment="借款ID")
    customer_id = Column(BigInteger, nullable=False, comment="客户ID")
    collector_id = Column(BigInteger, nullable=False, index=True, comment="催员ID")
    installment_number = Column(Integer, comment="期数")
    amount = Column(DECIMAL(15, 2), nullable=False, comment="还款金额")
    currency = Column(String(10), default='IDR', comment="币种")
    payment_type = Column(
        Enum('VA', 'H5', 'QR', name='payment_type_enum'),
        nullable=False,
        comment="支付类型：VA-虚拟账户，H5-H5链接，QR-二维码"
    )
    payment_code = Column(String(500), comment="支付码内容（VA码/链接地址）")
    qr_image_url = Column(String(500), comment="二维码图片URL")
    status = Column(
        Enum('PENDING', 'PAID', 'EXPIRED', name='payment_status_enum'),
        default='PENDING',
        index=True,
        comment="状态：PENDING-待支付，PAID-已支付，EXPIRED-已过期"
    )
    created_at = Column(TIMESTAMP, server_default=func.now(), index=True, comment="创建时间")
    expired_at = Column(TIMESTAMP, index=True, comment="过期时间（由第三方接口返回）")
    paid_at = Column(TIMESTAMP, comment="支付时间")
    third_party_order_id = Column(String(200), index=True, comment="第三方订单ID")
    third_party_response = Column(JSON, comment="第三方接口完整返回")
    request_params = Column(JSON, comment="请求参数快照")

