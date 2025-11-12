from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import and_, or_
from typing import List, Optional, Dict, Any
import json
from app.core.database import get_db
from app.models.case import Case, CaseStandardFieldValue, CaseCustomFieldValue
from app.models.tenant import Tenant
from app.models.standard_field import StandardField
from app.models.custom_field import CustomField
from app.schemas.case import (
    CaseCreate,
    CaseUpdate,
    CaseResponse,
    CaseSyncRequest,
    CaseSyncResponse
)

router = APIRouter(prefix="/cases", tags=["案件管理"])


def _get_case_with_fields(db: Session, case: Case) -> Dict[str, Any]:
    """获取案件及其所有字段值"""
    case_dict = {
        "id": case.id,
        "case_code": case.case_code,
        "loan_id": case.case_code,  # 使用case_code作为loan_id
        "tenant_id": case.tenant_id,
        "queue_id": case.queue_id,
        "agency_id": case.agency_id,
        "team_id": case.team_id,
        "collector_id": case.collector_id,
        "user_id": case.user_id,
        "user_name": case.user_name,
        "mobile": case.mobile,
        "mobile_number": case.mobile,  # 兼容前端使用的mobile_number字段
        "case_status": case.case_status,
        "overdue_days": case.overdue_days,
        "loan_amount": float(case.loan_amount) if case.loan_amount else 0,
        "total_due_amount": float(case.loan_amount) if case.loan_amount else 0,  # 兼容前端使用的total_due_amount字段
        "repaid_amount": float(case.repaid_amount) if case.repaid_amount else 0,
        "outstanding_amount": float(case.outstanding_amount) if case.outstanding_amount else 0,
        "due_date": case.due_date.isoformat() if case.due_date else None,
        "settlement_date": case.settlement_date.isoformat() if case.settlement_date else None,
        "assigned_at": case.assigned_at.isoformat() if case.assigned_at else None,
        "last_contact_at": case.last_contact_at.isoformat() if case.last_contact_at else None,
        "next_follow_up_at": case.next_follow_up_at.isoformat() if case.next_follow_up_at else None,
        "created_at": case.created_at.isoformat() if case.created_at else None,
        "updated_at": case.updated_at.isoformat() if case.updated_at else None,
        "standard_fields": {},
        "custom_fields": {}
    }
    
    # 获取标准字段值
    for field_value in case.standard_field_values:
        field = db.query(StandardField).filter(StandardField.id == field_value.field_id).first()
        if field:
            case_dict["standard_fields"][field.field_key] = field_value.field_value
            # 如果标准字段中有LOAN_ID，优先使用它作为loan_id
            if field.field_key == "LOAN_ID" and field_value.field_value:
                case_dict["loan_id"] = field_value.field_value
    
    # 获取自定义字段值
    for field_value in case.custom_field_values:
        field = db.query(CustomField).filter(CustomField.id == field_value.field_id).first()
        if field:
            case_dict["custom_fields"][field.field_key] = field_value.field_value
            # 如果自定义字段中有loan_id，优先使用它作为loan_id
            if field.field_key.lower() == "loan_id" and field_value.field_value:
                case_dict["loan_id"] = field_value.field_value
    
    return case_dict


@router.get("")
def get_cases(
    tenant_id: int = Query(..., description="甲方ID"),
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    case_status: Optional[str] = Query(None, description="案件状态"),
    queue_id: Optional[int] = Query(None, description="队列ID"),
    agency_id: Optional[int] = Query(None, description="机构ID"),
    team_id: Optional[int] = Query(None, description="小组ID"),
    collector_id: Optional[int] = Query(None, description="催员ID"),
    user_id: Optional[str] = Query(None, description="用户ID"),
    due_date_start: Optional[str] = Query(None, description="到期日开始日期"),
    due_date_end: Optional[str] = Query(None, description="到期日结束日期"),
    settlement_date_start: Optional[str] = Query(None, description="结清日开始日期"),
    settlement_date_end: Optional[str] = Query(None, description="结清日结束日期"),
    db: Session = Depends(get_db)
):
    """获取案件列表"""
    from datetime import datetime
    
    query = db.query(Case).filter(Case.tenant_id == tenant_id)
    
    if case_status:
        query = query.filter(Case.case_status == case_status)
    
    if queue_id:
        query = query.filter(Case.queue_id == queue_id)
    
    if agency_id:
        query = query.filter(Case.agency_id == agency_id)
    
    if team_id:
        query = query.filter(Case.team_id == team_id)
    
    if collector_id:
        query = query.filter(Case.collector_id == collector_id)
    
    if user_id:
        query = query.filter(Case.user_id == user_id)
    
    if due_date_start:
        try:
            start_date = datetime.fromisoformat(due_date_start.replace('Z', '+00:00'))
            query = query.filter(Case.due_date >= start_date)
        except:
            pass
    
    if due_date_end:
        try:
            end_date = datetime.fromisoformat(due_date_end.replace('Z', '+00:00'))
            query = query.filter(Case.due_date <= end_date)
        except:
            pass
    
    if settlement_date_start:
        try:
            start_date = datetime.fromisoformat(settlement_date_start.replace('Z', '+00:00'))
            query = query.filter(Case.settlement_date >= start_date)
        except:
            pass
    
    if settlement_date_end:
        try:
            end_date = datetime.fromisoformat(settlement_date_end.replace('Z', '+00:00'))
            query = query.filter(Case.settlement_date <= end_date)
        except:
            pass
    
    cases = query.offset(skip).limit(limit).all()
    
    # 构建响应，包含字段值
    result = []
    for case in cases:
        result.append(_get_case_with_fields(db, case))
    
    return result


