from pydantic import BaseModel
from typing import Optional
from datetime import datetime


class FieldGroupBase(BaseModel):
    group_key: str
    group_name: str
    group_name_en: Optional[str] = None
    parent_id: Optional[int] = None
    sort_order: int = 0
    is_active: bool = True


class FieldGroupCreate(FieldGroupBase):
    pass


class FieldGroupUpdate(BaseModel):
    group_name: Optional[str] = None
    group_name_en: Optional[str] = None
    parent_id: Optional[int] = None
    sort_order: Optional[int] = None
    is_active: Optional[bool] = None


class FieldGroupResponse(FieldGroupBase):
    id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True

