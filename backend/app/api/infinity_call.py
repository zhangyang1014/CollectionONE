"""Infinity外呼核心API"""
from fastapi import APIRouter, Depends, HTTPException, BackgroundTasks
from sqlalchemy.orm import Session
from datetime import datetime
from typing import Optional
import httpx
import logging

from app.core.database import get_db
from app.models.infinity_call_config import InfinityCallConfig
from app.models.infinity_extension_pool import InfinityExtensionPool
from app.models.collector import Collector
from app.models.case import Case
from app.models.communication_record import (
    CommunicationRecord,
    ChannelEnum,
    DirectionEnum,
    ContactResultEnum
)
from app.schemas.infinity import (
    MakeCallRequest,
    MakeCallResponse,
    CallRecordCallback
)
from app.services.extension_allocator import ExtensionAllocator

router = APIRouter(prefix="/infinity", tags=["Infinity外呼"])

logger = logging.getLogger(__name__)


@router.post("/make-call", response_model=MakeCallResponse, summary="发起外呼")
def make_call(
    call_request: MakeCallRequest,
    background_tasks: BackgroundTasks,
    db: Session = Depends(get_db)
):
    """
    发起外呼
    
    流程：
    1. 验证催员和案件信息
    2. 检查催员是否配置了回呼号码
    3. 从分机池分配空闲分机
    4. 调用 Infinity MakeCall API
    5. 创建通信记录
    6. 返回呼叫信息
    """
    try:
        # 1. 验证案件是否存在
        case = db.query(Case).filter(Case.id == call_request.case_id).first()
        if not case:
            raise HTTPException(status_code=404, detail=f"案件 ID {call_request.case_id} 不存在")
        
        # 2. 验证催员是否存在
        collector = db.query(Collector).filter(Collector.id == call_request.collector_id).first()
        if not collector:
            raise HTTPException(status_code=404, detail=f"催员 ID {call_request.collector_id} 不存在")
        
        # 3. 检查催员是否配置了回呼号码
        if not collector.callback_number:
            raise HTTPException(
                status_code=400,
                detail=f"催员 {collector.collector_name} 未配置回呼号码，无法发起外呼"
            )
        
        # 4. 获取甲方的 Infinity 配置
        config = db.query(InfinityCallConfig).filter(
            InfinityCallConfig.tenant_id == case.tenant_id,
            InfinityCallConfig.is_active == True
        ).first()
        
        if not config:
            raise HTTPException(
                status_code=404,
                detail=f"甲方 {case.tenant_id} 没有启用的 Infinity 配置"
            )
        
        # 5. 分配分机
        allocator = ExtensionAllocator(db)
        try:
            extension, extension_number = allocator.allocate_extension(
                tenant_id=case.tenant_id,
                collector_id=collector.id,
                config_id=config.id
            )
        except HTTPException as e:
            # 分配失败，返回友好错误
            raise HTTPException(
                status_code=503,
                detail=f"分机分配失败：{e.detail}"
            )
        
        # 6. 确定主叫号码
        caller_number = call_request.caller_number
        if not caller_number:
            # 从号段范围中选择主叫号码
            if config.caller_number_range_start and config.caller_number_range_end:
                # 使用号段起始号码作为主叫号码（可以后续优化为轮询或随机选择）
                caller_number = config.caller_number_range_start
            else:
                caller_number = None
        
        # 7. 调用 Infinity MakeCall API
        infinity_response = _call_infinity_api(
            api_url=config.api_url,
            access_token=config.access_token,
            extension_number=extension_number,
            callback_number=collector.callback_number,
            dest_number=call_request.contact_number,
            caller_number=caller_number,
            custom_params=call_request.custom_params
        )
        
        if not infinity_response.get('success'):
            # API调用失败，释放分机
            allocator.release_extension(
                tenant_id=case.tenant_id,
                extension_number=extension_number,
                collector_id=collector.id
            )
            raise HTTPException(
                status_code=500,
                detail=f"Infinity API调用失败：{infinity_response.get('message')}"
            )
        
        # 8. 创建通信记录
        call_uuid = infinity_response.get('call_uuid', f"CALL_{datetime.now().strftime('%Y%m%d%H%M%S')}_{case.id}")
        
        comm_record = CommunicationRecord(
            case_id=case.id,
            collector_id=collector.id,
            channel=ChannelEnum.PHONE,
            direction=DirectionEnum.OUTBOUND,
            supplier_id=config.supplier_id,
            infinity_extension_number=extension_number,
            call_uuid=call_uuid,
            custom_params=call_request.custom_params,
            is_connected=False,  # 初始状态，等待回调更新
            contact_result=ContactResultEnum.NOT_CONNECTED,  # 初始状态
            contacted_at=datetime.now()
        )
        
        db.add(comm_record)
        
        # 更新催员的当前分机号
        collector.infinity_extension_number = extension_number
        
        db.commit()
        db.refresh(comm_record)
        
        # 9. 返回响应
        return MakeCallResponse(
            success=True,
            call_id=comm_record.id,
            call_uuid=call_uuid,
            extension_number=extension_number,
            message="外呼请求已发送，请等待接听"
        )
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"外呼异常：{str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"外呼失败：{str(e)}")


