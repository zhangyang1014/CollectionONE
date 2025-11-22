# -*- coding: utf-8 -*-
"""
还款码管理API（催员IM端）
"""
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import and_, or_
from typing import Optional
from datetime import datetime, timedelta
import uuid
import httpx

from app.core.database import get_db
from app.core.response import success_response, error_response
from app.models.payment_channel import PaymentChannel
from app.models.payment_code import PaymentCode
from app.models.case import Case
from app.schemas.payment import (
    PaymentCodeRequest,
    PaymentCodeResponse,
    PaymentCodeListItem,
    PaymentCodeDetail,
    InstallmentInfo,
    InstallmentListResponse
)

router = APIRouter(prefix="/api/im", tags=["还款码管理"])


@router.get("/payment-channels", summary="获取可用还款渠道")
async def get_available_channels(
    party_id: int = Query(..., description="甲方ID"),
    db: Session = Depends(get_db)
):
    """
    获取该甲方已启用的还款渠道列表（供催员选择）
    
    - 仅返回已启用的渠道
    - 按排序权重排序
    """
    try:
        channels = db.query(PaymentChannel).filter(
            and_(
                PaymentChannel.party_id == party_id,
                PaymentChannel.is_enabled == True
            )
        ).order_by(PaymentChannel.sort_order).all()
        
        result = [
            {
                "id": ch.id,
                "channel_name": ch.channel_name,
                "channel_icon": ch.channel_icon,
                "channel_type": ch.channel_type,
                "service_provider": ch.service_provider,
                "sort_order": ch.sort_order
            }
            for ch in channels
        ]
        
        return success_response(data=result)
    
    except Exception as e:
        return error_response(message=f"获取可用渠道失败: {str(e)}")


@router.get("/cases/{case_id}/installments", summary="获取期数信息")
async def get_case_installments(
    case_id: int,
    db: Session = Depends(get_db)
):
    """
    获取案件的期数信息
    
    - 返回所有期数的应还金额明细
    - 标注当前逾期期数
    """
    try:
        # TODO: 这里需要根据实际的分期表结构来查询
        # 现在返回模拟数据
        
        # 示例数据
        mock_data = {
            "total_installments": 12,
            "current_overdue": 2,
            "installments": [
                {
                    "number": 1,
                    "status": "PAID",
                    "due_date": "2025-10-01",
                    "overdue_days": None,
                    "principal": 5000.00,
                    "interest": 200.00,
                    "penalty": 0.00,
                    "fee": 50.00,
                    "total": 5250.00
                },
                {
                    "number": 2,
                    "status": "OVERDUE",
                    "due_date": "2025-11-01",
                    "overdue_days": 20,
                    "principal": 5000.00,
                    "interest": 200.00,
                    "penalty": 100.00,
                    "fee": 50.00,
                    "total": 5350.00
                },
                {
                    "number": 3,
                    "status": "PENDING",
                    "due_date": "2025-12-01",
                    "overdue_days": None,
                    "principal": 5000.00,
                    "interest": 200.00,
                    "penalty": 0.00,
                    "fee": 50.00,
                    "total": 5250.00
                }
            ]
        }
        
        return success_response(data=mock_data)
    
    except Exception as e:
        return error_response(message=f"获取期数信息失败: {str(e)}")


