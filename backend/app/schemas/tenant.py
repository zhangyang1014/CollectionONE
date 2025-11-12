from pydantic import BaseModel
from typing import Optional
from datetime import datetime


class TenantBase(BaseModel):
    tenant_code: str
    tenant_name: str
    tenant_name_en: Optional[str] = None
    country_code: Optional[str] = None
    timezone: int = 0  # UTC偏移量（范围-12到+14）
    currency_code: str = 'USD'
    is_active: bool = True


class TenantCreate(TenantBase):
    pass


class TenantUpdate(BaseModel):
    tenant_name: Optional[str] = None
    tenant_name_en: Optional[str] = None
    country_code: Optional[str] = None
    timezone: Optional[int] = None  # UTC偏移量（范围-12到+14）
    currency_code: Optional[str] = None
    is_active: Optional[bool] = None


class TenantResponse(TenantBase):
    id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class TenantFieldConfigBase(BaseModel):
    tenant_id: int
    field_id: int
    field_type: str  # standard or custom
    is_enabled: bool = True
    is_required: bool = False
    is_readonly: bool = False
    is_visible: bool = True
    sort_order: int = 0


class TenantFieldConfigCreate(TenantFieldConfigBase):
    pass


class TenantFieldConfigUpdate(BaseModel):
    is_enabled: Optional[bool] = None
    is_required: Optional[bool] = None
    is_readonly: Optional[bool] = None
    is_visible: Optional[bool] = None
    sort_order: Optional[int] = None


class TenantFieldConfigResponse(TenantFieldConfigBase):
    id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True

