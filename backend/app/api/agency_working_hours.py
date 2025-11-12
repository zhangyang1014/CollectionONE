"""机构作息时间管理 API"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from app.core.database import get_db
from app.models.agency_working_hours import AgencyWorkingHours
from app.models.collection_agency import CollectionAgency
from app.schemas.organization import (
    AgencyWorkingHoursResponse,
    AgencyWorkingHoursCreate,
    AgencyWorkingHoursUpdate,
    AgencyWorkingHoursBatchUpdate,
    TimeSlot
)

router = APIRouter(prefix="/agencies/{agency_id}/working-hours", tags=["机构作息时间管理"])


def validate_time_slots(time_slots: List[TimeSlot]):
    """验证时间段"""
    if len(time_slots) > 5:
        raise HTTPException(status_code=400, detail="每天最多只能配置5个时间段")
    
    # 验证时间格式和逻辑
    for slot in time_slots:
        try:
            start_hour, start_min = map(int, slot.start.split(':'))
            end_hour, end_min = map(int, slot.end.split(':'))
            
            if not (0 <= start_hour < 24 and 0 <= start_min < 60):
                raise HTTPException(status_code=400, detail=f"开始时间格式错误：{slot.start}")
            if not (0 <= end_hour < 24 and 0 <= end_min < 60):
                raise HTTPException(status_code=400, detail=f"结束时间格式错误：{slot.end}")
            
            start_total = start_hour * 60 + start_min
            end_total = end_hour * 60 + end_min
            
            if start_total >= end_total:
                raise HTTPException(status_code=400, detail=f"开始时间必须小于结束时间：{slot.start} - {slot.end}")
        except ValueError:
            raise HTTPException(status_code=400, detail=f"时间格式错误，应为HH:MM格式：{slot.start} 或 {slot.end}")
    
    # 验证时间段不重叠
    sorted_slots = sorted(time_slots, key=lambda x: x.start)
    for i in range(len(sorted_slots) - 1):
        current_end = sorted_slots[i].end
        next_start = sorted_slots[i + 1].start
        
        current_end_total = int(current_end.split(':')[0]) * 60 + int(current_end.split(':')[1])
        next_start_total = int(next_start.split(':')[0]) * 60 + int(next_start.split(':')[1])
        
        if current_end_total > next_start_total:
            raise HTTPException(status_code=400, detail="时间段不能重叠")


@router.get("", response_model=List[AgencyWorkingHoursResponse])
def get_agency_working_hours(
    agency_id: int,
    db: Session = Depends(get_db)
):
    """获取机构作息时间（返回7天的数据）"""
    # 检查机构是否存在
    agency = db.query(CollectionAgency).filter(CollectionAgency.id == agency_id).first()
    if not agency:
        raise HTTPException(status_code=404, detail="机构不存在")
    
    # 获取所有作息时间记录
    working_hours = db.query(AgencyWorkingHours).filter(
        AgencyWorkingHours.agency_id == agency_id
    ).order_by(AgencyWorkingHours.day_of_week).all()
    
    # 如果不存在，返回默认的7天空数据
    if not working_hours:
        result = []
        for day in range(7):
            result.append({
                "id": 0,
                "agency_id": agency_id,
                "day_of_week": day,
                "time_slots": []
            })
        return result
    
    return working_hours


@router.put("", response_model=List[AgencyWorkingHoursResponse])
def update_agency_working_hours(
    agency_id: int,
    batch_update: AgencyWorkingHoursBatchUpdate,
    db: Session = Depends(get_db)
):
    """批量更新机构作息时间"""
    # 检查机构是否存在
    agency = db.query(CollectionAgency).filter(CollectionAgency.id == agency_id).first()
    if not agency:
        raise HTTPException(status_code=404, detail="机构不存在")
    
    # 验证必须包含7天的数据
    if len(batch_update.working_hours) != 7:
        raise HTTPException(status_code=400, detail="必须提供7天的作息时间配置")
    
    # 验证每天的数据
    days_set = set()
    for wh in batch_update.working_hours:
        if wh.agency_id != agency_id:
            raise HTTPException(status_code=400, detail="机构ID不匹配")
        if wh.day_of_week in days_set:
            raise HTTPException(status_code=400, detail=f"星期{wh.day_of_week + 1}重复配置")
        days_set.add(wh.day_of_week)
        validate_time_slots(wh.time_slots)
    
    # 删除旧的作息时间记录
    db.query(AgencyWorkingHours).filter(
        AgencyWorkingHours.agency_id == agency_id
    ).delete()
    
    # 创建新的作息时间记录
    new_records = []
    for wh in batch_update.working_hours:
        db_wh = AgencyWorkingHours(
            agency_id=wh.agency_id,
            day_of_week=wh.day_of_week,
            time_slots=[{"start": ts.start, "end": ts.end} for ts in wh.time_slots]
        )
        db.add(db_wh)
        new_records.append(db_wh)
    
    db.commit()
    
    # 刷新并返回
    for record in new_records:
        db.refresh(record)
    
    return new_records


@router.put("/{day_of_week}", response_model=AgencyWorkingHoursResponse)
def update_single_day_working_hours(
    agency_id: int,
    day_of_week: int,
    update_data: AgencyWorkingHoursUpdate,
    db: Session = Depends(get_db)
):
    """更新单天的作息时间"""
    if not (0 <= day_of_week <= 6):
        raise HTTPException(status_code=400, detail="day_of_week必须在0-6之间")
    
    # 检查机构是否存在
    agency = db.query(CollectionAgency).filter(CollectionAgency.id == agency_id).first()
    if not agency:
        raise HTTPException(status_code=404, detail="机构不存在")
    
    # 验证时间段
    validate_time_slots(update_data.time_slots)
    
    # 查找或创建该天的记录
    working_hours = db.query(AgencyWorkingHours).filter(
        AgencyWorkingHours.agency_id == agency_id,
        AgencyWorkingHours.day_of_week == day_of_week
    ).first()
    
    if working_hours:
        # 更新现有记录
        working_hours.time_slots = [{"start": ts.start, "end": ts.end} for ts in update_data.time_slots]
    else:
        # 创建新记录
        working_hours = AgencyWorkingHours(
            agency_id=agency_id,
            day_of_week=day_of_week,
            time_slots=[{"start": ts.start, "end": ts.end} for ts in update_data.time_slots]
        )
        db.add(working_hours)
    
    db.commit()
    db.refresh(working_hours)
    
    return working_hours

