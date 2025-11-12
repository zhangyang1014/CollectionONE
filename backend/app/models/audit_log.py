from sqlalchemy import Column, BigInteger, String, DateTime, ForeignKey, JSON, Text
from sqlalchemy.sql import func
from app.core.database import Base


class AuditLog(Base):
    """审计日志表"""
    __tablename__ = "audit_logs"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    tenant_id = Column(BigInteger, ForeignKey('tenants.id'), comment='甲方ID')
    operation_type = Column(String(50), nullable=False, comment='操作类型：create/update/delete')
    entity_type = Column(String(50), nullable=False, comment='实体类型')
    entity_id = Column(BigInteger, comment='实体ID')
    field_id = Column(BigInteger, comment='字段ID（如果是字段相关操作）')
    before_value = Column(JSON, comment='变更前值')
    after_value = Column(JSON, comment='变更后值')
    operator_id = Column(BigInteger, nullable=False, comment='操作人ID')
    operator_name = Column(String(100), comment='操作人姓名')
    operator_ip = Column(String(50), comment='操作IP')
    remark = Column(Text, comment='备注')
    created_at = Column(DateTime, server_default=func.now())

