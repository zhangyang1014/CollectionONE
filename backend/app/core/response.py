# -*- coding: utf-8 -*-
"""
统一API响应格式
"""
from typing import Any, Optional
from pydantic import BaseModel


class ApiResponse(BaseModel):
    """统一API响应"""
    code: int = 200
    message: str = "success"
    data: Optional[Any] = None


def success_response(data: Any = None, message: str = "success") -> dict:
    """成功响应"""
    return {
        "code": 200,
        "message": message,
        "data": data
    }


def error_response(code: int = 500, message: str = "error", data: Any = None) -> dict:
    """错误响应"""
    return {
        "code": code,
        "message": message,
        "data": data
    }

