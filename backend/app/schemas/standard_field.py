from pydantic import BaseModel
from typing import Optional, Dict, Any, List
from datetime import datetime


class StandardFieldBase(BaseModel):
    field_key: str
    field_name: str
    field_name_en: Optional[str] = None
    field_type: str
    field_group_id: int
    is_required: bool = False
    is_extended: bool = False
    description: Optional[str] = None
    example_value: Optional[str] = None
    validation_rules: Optional[Dict[str, Any]] = None
    enum_options: Optional[List[str]] = None
    sort_order: int = 0
    is_active: bool = True


class StandardFieldCreate(StandardFieldBase):
    pass


class StandardFieldUpdate(BaseModel):
    field_name: Optional[str] = None
    field_name_en: Optional[str] = None
    field_type: Optional[str] = None
    field_group_id: Optional[int] = None
    is_required: Optional[bool] = None
    is_extended: Optional[bool] = None
    description: Optional[str] = None
    example_value: Optional[str] = None
    validation_rules: Optional[Dict[str, Any]] = None
    enum_options: Optional[List[str]] = None
    sort_order: Optional[int] = None
    is_active: Optional[bool] = None


class StandardFieldResponse(StandardFieldBase):
    id: int
    is_deleted: bool
    deleted_at: Optional[datetime] = None
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True

