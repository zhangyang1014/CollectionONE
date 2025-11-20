"""
空闲催员监控 - 数据计算API

提供API接口来触发空闲数据计算
"""
from fastapi import APIRouter, Depends, HTTPException, Query, BackgroundTasks
from sqlalchemy.orm import Session
from sqlalchemy import and_, func
from typing import List, Dict, Optional
from datetime import datetime, date, timedelta, time
from decimal import Decimal

from app.core.database import get_db
from app.models.idle_monitor_config import IdleMonitorConfig
from app.models.collector_idle_record import CollectorIdleRecord, CollectorIdleStats
from app.models.communication_record import CommunicationRecord
from app.models.collector import Collector
from app.models.case import Case
from app.models.case_contact import CaseContact
from app.models.communication_record import ChannelEnum, DirectionEnum, ContactResultEnum
from random import randint, choice, random as rand_random

router = APIRouter()


class IdleCalculator:
    """空闲数据计算器"""
    
    def __init__(self, db: Session, tenant_id: int, calc_date: date):
        self.db = db
        self.tenant_id = tenant_id
        self.calc_date = calc_date
        self.config = self._load_config()
        self.log = []
        
    def add_log(self, message: str):
        """添加日志"""
        self.log.append(f"[{datetime.now().strftime('%H:%M:%S')}] {message}")
    
    def _load_config(self) -> Dict:
        """加载空闲监控配置"""
        config = self.db.query(IdleMonitorConfig).filter(
            and_(
                IdleMonitorConfig.tenant_id == self.tenant_id,
                IdleMonitorConfig.is_active == True
            )
        ).first()
        
        if not config:
            # 使用默认配置
            self.add_log("⚠️ 未找到配置，使用默认配置")
            return {
                'work_time_slots': [
                    {'start': '09:00', 'end': '12:00'},
                    {'start': '14:00', 'end': '18:00'}
                ],
                'idle_threshold_minutes': 30,
                'monitored_actions': ['call', 'whatsapp', 'rcs', 'sms', 'email'],
                'exclude_holidays': True,
                'config_id': 0
            }
        
        return {
            'work_time_slots': config.work_time_slots,
            'idle_threshold_minutes': config.idle_threshold_minutes,
            'monitored_actions': config.monitored_actions,
            'exclude_holidays': config.exclude_holidays,
            'config_id': config.id
        }
    
    def _get_collectors(self) -> List[Collector]:
        """获取需要计算的催员列表"""
        collectors = self.db.query(Collector).filter(
            and_(
                Collector.tenant_id == self.tenant_id,
                Collector.is_active == True
            )
        ).all()
        
        self.add_log(f"📊 找到 {len(collectors)} 个活跃催员")
        return collectors
    
    def _get_collector_actions(self, collector_id: int) -> List[Dict]:
        """获取催员的所有行为记录"""
        actions = []
        
        # 从通信记录获取
        communications = self.db.query(CommunicationRecord).filter(
            and_(
                CommunicationRecord.collector_id == collector_id,
                func.date(CommunicationRecord.contacted_at) == self.calc_date
            )
        ).order_by(CommunicationRecord.contacted_at).all()
        
        for comm in communications:
            action_type = self._map_channel_to_action(comm.channel.value)
            if action_type in self.config['monitored_actions']:
                actions.append({
                    'type': action_type,
                    'timestamp': comm.contacted_at,
                    'details': f"{action_type} - {comm.channel.value}"
                })
        
        return sorted(actions, key=lambda x: x['timestamp'])
    
    def _map_channel_to_action(self, channel: str) -> str:
        """将通信渠道映射到行为类型"""
        mapping = {
            'phone': 'call',
            'whatsapp': 'whatsapp',
            'sms': 'sms',
            'rcs': 'rcs',
            'email': 'email'
        }
        return mapping.get(channel, channel)
    
    def _is_work_time(self, dt: datetime) -> bool:
        """判断时间是否在上班时间内"""
        current_time = dt.time()
        
        for slot in self.config['work_time_slots']:
            start_time = time.fromisoformat(slot['start'])
            end_time = time.fromisoformat(slot['end'])
            
            if start_time <= current_time <= end_time:
                return True
        
        return False
    
    def detect_idle_periods(self, collector_id: int, actions: List[Dict]) -> List[Dict]:
        """检测空闲时段"""
        idle_periods = []
        threshold = self.config['idle_threshold_minutes']
        
        if len(actions) < 2:
            return idle_periods
        
        for i in range(len(actions) - 1):
            current = actions[i]
            next_action = actions[i + 1]
            
            gap = (next_action['timestamp'] - current['timestamp']).total_seconds() / 60
            
            if gap >= threshold:
                if self._is_work_time(current['timestamp']) and self._is_work_time(next_action['timestamp']):
                    idle_periods.append({
                        'start_time': current['timestamp'],
                        'end_time': next_action['timestamp'],
                        'duration_minutes': int(gap),
                        'before_action': {
                            'type': current['type'],
                            'time': current['timestamp'].isoformat(),
                            'details': current.get('details', '')
                        },
                        'after_action': {
                            'type': next_action['type'],
                            'time': next_action['timestamp'].isoformat(),
                            'details': next_action.get('details', '')
                        }
                    })
        
        return idle_periods
    
    def _get_case_statistics(self, collector_id: int) -> Dict:
        """获取催员的案件管理情况"""
        cases = self.db.query(Case).filter(
            Case.collector_id == collector_id
        ).all()
        
        return {
            'total': len(cases),
            'collected': 0,
            'total_amount': Decimal('0'),
            'collected_amount': Decimal('0')
        }
    
    def save_idle_records(self, collector_id: int, idle_periods: List[Dict]):
        """保存空闲记录"""
        collector = self.db.query(Collector).filter(Collector.id == collector_id).first()
        if not collector:
            return
        
        self.db.query(CollectorIdleRecord).filter(
            and_(
                CollectorIdleRecord.collector_id == collector_id,
                CollectorIdleRecord.idle_date == self.calc_date
            )
        ).delete()
        
        for period in idle_periods:
            record = CollectorIdleRecord(
                tenant_id=self.tenant_id,
                collector_id=collector_id,
                agency_id=collector.agency_id,
                team_id=collector.team_id,
                idle_date=self.calc_date,
                idle_start_time=period['start_time'],
                idle_end_time=period['end_time'],
                idle_duration_minutes=period['duration_minutes'],
                before_action=period['before_action'],
                after_action=period['after_action'],
                config_id=self.config['config_id']
            )
            self.db.add(record)
    
    def save_idle_statistics(self, collector_id: int, idle_periods: List[Dict]):
        """保存空闲统计数据"""
        collector = self.db.query(Collector).filter(Collector.id == collector_id).first()
        if not collector:
            return
        
        self.db.query(CollectorIdleStats).filter(
            and_(
                CollectorIdleStats.collector_id == collector_id,
                CollectorIdleStats.stat_date == self.calc_date
            )
        ).delete()
        
        idle_count = len(idle_periods)
        total_idle_minutes = sum(p['duration_minutes'] for p in idle_periods)
        longest_idle = max([p['duration_minutes'] for p in idle_periods]) if idle_periods else 0
        avg_idle = total_idle_minutes / idle_count if idle_count > 0 else 0
        
        work_minutes = 0
        for slot in self.config['work_time_slots']:
            start = time.fromisoformat(slot['start'])
            end = time.fromisoformat(slot['end'])
            start_minutes = start.hour * 60 + start.minute
            end_minutes = end.hour * 60 + end.minute
            work_minutes += (end_minutes - start_minutes)
        
        idle_rate = total_idle_minutes / work_minutes if work_minutes > 0 else 0
        case_stats = self._get_case_statistics(collector_id)
        
        stats = CollectorIdleStats(
            tenant_id=self.tenant_id,
            collector_id=collector_id,
            agency_id=collector.agency_id,
            team_id=collector.team_id,
            stat_date=self.calc_date,
            idle_count=idle_count,
            total_idle_minutes=total_idle_minutes,
            longest_idle_minutes=longest_idle,
            avg_idle_minutes=Decimal(str(avg_idle)),
            work_minutes=work_minutes,
            idle_rate=Decimal(str(idle_rate)),
            managed_cases_total=case_stats['total'],
            managed_cases_collected=case_stats['collected'],
            managed_amount_total=case_stats['total_amount'],
            managed_amount_collected=case_stats['collected_amount']
        )
        self.db.add(stats)
    
    def calculate_for_collector(self, collector: Collector):
        """计算单个催员的空闲数据"""
        actions = self._get_collector_actions(collector.id)
        idle_periods = self.detect_idle_periods(collector.id, actions)
        
        if idle_periods:
            self.add_log(f"   催员 {collector.collector_name}: 发现 {len(idle_periods)} 个空闲时段")
            self.save_idle_records(collector.id, idle_periods)
            self.save_idle_statistics(collector.id, idle_periods)
        else:
            self.save_idle_statistics(collector.id, [])
    
    def run(self) -> Dict:
        """执行计算"""
        self.add_log(f"🚀 开始计算空闲数据 - 甲方ID: {self.tenant_id}, 日期: {self.calc_date}")
        
        collectors = self._get_collectors()
        
        if not collectors:
            self.add_log("❌ 没有找到活跃催员")
            return {'success': False, 'message': '没有找到活跃催员', 'log': self.log}
        
        success_count = 0
        for collector in collectors:
            try:
                self.calculate_for_collector(collector)
                success_count += 1
            except Exception as e:
                self.add_log(f"   ❌ 催员 {collector.id} 计算失败: {str(e)}")
        
        try:
            self.db.commit()
            self.add_log(f"✅ 计算完成！成功: {success_count}/{len(collectors)} 个催员")
            return {
                'success': True,
                'message': f'计算完成，成功 {success_count}/{len(collectors)} 个催员',
                'log': self.log,
                'stats': {
                    'total_collectors': len(collectors),
                    'success_count': success_count,
                    'fail_count': len(collectors) - success_count
                }
            }
        except Exception as e:
            self.db.rollback()
            self.add_log(f"❌ 保存失败: {str(e)}")
            return {'success': False, 'message': f'保存失败: {str(e)}', 'log': self.log}


