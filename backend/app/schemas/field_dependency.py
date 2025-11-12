from pydantic import BaseModel
from typing import Dict, Any
from datetime import datetime


class FieldDependencyBase(BaseModel):
    source_field_id: int
    target_field_id: int
    dependency_type: str  # show/hide/options_change
    dependency_rule: Dict[str, Any]


class FieldDependencyCreate(FieldDependencyBase):
    pass


class FieldDependencyUpdate(BaseModel):
    dependency_type: str
    dependency_rule: Dict[str, Any]


class FieldDependencyResponse(FieldDependencyBase):
    id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True