@router.post("/callback/call-record", summary="接收Infinity通话记录回调")
def receive_call_record_callback(
    callback_data: CallRecordCallback,
    background_tasks: BackgroundTasks,
    db: Session = Depends(get_db)
):
    """
    接收Infinity推送的通话记录
    
    更新：
    - 通话时长
    - 通话结果
    - 录音链接
    - 释放分机
    """
    try:
        # 1. 查找对应的通信记录
        comm_record = db.query(CommunicationRecord).filter(
            CommunicationRecord.call_uuid == callback_data.call_uuid
        ).first()
        
        if not comm_record:
            logger.warning(f"未找到通话记录：call_uuid={callback_data.call_uuid}")
            return {"message": "通话记录不存在", "status": "ignored"}
        
        # 2. 更新通信记录
        comm_record.call_duration = callback_data.call_duration
        comm_record.is_connected = callback_data.is_connected
        comm_record.call_record_url = callback_data.call_record_url
        
        # 更新联系结果
        if callback_data.contact_result:
            try:
                comm_record.contact_result = ContactResultEnum(callback_data.contact_result)
            except ValueError:
                # 如果提供的值不在枚举中，使用默认值
                if callback_data.is_connected:
                    comm_record.contact_result = ContactResultEnum.CONNECTED
                else:
                    comm_record.contact_result = ContactResultEnum.NOT_CONNECTED
        else:
            # 根据是否接通自动设置
            if callback_data.is_connected:
                comm_record.contact_result = ContactResultEnum.CONNECTED
            else:
                comm_record.contact_result = ContactResultEnum.NOT_CONNECTED
        
        if callback_data.remark:
            comm_record.remark = callback_data.remark
        
        # 3. 释放分机
        if comm_record.infinity_extension_number:
            allocator = ExtensionAllocator(db)
            allocator.release_extension(
                tenant_id=comm_record.case.tenant_id,
                extension_number=comm_record.infinity_extension_number,
                collector_id=comm_record.collector_id
            )
            
            # 清除催员的当前分机号
            collector = db.query(Collector).filter(Collector.id == comm_record.collector_id).first()
            if collector and collector.infinity_extension_number == comm_record.infinity_extension_number:
                collector.infinity_extension_number = None
        
        db.commit()
        
        logger.info(f"通话记录已更新：call_uuid={callback_data.call_uuid}, 时长={callback_data.call_duration}秒")
        
        return {
            "message": "通话记录已更新",
            "status": "success",
            "call_id": comm_record.id
        }
    
    except Exception as e:
        logger.error(f"处理通话记录回调异常：{str(e)}", exc_info=True)
        db.rollback()
        raise HTTPException(status_code=500, detail=f"处理回调失败：{str(e)}")


def _call_infinity_api(
    api_url: str,
    access_token: str,
    extension_number: str,
    callback_number: str,
    dest_number: str,
    caller_number: Optional[str] = None,
    custom_params: Optional[dict] = None
) -> dict:
    """
    调用 Infinity MakeCall API
    
    Args:
        api_url: Infinity API地址
        access_token: 访问令牌
        extension_number: 分机号
        callback_number: 催员回呼号码
        dest_number: 客户号码
        caller_number: 主叫显示号码
        custom_params: 自定义参数
    
    Returns:
        dict: {success: bool, message: str, call_uuid: str}
    """
    try:
        # 构建请求参数（根据Infinity文档）
        data = {
            'service': 'App.Sip_Call.MakeCall',
            'token': access_token,
            'extnumber': extension_number,       # 分机号
            'destnumber': dest_number,            # 目标号码（客户）
        }
        
        # 主叫号码（可选）
        if caller_number:
            data['disnumber'] = caller_number
        
        # 添加自定义参数
        if custom_params:
            data['userid'] = custom_params.get('userid', '')
            data['memberid'] = custom_params.get('memberid', '')
            data['chengshudu'] = custom_params.get('chengshudu', '')
            data['customuuid'] = custom_params.get('customuuid', '')
        
        # 发送请求
        logger.info(f"调用 Infinity API: extension={extension_number}, dest={dest_number}")
        
        with httpx.Client() as client:
            response = client.post(
                api_url,
                data=data,
                timeout=10.0
            )
        
        if response.status_code == 200:
            result = response.json()
            
            # 根据Infinity返回格式判断（ret=200表示成功）
            if result.get('ret') == 200:
                return {
                    'success': True,
                    'message': '呼叫命令发送成功',
                    'call_uuid': result.get('data', {}).get('call_uuid', '')
                }
            else:
                return {
                    'success': False,
                    'message': result.get('msg', '未知错误')
                }
        else:
            return {
                'success': False,
                'message': f"HTTP错误：{response.status_code}"
            }
    
    except httpx.TimeoutException:
        return {
            'success': False,
            'message': 'API请求超时'
        }
    except Exception as e:
        logger.error(f"Infinity API调用异常：{str(e)}", exc_info=True)
        return {
            'success': False,
            'message': f'API调用异常：{str(e)}'
        }

