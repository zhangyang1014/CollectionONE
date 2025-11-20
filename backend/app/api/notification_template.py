"""通知模板API"""
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from app.core.database import get_db
from app.models.notification_template import NotificationTemplate
from app.schemas.notification import (
    NotificationTemplateCreate,
    NotificationTemplateUpdate,
    NotificationTemplateResponse
)
import json

router = APIRouter(prefix="/notification-templates", tags=["通知模板"])


@router.get("", response_model=List[NotificationTemplateResponse])
def get_notification_templates(
    tenant_id: Optional[int] = Query(None, description="甲方ID筛选"),
    template_type: Optional[str] = Query(None, description="模板类型筛选"),
    is_enabled: Optional[bool] = Query(None, description="启用状态筛选"),
    db: Session = Depends(get_db)
):
    """获取通知模板列表"""
    query = db.query(NotificationTemplate)
    
    if tenant_id is not None:
        query = query.filter(NotificationTemplate.tenant_id == tenant_id)
    
    if template_type:
        query = query.filter(NotificationTemplate.template_type == template_type)
    
    if is_enabled is not None:
        query = query.filter(NotificationTemplate.is_enabled == is_enabled)
    
    templates = query.order_by(NotificationTemplate.created_at.desc()).all()
    return templates


@router.get("/{template_id}", response_model=NotificationTemplateResponse)
def get_notification_template(template_id: int, db: Session = Depends(get_db)):
    """获取单个通知模板详情"""
    template = db.query(NotificationTemplate).filter(
        NotificationTemplate.id == template_id
    ).first()
    
    if not template:
        raise HTTPException(status_code=404, detail="通知模板不存在")
    
    return template


@router.post("", response_model=NotificationTemplateResponse)
def create_notification_template(
    template_data: NotificationTemplateCreate,
    db: Session = Depends(get_db)
):
    """创建通知模板"""
    # 检查template_id是否已存在
    existing = db.query(NotificationTemplate).filter(
        NotificationTemplate.template_id == template_data.template_id
    ).first()
    
    if existing:
        raise HTTPException(status_code=400, detail=f"模板ID '{template_data.template_id}' 已存在")
    
    # 创建模板
    template = NotificationTemplate(
        tenant_id=template_data.tenant_id,
        template_id=template_data.template_id,
        template_name=template_data.template_name,
        template_type=template_data.template_type,
        description=template_data.description,
        content_template=template_data.content_template,
        jump_url_template=template_data.jump_url_template,
        target_type=template_data.target_type,
        target_agencies=json.dumps(template_data.target_agencies) if template_data.target_agencies else None,
        target_teams=json.dumps(template_data.target_teams) if template_data.target_teams else None,
        target_collectors=json.dumps(template_data.target_collectors) if template_data.target_collectors else None,
        is_forced_read=template_data.is_forced_read,
        repeat_interval_minutes=template_data.repeat_interval_minutes,
        max_remind_count=template_data.max_remind_count,
        notify_time_start=template_data.notify_time_start,
        notify_time_end=template_data.notify_time_end,
        priority=template_data.priority,
        display_duration_seconds=template_data.display_duration_seconds,
        is_enabled=template_data.is_enabled,
        available_variables=json.dumps(template_data.available_variables) if template_data.available_variables else None
    )
    
    db.add(template)
    db.commit()
    db.refresh(template)
    
    return template


@router.put("/{template_id}", response_model=NotificationTemplateResponse)
def update_notification_template(
    template_id: int,
    template_data: NotificationTemplateUpdate,
    db: Session = Depends(get_db)
):
    """更新通知模板"""
    template = db.query(NotificationTemplate).filter(
        NotificationTemplate.id == template_id
    ).first()
    
    if not template:
        raise HTTPException(status_code=404, detail="通知模板不存在")
    
    # 更新字段
    update_data = template_data.dict(exclude_unset=True)
    
    # 处理JSON字段
    if 'target_agencies' in update_data and update_data['target_agencies'] is not None:
        update_data['target_agencies'] = json.dumps(update_data['target_agencies'])
    if 'target_teams' in update_data and update_data['target_teams'] is not None:
        update_data['target_teams'] = json.dumps(update_data['target_teams'])
    if 'target_collectors' in update_data and update_data['target_collectors'] is not None:
        update_data['target_collectors'] = json.dumps(update_data['target_collectors'])
    if 'available_variables' in update_data and update_data['available_variables'] is not None:
        update_data['available_variables'] = json.dumps(update_data['available_variables'])
    
    for key, value in update_data.items():
        setattr(template, key, value)
    
    db.commit()
    db.refresh(template)
    
    return template


@router.delete("/{template_id}")
def delete_notification_template(template_id: int, db: Session = Depends(get_db)):
    """删除通知模板"""
    template = db.query(NotificationTemplate).filter(
        NotificationTemplate.id == template_id
    ).first()
    
    if not template:
        raise HTTPException(status_code=404, detail="通知模板不存在")
    
    db.delete(template)
    db.commit()
    
    return {"message": "删除成功"}


@router.get("/types/list")
def get_template_types():
    """获取所有可用的模板类型"""
    return {
        "types": [
            {
                "value": "case_tag_change",
                "label": "案件标签变化",
                "description": "当案件标签发生变化时发送通知",
                "variables": {
                    "case_id": "案件ID",
                    "case_number": "案件编号",
                    "tag_name": "标签名称",
                    "old_tag": "旧标签",
                    "new_tag": "新标签",
                    "operator": "操作人"
                }
            },
            {
                "value": "case_payment",
                "label": "案件还款",
                "description": "当案件收到还款时发送通知",
                "variables": {
                    "case_id": "案件ID",
                    "case_number": "案件编号",
                    "amount": "还款金额",
                    "payment_time": "还款时间",
                    "payment_channel": "还款渠道",
                    "debtor_name": "债务人姓名"
                }
            },
            {
                "value": "user_app_visit",
                "label": "用户访问APP",
                "description": "当用户访问APP时发送通知",
                "variables": {
                    "case_id": "案件ID",
                    "case_number": "案件编号",
                    "user_name": "用户姓名",
                    "user_phone": "用户手机号",
                    "visit_time": "访问时间",
                    "device_type": "设备类型"
                }
            },
            {
                "value": "user_payment_page_visit",
                "label": "用户访问还款页",
                "description": "当用户访问还款页面时发送通知",
                "variables": {
                    "case_id": "案件ID",
                    "case_number": "案件编号",
                    "user_name": "用户姓名",
                    "user_phone": "用户手机号",
                    "visit_time": "访问时间",
                    "outstanding_amount": "待还金额"
                }
            },
            {
                "value": "case_assigned",
                "label": "案件分配",
                "description": "当案件被分配给催员时发送通知",
                "variables": {
                    "case_id": "案件ID",
                    "case_number": "案件编号",
                    "collector_name": "催员姓名",
                    "assign_time": "分配时间",
                    "case_amount": "案件金额"
                }
            },
            {
                "value": "ptp_reminder",
                "label": "PTP提醒",
                "description": "PTP到期前提醒",
                "variables": {
                    "case_id": "案件ID",
                    "case_number": "案件编号",
                    "ptp_date": "PTP日期",
                    "ptp_amount": "承诺金额",
                    "debtor_name": "债务人姓名"
                }
            }
        ]
    }