@router.get("/{case_id}", response_model=CaseResponse)
def get_case(case_id: int, db: Session = Depends(get_db)):
    """获取单个案件详情"""
    case = db.query(Case).filter(Case.id == case_id).first()
    if not case:
        raise HTTPException(status_code=404, detail="案件不存在")
    
    return _get_case_with_fields(db, case)


@router.post("", response_model=CaseResponse)
def create_case(case_data: CaseCreate, db: Session = Depends(get_db)):
    """创建案件"""
    # 检查 case_id 是否已存在
    existing = db.query(Case).filter(Case.case_id == case_data.case_id).first()
    if existing:
        raise HTTPException(status_code=400, detail="案件编号已存在")
    
    # 创建案件主记录
    case = Case(
        case_id=case_data.case_id,
        tenant_id=case_data.tenant_id,
        loan_id=case_data.loan_id,
        user_id=case_data.user_id,
        case_status=case_data.case_status
    )
    db.add(case)
    db.flush()
    
    # 保存标准字段值
    if case_data.standard_fields:
        for field_key, field_value in case_data.standard_fields.items():
            field = db.query(StandardField).filter(StandardField.field_key == field_key).first()
            if field:
                value_record = CaseStandardFieldValue(
                    case_id=case.id,
                    field_id=field.id,
                    field_value=str(field_value) if field_value is not None else None
                )
                db.add(value_record)
    
    # 保存自定义字段值
    if case_data.custom_fields:
        for field_key, field_value in case_data.custom_fields.items():
            field = db.query(CustomField).filter(
                CustomField.tenant_id == case_data.tenant_id,
                CustomField.field_key == field_key
            ).first()
            if field:
                value_record = CaseCustomFieldValue(
                    case_id=case.id,
                    field_id=field.id,
                    field_value=str(field_value) if field_value is not None else None
                )
                db.add(value_record)
    
    db.commit()
    db.refresh(case)
    
    return _get_case_with_fields(db, case)


@router.put("/{case_id}", response_model=CaseResponse)
def update_case(
    case_id: int,
    case_data: CaseUpdate,
    db: Session = Depends(get_db)
):
    """更新案件"""
    case = db.query(Case).filter(Case.id == case_id).first()
    if not case:
        raise HTTPException(status_code=404, detail="案件不存在")
    
    # 更新基本信息
    if case_data.loan_id is not None:
        case.loan_id = case_data.loan_id
    if case_data.user_id is not None:
        case.user_id = case_data.user_id
    if case_data.case_status is not None:
        case.case_status = case_data.case_status
    
    # 更新标准字段值
    if case_data.standard_fields:
        for field_key, field_value in case_data.standard_fields.items():
            field = db.query(StandardField).filter(StandardField.field_key == field_key).first()
            if field:
                value_record = db.query(CaseStandardFieldValue).filter(
                    CaseStandardFieldValue.case_id == case_id,
                    CaseStandardFieldValue.field_id == field.id
                ).first()
                
                if value_record:
                    value_record.field_value = str(field_value) if field_value is not None else None
                else:
                    value_record = CaseStandardFieldValue(
                        case_id=case_id,
                        field_id=field.id,
                        field_value=str(field_value) if field_value is not None else None
                    )
                    db.add(value_record)
    
    # 更新自定义字段值
    if case_data.custom_fields:
        for field_key, field_value in case_data.custom_fields.items():
            field = db.query(CustomField).filter(
                CustomField.tenant_id == case.tenant_id,
                CustomField.field_key == field_key
            ).first()
            if field:
                value_record = db.query(CaseCustomFieldValue).filter(
                    CaseCustomFieldValue.case_id == case_id,
                    CaseCustomFieldValue.field_id == field.id
                ).first()
                
                if value_record:
                    value_record.field_value = str(field_value) if field_value is not None else None
                else:
                    value_record = CaseCustomFieldValue(
                        case_id=case_id,
                        field_id=field.id,
                        field_value=str(field_value) if field_value is not None else None
                    )
                    db.add(value_record)
    
    db.commit()
    db.refresh(case)
    
    return _get_case_with_fields(db, case)


