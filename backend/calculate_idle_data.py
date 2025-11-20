"""
空闲催员监控 - 数据计算引擎

功能：
1. 从通信记录等数据源采集行为记录
2. 识别空闲时段
3. 计算统计数据
4. 写入数据库

使用方法：
python calculate_idle_data.py --date 2025-11-20 --tenant-id 1
"""
import sys
import os
from datetime import datetime, date, timedelta, time
from typing import List, Dict, Optional
from decimal import Decimal
import argparse

# 添加项目根目录到Python路径
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from sqlalchemy.orm import Session
from sqlalchemy import and_, or_, func
from app.core.database import get_db, engine
from app.models.idle_monitor_config import IdleMonitorConfig
from app.models.collector_idle_record import CollectorIdleRecord, CollectorIdleStats
from app.models.communication_record import CommunicationRecord
from app.models.collector import Collector
from app.models.case import Case


class IdleCalculator:
    """空闲数据计算器"""
    
    def __init__(self, db: Session, tenant_id: int, calc_date: date):
        self.db = db
        self.tenant_id = tenant_id
        self.calc_date = calc_date
        self.config = self._load_config()
        
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
            print("⚠️ 未找到配置，使用默认配置")
            return {
                'work_time_slots': [
                    {'start': '09:00', 'end': '12:00'},
                    {'start': '14:00', 'end': '18:00'}
                ],
                'idle_threshold_minutes': 30,
                'monitored_actions': ['call', 'whatsapp', 'rcs', 'sms', 'email', 'case_update', 'login'],
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
        
        print(f"📊 找到 {len(collectors)} 个活跃催员")
        return collectors
    
    def _get_collector_actions(self, collector_id: int) -> List[Dict]:
        """获取催员的所有行为记录"""
        actions = []
        
        # 1. 从通信记录获取
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
        
        # 2. 可以添加其他数据源（案件操作、登录记录等）
        # TODO: 如果有案件操作日志表，从中获取
        # TODO: 如果有登录日志表，从中获取
        
        print(f"   催员 {collector_id}: 找到 {len(actions)} 条行为记录")
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
        """
        检测空闲时段
        
        算法：
        1. 遍历相邻的两个行为
        2. 计算时间间隔
        3. 如果间隔 >= 阈值，且在上班时间内，则记录为空闲
        """
        idle_periods = []
        threshold = self.config['idle_threshold_minutes']
        
        if len(actions) < 2:
            # 如果行为记录太少，可能整天都在空闲
            # 这里简化处理，不记录
            return idle_periods
        
        for i in range(len(actions) - 1):
            current = actions[i]
            next_action = actions[i + 1]
            
            # 计算间隔（分钟）
            gap = (next_action['timestamp'] - current['timestamp']).total_seconds() / 60
            
            if gap >= threshold:
                # 检查是否在上班时间内
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
        # 获取催员负责的案件
        cases = self.db.query(Case).filter(
            Case.collector_id == collector_id
        ).all()
        
        total_cases = len(cases)
        collected_cases = 0
        total_amount = Decimal('0')
        collected_amount = Decimal('0')
        
        # 简化统计（实际应该根据还款记录表）
        # 这里假设案件有相关字段，根据实际情况调整
        
        return {
            'total': total_cases,
            'collected': collected_cases,
            'total_amount': total_amount,
            'collected_amount': collected_amount
        }
    
    def save_idle_records(self, collector_id: int, idle_periods: List[Dict]):
        """保存空闲记录"""
        collector = self.db.query(Collector).filter(Collector.id == collector_id).first()
        if not collector:
            return
        
        # 删除当天已有的记录（重新计算）
        self.db.query(CollectorIdleRecord).filter(
            and_(
                CollectorIdleRecord.collector_id == collector_id,
                CollectorIdleRecord.idle_date == self.calc_date
            )
        ).delete()
        
        # 插入新记录
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
        
        # 删除当天已有的统计（重新计算）
        self.db.query(CollectorIdleStats).filter(
            and_(
                CollectorIdleStats.collector_id == collector_id,
                CollectorIdleStats.stat_date == self.calc_date
            )
        ).delete()
        
        # 计算统计数据
        idle_count = len(idle_periods)
        total_idle_minutes = sum(p['duration_minutes'] for p in idle_periods)
        longest_idle = max([p['duration_minutes'] for p in idle_periods]) if idle_periods else 0
        avg_idle = total_idle_minutes / idle_count if idle_count > 0 else 0
        
        # 计算工作时长（所有上班时间段的总和）
        work_minutes = 0
        for slot in self.config['work_time_slots']:
            start = time.fromisoformat(slot['start'])
            end = time.fromisoformat(slot['end'])
            # 计算时间段的分钟数
            start_minutes = start.hour * 60 + start.minute
            end_minutes = end.hour * 60 + end.minute
            work_minutes += (end_minutes - start_minutes)
        
        idle_rate = total_idle_minutes / work_minutes if work_minutes > 0 else 0
        
        # 获取案件统计
        case_stats = self._get_case_statistics(collector_id)
        
        # 创建统计记录
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
        print(f"   计算催员: {collector.collector_name} (ID: {collector.id})")
        
        # 1. 获取行为记录
        actions = self._get_collector_actions(collector.id)
        
        # 2. 识别空闲时段
        idle_periods = self.detect_idle_periods(collector.id, actions)
        
        if idle_periods:
            print(f"   ⚠️  发现 {len(idle_periods)} 个空闲时段")
            
            # 3. 保存空闲记录
            self.save_idle_records(collector.id, idle_periods)
            
            # 4. 保存统计数据
            self.save_idle_statistics(collector.id, idle_periods)
        else:
            print(f"   ✅ 无空闲时段")
            # 即使没有空闲，也保存统计（全为0）
            self.save_idle_statistics(collector.id, [])
    
    def run(self):
        """执行计算"""
        print(f"\n{'='*60}")
        print(f"🚀 开始计算空闲数据")
        print(f"{'='*60}")
        print(f"甲方ID: {self.tenant_id}")
        print(f"计算日期: {self.calc_date}")
        print(f"空闲阈值: {self.config['idle_threshold_minutes']} 分钟")
        print(f"上班时间: {self.config['work_time_slots']}")
        print(f"监控行为: {self.config['monitored_actions']}")
        print(f"{'='*60}\n")
        
        # 获取催员列表
        collectors = self._get_collectors()
        
        if not collectors:
            print("❌ 没有找到活跃催员")
            return
        
        # 计算每个催员的空闲数据
        success_count = 0
        for collector in collectors:
            try:
                self.calculate_for_collector(collector)
                success_count += 1
            except Exception as e:
                print(f"   ❌ 错误: {str(e)}")
                import traceback
                traceback.print_exc()
        
        # 提交事务
        try:
            self.db.commit()
            print(f"\n{'='*60}")
            print(f"✅ 计算完成！")
            print(f"{'='*60}")
            print(f"成功: {success_count}/{len(collectors)} 个催员")
            print(f"{'='*60}\n")
        except Exception as e:
            self.db.rollback()
            print(f"\n❌ 保存失败: {str(e)}")
            raise


def main():
    """主函数"""
    parser = argparse.ArgumentParser(description='空闲催员监控数据计算')
    parser.add_argument('--date', type=str, help='计算日期 (YYYY-MM-DD)，默认为昨天')
    parser.add_argument('--tenant-id', type=int, required=True, help='甲方ID')
    parser.add_argument('--days', type=int, default=1, help='计算最近N天的数据，默认1天')
    
    args = parser.parse_args()
    
    # 确定计算日期
    if args.date:
        calc_date = datetime.strptime(args.date, '%Y-%m-%d').date()
    else:
        # 默认计算昨天
        calc_date = date.today() - timedelta(days=1)
    
    # 获取数据库会话
    db = next(get_db())
    
    try:
        if args.days > 1:
            # 计算多天
            for i in range(args.days):
                current_date = calc_date - timedelta(days=i)
                print(f"\n📅 计算日期: {current_date}")
                
                calculator = IdleCalculator(db, args.tenant_id, current_date)
                calculator.run()
        else:
            # 计算单天
            calculator = IdleCalculator(db, args.tenant_id, calc_date)
            calculator.run()
            
    except Exception as e:
        print(f"\n❌ 计算失败: {str(e)}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
    finally:
        db.close()


if __name__ == '__main__':
    main()

