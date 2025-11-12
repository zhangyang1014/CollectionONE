from pydantic import BaseModel
from typing import Optional, Dict, Any, List
from datetime import datetime


class CaseBase(BaseModel):
    case_id: str
    tenant_id: int
    loan_id: Optional[str] = None
    user_id: Optional[str] = None
    case_status: Optional[str] = None


class CaseCreate(CaseBase):
    standard_fields: Optional[Dict[str, Any]] = {}
    custom_fields: Optional[Dict[str, Any]] = {}


class CaseUpdate(BaseModel):
    loan_id: Optional[str] = None
    user_id: Optional[str] = None
    case_status: Optional[str] = None
    standard_fields: Optional[Dict[str, Any]] = None
    custom_fields: Optional[Dict[str, Any]] = None


class CaseResponse(CaseBase):
    id: int
    standard_fields: Dict[str, Any] = {}
    custom_fields: Dict[str, Any] = {}
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class CaseSyncRequest(BaseModel):
    """数据同步请求"""
    tenant_code: str
    cases: List[Dict[str, Any]]


class CaseSyncResponse(BaseModel):
    """数据同步响应"""
    code: int = 200
    message: str = "success"
    data: Dict[str, Any]