@router.delete("/{case_id}")
def delete_case(case_id: int, db: Session = Depends(get_db)):
    """删除案件"""
    case = db.query(Case).filter(Case.id == case_id).first()
    if not case:
        raise HTTPException(status_code=404, detail="案件不存在")
    
    # 删除相关字段值
    db.query(CaseStandardFieldValue).filter(CaseStandardFieldValue.case_id == case_id).delete()
    db.query(CaseCustomFieldValue).filter(CaseCustomFieldValue.case_id == case_id).delete()
    
    # 删除案件
    db.delete(case)
    db.commit()
    
    return {"message": "案件已删除"}


@router.post("/sync", response_model=CaseSyncResponse)
def sync_cases(sync_data: CaseSyncRequest, db: Session = Depends(get_db)):
    """数据同步接口"""
    # 查找甲方
    tenant = db.query(Tenant).filter(Tenant.tenant_code == sync_data.tenant_code).first()
    if not tenant:
        raise HTTPException(status_code=404, detail="甲方不存在")
    
    success_count = 0
    fail_count = 0
    errors = []
    
    for case_data in sync_data.cases:
        try:
            case_id = case_data.get("case_id")
            if not case_id:
                fail_count += 1
                errors.append({"case": case_data, "error": "缺少case_id"})
                continue
            
            # 检查案件是否存在
            existing_case = db.query(Case).filter(Case.case_id == case_id).first()
            
            if existing_case:
                # 更新案件
                if "loan_id" in case_data:
                    existing_case.loan_id = case_data["loan_id"]
                if "user_id" in case_data:
                    existing_case.user_id = case_data["user_id"]
                if "case_status" in case_data:
                    existing_case.case_status = case_data.get("case_status")
                
                case_obj = existing_case
            else:
                # 创建新案件
                case_obj = Case(
                    case_id=case_id,
                    tenant_id=tenant.id,
                    loan_id=case_data.get("loan_id"),
                    user_id=case_data.get("user_id"),
                    case_status=case_data.get("case_status")
                )
                db.add(case_obj)
                db.flush()
            
            # 更新标准字段
            if "standard_fields" in case_data:
                for field_key, field_value in case_data["standard_fields"].items():
                    field = db.query(StandardField).filter(StandardField.field_key == field_key).first()
                    if field:
                        value_record = db.query(CaseStandardFieldValue).filter(
                            CaseStandardFieldValue.case_id == case_obj.id,
                            CaseStandardFieldValue.field_id == field.id
                        ).first()
                        
                        if value_record:
                            value_record.field_value = str(field_value) if field_value is not None else None
                        else:
                            value_record = CaseStandardFieldValue(
                                case_id=case_obj.id,
                                field_id=field.id,
                                field_value=str(field_value) if field_value is not None else None
                            )
                            db.add(value_record)
            
            # 更新自定义字段
            if "custom_fields" in case_data:
                for field_key, field_value in case_data["custom_fields"].items():
                    field = db.query(CustomField).filter(
                        CustomField.tenant_id == tenant.id,
                        CustomField.field_key == field_key
                    ).first()
                    if field:
                        value_record = db.query(CaseCustomFieldValue).filter(
                            CaseCustomFieldValue.case_id == case_obj.id,
                            CaseCustomFieldValue.field_id == field.id
                        ).first()
                        
                        if value_record:
                            value_record.field_value = str(field_value) if field_value is not None else None
                        else:
                            value_record = CaseCustomFieldValue(
                                case_id=case_obj.id,
                                field_id=field.id,
                                field_value=str(field_value) if field_value is not None else None
                            )
                            db.add(value_record)
            
            db.commit()
            success_count += 1
            
        except Exception as e:
            db.rollback()
            fail_count += 1
            errors.append({"case_id": case_data.get("case_id"), "error": str(e)})
    
    return CaseSyncResponse(
        code=200,
        message="success",
        data={
            "success_count": success_count,
            "fail_count": fail_count,
            "errors": errors
        }
    )