@router.post("/calculate")
def calculate_idle_data(
    tenant_id: int = Query(..., description="甲方ID"),
    calc_date: Optional[str] = Query(None, description="计算日期 YYYY-MM-DD，默认为昨天"),
    db: Session = Depends(get_db)
):
    """
    计算空闲数据
    
    立即执行计算并返回结果
    """
    # 解析日期
    if calc_date:
        try:
            target_date = datetime.strptime(calc_date, '%Y-%m-%d').date()
        except ValueError:
            raise HTTPException(status_code=400, detail="日期格式错误，应为 YYYY-MM-DD")
    else:
        target_date = date.today() - timedelta(days=1)
    
    # 执行计算
    calculator = IdleCalculator(db, tenant_id, target_date)
    result = calculator.run()
    
    return result


@router.post("/calculate-async")
def calculate_idle_data_async(
    background_tasks: BackgroundTasks,
    tenant_id: int = Query(..., description="甲方ID"),
    calc_date: Optional[str] = Query(None, description="计算日期 YYYY-MM-DD，默认为昨天"),
):
    """
    异步计算空闲数据
    
    将计算任务放入后台队列，立即返回
    """
    # 解析日期
    if calc_date:
        try:
            target_date = datetime.strptime(calc_date, '%Y-%m-%d').date()
        except ValueError:
            raise HTTPException(status_code=400, detail="日期格式错误，应为 YYYY-MM-DD")
    else:
        target_date = date.today() - timedelta(days=1)
    
    # 添加到后台任务
    def calc_task():
        db = next(get_db())
        try:
            calculator = IdleCalculator(db, tenant_id, target_date)
            calculator.run()
        finally:
            db.close()
    
    background_tasks.add_task(calc_task)
    
    return {
        'success': True,
        'message': '计算任务已提交到后台队列',
        'tenant_id': tenant_id,
        'calc_date': target_date.isoformat()
    }