@router.post("/payment-codes/request", summary="请求还款码")
async def request_payment_code(
    request_data: PaymentCodeRequest,
    # TODO: 添加当前用户信息依赖
    # current_user = Depends(get_current_collector)
    db: Session = Depends(get_db)
):
    """
    请求生成还款码
    
    - 调用第三方支付接口生成还款码
    - 保存还款码记录
    - 返回还款码信息
    """
    try:
        # 1. 获取渠道配置
        channel = db.query(PaymentChannel).filter(
            PaymentChannel.id == request_data.channel_id
        ).first()
        
        if not channel:
            return error_response(message="渠道不存在", code=404)
        
        if not channel.is_enabled:
            return error_response(message="该渠道已禁用", code=400)
        
        # 2. 获取案件信息
        case = db.query(Case).filter(Case.id == request_data.case_id).first()
        if not case:
            return error_response(message="案件不存在", code=404)
        
        # TODO: 验证催员是否有权限操作该案件
        
        # 3. 生成还款码编号
        code_no = f"PAY{datetime.now().strftime('%Y%m%d')}{str(uuid.uuid4())[:8].upper()}"
        
        # 4. 替换接口参数中的占位符
        request_params = channel.request_params.copy() if channel.request_params else {}
        
        # 替换变量
        replacements = {
            "{loan_id}": str(request_data.loan_id),
            "{case_id}": str(request_data.case_id),
            "{installment_number}": str(request_data.installment_number) if request_data.installment_number else "",
            "{amount}": str(request_data.amount),
            "{customer_name}": case.customer_name or "",
            "{customer_phone}": case.customer_phone or "",
            "{customer_id}": str(case.customer_id) if case.customer_id else ""
        }
        
        # 递归替换JSON中的占位符
        def replace_placeholders(obj, replacements):
            if isinstance(obj, dict):
                return {k: replace_placeholders(v, replacements) for k, v in obj.items()}
            elif isinstance(obj, list):
                return [replace_placeholders(item, replacements) for item in obj]
            elif isinstance(obj, str):
                for placeholder, value in replacements.items():
                    obj = obj.replace(placeholder, value)
                return obj
            return obj
        
        request_params = replace_placeholders(request_params, replacements)
        
        # 5. 调用第三方接口
        # TODO: 根据auth_type添加认证头
        third_party_response = None
        payment_code_value = None
        qr_image_url = None
        expired_at = None
        
        try:
            # 模拟调用第三方接口
            # async with httpx.AsyncClient() as client:
            #     response = await client.request(
            #         method=channel.api_method,
            #         url=channel.api_url,
            #         json=request_params,
            #         timeout=10.0
            #     )
            #     third_party_response = response.json()
            
            # 模拟第三方返回数据
            third_party_response = {
                "status": "success",
                "order_id": f"TPO{datetime.now().strftime('%Y%m%d%H%M%S')}",
                "payment_code": f"8808{str(uuid.uuid4().int)[:12]}",
                "expired_at": (datetime.now() + timedelta(hours=24)).isoformat()
            }
            
            payment_code_value = third_party_response.get("payment_code")
            expired_at = datetime.fromisoformat(third_party_response.get("expired_at"))
            
            if channel.channel_type == "QR":
                qr_image_url = third_party_response.get("qr_image_url")
        
        except Exception as api_error:
            return error_response(message=f"调用第三方接口失败: {str(api_error)}")
        
        # 6. 保存还款码记录
        payment_code = PaymentCode(
            code_no=code_no,
            party_id=channel.party_id,
            channel_id=channel.id,
            case_id=request_data.case_id,
            loan_id=request_data.loan_id,
            customer_id=case.customer_id,
            collector_id=1,  # TODO: 使用实际的催员ID
            installment_number=request_data.installment_number,
            amount=request_data.amount,
            currency=case.currency or "IDR",
            payment_type=channel.channel_type,
            payment_code=payment_code_value,
            qr_image_url=qr_image_url,
            status="PENDING",
            expired_at=expired_at,
            third_party_order_id=third_party_response.get("order_id"),
            third_party_response=third_party_response,
            request_params=request_params
        )
        
        db.add(payment_code)
        db.commit()
        db.refresh(payment_code)
        
        # 7. 返回结果
        result = {
            "code_no": code_no,
            "payment_type": channel.channel_type,
            "payment_code": payment_code_value,
            "qr_image_url": qr_image_url,
            "channel_name": channel.channel_name,
            "channel_icon": channel.channel_icon,
            "amount": float(request_data.amount),
            "currency": payment_code.currency,
            "expired_at": expired_at,
            "created_at": payment_code.created_at
        }
        
        return success_response(data=result, message="还款码生成成功")
    
    except Exception as e:
        db.rollback()
        return error_response(message=f"请求还款码失败: {str(e)}")


@router.get("/payment-codes", summary="查询还款码列表")
async def get_payment_codes(
    case_id: Optional[int] = Query(None, description="案件ID"),
    status: Optional[str] = Query(None, description="状态：PENDING/PAID/EXPIRED"),
    page: int = Query(1, ge=1, description="页码"),
    page_size: int = Query(20, ge=1, le=100, description="每页数量"),
    # TODO: current_user = Depends(get_current_collector),
    db: Session = Depends(get_db)
):
    """
    查询还款码列表
    
    - 催员只能查看自己请求的还款码
    - 支持按案件和状态筛选
    """
    try:
        # TODO: 使用实际的催员ID
        collector_id = 1
        
        # 构建查询
        query = db.query(PaymentCode, PaymentChannel).join(
            PaymentChannel,
            PaymentCode.channel_id == PaymentChannel.id
        ).filter(PaymentCode.collector_id == collector_id)
        
        if case_id:
            query = query.filter(PaymentCode.case_id == case_id)
        
        if status:
            query = query.filter(PaymentCode.status == status)
        
        # 按创建时间倒序
        query = query.order_by(PaymentCode.created_at.desc())
        
        # 计算总数
        total = query.count()
        
        # 分页
        offset = (page - 1) * page_size
        results = query.offset(offset).limit(page_size).all()
        
        # 构建返回数据
        result_list = []
        for payment_code, channel in results:
            # 计算剩余秒数
            remaining_seconds = None
            if payment_code.status == "PENDING" and payment_code.expired_at:
                delta = payment_code.expired_at - datetime.now()
                remaining_seconds = max(0, int(delta.total_seconds()))
            
            result_list.append({
                "id": payment_code.id,
                "code_no": payment_code.code_no,
                "channel_name": channel.channel_name,
                "channel_icon": channel.channel_icon,
                "payment_type": payment_code.payment_type,
                "installment_number": payment_code.installment_number,
                "amount": float(payment_code.amount),
                "currency": payment_code.currency,
                "status": payment_code.status,
                "created_at": payment_code.created_at,
                "expired_at": payment_code.expired_at,
                "paid_at": payment_code.paid_at,
                "remaining_seconds": remaining_seconds
            })
        
        return success_response(data={
            "total": total,
            "page": page,
            "page_size": page_size,
            "list": result_list
        })
    
    except Exception as e:
        return error_response(message=f"查询还款码列表失败: {str(e)}")


@router.get("/payment-codes/{code_no}", summary="查询还款码详情")
async def get_payment_code_detail(
    code_no: str,
    # TODO: current_user = Depends(get_current_collector),
    db: Session = Depends(get_db)
):
    """查询还款码详情"""
    try:
        # 联表查询
        result = db.query(PaymentCode, PaymentChannel, Case).join(
            PaymentChannel,
            PaymentCode.channel_id == PaymentChannel.id
        ).join(
            Case,
            PaymentCode.case_id == Case.id
        ).filter(PaymentCode.code_no == code_no).first()
        
        if not result:
            return error_response(message="还款码不存在", code=404)
        
        payment_code, channel, case = result
        
        # TODO: 验证催员权限
        
        # 构建详情数据
        detail = {
            "code_no": payment_code.code_no,
            "party_id": payment_code.party_id,
            "channel_id": payment_code.channel_id,
            "channel_name": channel.channel_name,
            "channel_icon": channel.channel_icon,
            "service_provider": channel.service_provider,
            "payment_type": payment_code.payment_type,
            "payment_code": payment_code.payment_code,
            "qr_image_url": payment_code.qr_image_url,
            "case_id": payment_code.case_id,
            "case_no": case.case_no if hasattr(case, 'case_no') else None,
            "loan_id": payment_code.loan_id,
            "loan_no": None,  # TODO: 如果有贷款编号字段
            "customer_name": case.customer_name,
            "installment_number": payment_code.installment_number,
            "amount": float(payment_code.amount),
            "currency": payment_code.currency,
            "status": payment_code.status,
            "created_at": payment_code.created_at,
            "expired_at": payment_code.expired_at,
            "paid_at": payment_code.paid_at
        }
        
        return success_response(data=detail)
    
    except Exception as e:
        return error_response(message=f"查询还款码详情失败: {str(e)}")


@router.post("/webhook/payment-callback", summary="支付回调Webhook")
async def payment_webhook(
    webhook_data: dict,
    db: Session = Depends(get_db)
):
    """
    接收第三方支付平台的支付通知
    
    - 验证签名
    - 更新还款码状态
    - 同步案件还款记录
    """
    try:
        # TODO: 验证签名
        
        # 获取第三方订单ID
        third_party_order_id = webhook_data.get("order_id")
        if not third_party_order_id:
            return error_response(message="缺少订单ID")
        
        # 查询还款码记录
        payment_code = db.query(PaymentCode).filter(
            PaymentCode.third_party_order_id == third_party_order_id
        ).first()
        
        if not payment_code:
            return error_response(message="订单不存在", code=404)
        
        # 更新状态
        webhook_status = webhook_data.get("status")
        if webhook_status == "paid" or webhook_status == "success":
            payment_code.status = "PAID"
            payment_code.paid_at = datetime.now()
            
            # TODO: 同步更新案件还款记录
        
        elif webhook_status == "expired":
            payment_code.status = "EXPIRED"
        
        # 更新第三方响应
        payment_code.third_party_response = webhook_data
        
        db.commit()
        
        return success_response(message="处理成功")
    
    except Exception as e:
        db.rollback()
        return error_response(message=f"处理支付回调失败: {str(e)}")