@router.post("/generate-test-data")
def generate_test_data(
    tenant_id: int = Query(..., description="甲方ID"),
    test_date: Optional[str] = Query(None, description="测试日期 YYYY-MM-DD，默认为今天"),
    collector_count: int = Query(5, description="催员数量"),
    db: Session = Depends(get_db)
):
    """
    生成测试数据
    
    为指定日期生成模拟的通信记录
    """
    # 解析日期
    if test_date:
        try:
            target_date = datetime.strptime(test_date, '%Y-%m-%d').date()
        except ValueError:
            raise HTTPException(status_code=400, detail="日期格式错误，应为 YYYY-MM-DD")
    else:
        target_date = date.today()
    
    # 获取催员
    collectors = db.query(Collector).filter(
        and_(
            Collector.tenant_id == tenant_id,
            Collector.is_active == True
        )
    ).limit(collector_count).all()
    
    if not collectors:
        raise HTTPException(status_code=404, detail="没有找到催员")
    
    generated_count = 0
    log = []
    
    for collector in collectors:
        # 获取案件
        cases = db.query(Case).filter(
            Case.collector_id == collector.id
        ).limit(5).all()
        
        if not cases:
            log.append(f"⚠️ 催员 {collector.collector_name} 没有案件，跳过")
            continue
        
        test_case = choice(cases)
        
        # 获取联系人
        contacts = db.query(CaseContact).filter(
            CaseContact.case_id == test_case.id
        ).limit(3).all()
        
        if not contacts:
            log.append(f"⚠️ 案件 {test_case.id} 没有联系人，跳过")
            continue
        
        # 生成行为记录
        actions = []
        
        # 上午
        current_time = datetime.combine(target_date, datetime.strptime('09:00', '%H:%M').time())
        end_morning = datetime.combine(target_date, datetime.strptime('12:00', '%H:%M').time())
        
        while current_time < end_morning:
            if rand_random() < 0.8:
                channel = choice([ChannelEnum.PHONE, ChannelEnum.WHATSAPP, ChannelEnum.SMS])
                record = CommunicationRecord(
                    case_id=test_case.id,
                    collector_id=collector.id,
                    contact_person_id=choice(contacts).id,
                    channel=channel,
                    direction=DirectionEnum.OUTBOUND,
                    contact_result=choice([ContactResultEnum.CONNECTED, ContactResultEnum.NOT_CONNECTED]),
                    contacted_at=current_time,
                    call_duration=randint(30, 300) if channel == ChannelEnum.PHONE else None
                )
                actions.append(record)
            current_time += timedelta(minutes=randint(10, 20))
        
        # 下午（包含空闲）
        current_time = datetime.combine(target_date, datetime.strptime('14:00', '%H:%M').time())
        end_afternoon = datetime.combine(target_date, datetime.strptime('18:00', '%H:%M').time())
        
        # 随机空闲
        idle_start = None
        idle_duration = 0
        if rand_random() < 0.6:
            idle_start_minutes = randint(30, 120)
            idle_start = current_time + timedelta(minutes=idle_start_minutes)
            idle_duration = randint(30, 60)
        
        while current_time < end_afternoon:
            if idle_start and idle_start <= current_time < (idle_start + timedelta(minutes=idle_duration)):
                current_time += timedelta(minutes=5)
                continue
            
            if rand_random() < 0.8:
                channel = choice([ChannelEnum.PHONE, ChannelEnum.WHATSAPP, ChannelEnum.SMS])
                record = CommunicationRecord(
                    case_id=test_case.id,
                    collector_id=collector.id,
                    contact_person_id=choice(contacts).id,
                    channel=channel,
                    direction=DirectionEnum.OUTBOUND,
                    contact_result=choice([ContactResultEnum.CONNECTED, ContactResultEnum.NOT_CONNECTED]),
                    contacted_at=current_time,
                    call_duration=randint(30, 300) if channel == ChannelEnum.PHONE else None
                )
                actions.append(record)
            current_time += timedelta(minutes=randint(8, 15))
        
        # 删除旧数据
        db.query(CommunicationRecord).filter(
            and_(
                CommunicationRecord.collector_id == collector.id,
                func.date(CommunicationRecord.contacted_at) == target_date
            )
        ).delete()
        
        # 保存新数据
        for action in actions:
            db.add(action)
        
        generated_count += 1
        log.append(f"✅ 催员 {collector.collector_name}: 生成 {len(actions)} 条记录")
    
    try:
        db.commit()
        return {
            'success': True,
            'message': f'成功为 {generated_count}/{len(collectors)} 个催员生成测试数据',
            'log': log,
            'stats': {
                'total_collectors': len(collectors),
                'generated_count': generated_count
            }
        }
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"保存失败: {str(e)}")

