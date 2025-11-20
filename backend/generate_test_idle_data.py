"""
生成空闲监控测试数据

功能：
1. 生成模拟的通信记录（包含一些空闲时段）
2. 为指定日期生成数据
3. 可以控制空闲时段的数量和时长

使用方法：
python generate_test_idle_data.py --tenant-id 1 --date 2025-11-20 --collectors 5
"""
import sys
import os
from datetime import datetime, date, timedelta
from random import randint, choice, random
import argparse

# 添加项目根目录到Python路径
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from sqlalchemy.orm import Session
from sqlalchemy import and_, func
from app.core.database import get_db
from app.models.communication_record import CommunicationRecord, ChannelEnum, DirectionEnum, ContactResultEnum
from app.models.collector import Collector
from app.models.case import Case
from app.models.case_contact import CaseContact


class TestDataGenerator:
    """测试数据生成器"""
    
    def __init__(self, db: Session, tenant_id: int, test_date: date):
        self.db = db
        self.tenant_id = tenant_id
        self.test_date = test_date
        
    def get_or_create_collectors(self, count: int) -> list:
        """获取或创建测试催员"""
        collectors = self.db.query(Collector).filter(
            and_(
                Collector.tenant_id == self.tenant_id,
                Collector.is_active == True
            )
        ).limit(count).all()
        
        print(f"📊 找到 {len(collectors)} 个催员用于测试")
        return collectors
    
    def get_collector_cases(self, collector_id: int) -> list:
        """获取催员的案件"""
        cases = self.db.query(Case).filter(
            Case.collector_id == collector_id
        ).limit(10).all()
        
        return cases if cases else []
    
    def generate_work_day_actions(self, collector_id: int) -> list:
        """
        生成一天的工作行为记录
        
        策略：
        1. 正常工作时段：密集的行为记录
        2. 空闲时段：故意留出30-60分钟的间隔
        3. 午休时段：没有记录（不在上班时间，不算空闲）
        """
        actions = []
        
        # 获取案件（用于生成联系记录）
        cases = self.get_collector_cases(collector_id)
        if not cases:
            print(f"   ⚠️  催员 {collector_id} 没有案件，跳过")
            return []
        
        # 随机选择一个案件
        test_case = choice(cases)
        
        # 获取案件的联系人
        contacts = self.db.query(CaseContact).filter(
            CaseContact.case_id == test_case.id
        ).limit(3).all()
        
        if not contacts:
            print(f"   ⚠️  案件 {test_case.id} 没有联系人，跳过")
            return []
        
        # 上午时段：09:00 - 12:00
        current_time = datetime.combine(self.test_date, datetime.strptime('09:00', '%H:%M').time())
        end_morning = datetime.combine(self.test_date, datetime.strptime('12:00', '%H:%M').time())
        
        # 上午工作：正常频率（每10-20分钟一次行为）
        while current_time < end_morning:
            if random() < 0.8:  # 80%概率产生行为
                actions.append(self._create_action(current_time, test_case.id, choice(contacts).id))
            current_time += timedelta(minutes=randint(10, 20))
        
        # 午休：12:00 - 14:00（不生成记录）
        
        # 下午时段：14:00 - 18:00
        current_time = datetime.combine(self.test_date, datetime.strptime('14:00', '%H:%M').time())
        end_afternoon = datetime.combine(self.test_date, datetime.strptime('18:00', '%H:%M').time())
        
        # 下午工作：包含空闲时段
        # 策略：随机在某个时间点后停止30-60分钟（制造空闲）
        idle_start = None
        idle_duration = 0
        
        if random() < 0.6:  # 60%概率产生空闲
            # 在14:30-16:00之间随机选择一个空闲开始时间
            idle_start_minutes = randint(30, 120)
            idle_start = current_time + timedelta(minutes=idle_start_minutes)
            idle_duration = randint(30, 60)  # 空闲30-60分钟
        
        while current_time < end_afternoon:
            # 检查是否在空闲时段
            if idle_start and idle_start <= current_time < (idle_start + timedelta(minutes=idle_duration)):
                # 在空闲时段，不生成行为
                current_time += timedelta(minutes=5)
                continue
            
            if random() < 0.8:  # 80%概率产生行为
                actions.append(self._create_action(current_time, test_case.id, choice(contacts).id))
            current_time += timedelta(minutes=randint(8, 15))
        
        return actions
    
    def _create_action(self, timestamp: datetime, case_id: int, contact_id: int) -> dict:
        """创建一个行为记录"""
        # 随机选择渠道
        channel = choice([ChannelEnum.PHONE, ChannelEnum.WHATSAPP, ChannelEnum.SMS, ChannelEnum.RCS])
        
        return {
            'timestamp': timestamp,
            'channel': channel,
            'case_id': case_id,
            'contact_id': contact_id,
            'direction': DirectionEnum.OUTBOUND,
            'result': choice([ContactResultEnum.CONNECTED, ContactResultEnum.NOT_CONNECTED, ContactResultEnum.NO_ANSWER])
        }
    
    def save_actions(self, collector_id: int, actions: list):
        """保存行为记录到数据库"""
        if not actions:
            return
        
        # 删除该催员当天已有的测试数据
        self.db.query(CommunicationRecord).filter(
            and_(
                CommunicationRecord.collector_id == collector_id,
                func.date(CommunicationRecord.contacted_at) == self.test_date
            )
        ).delete()
        
        # 插入新数据
        for action in actions:
            record = CommunicationRecord(
                case_id=action['case_id'],
                collector_id=collector_id,
                contact_person_id=action['contact_id'],
                channel=action['channel'],
                direction=action['direction'],
                contact_result=action['result'],
                contacted_at=action['timestamp'],
                # 电话专属字段
                call_duration=randint(30, 300) if action['channel'] == ChannelEnum.PHONE else None,
                is_connected=(action['result'] == ContactResultEnum.CONNECTED),
                # 消息专属字段
                is_replied=(random() < 0.3) if action['channel'] in [ChannelEnum.WHATSAPP, ChannelEnum.SMS, ChannelEnum.RCS] else None,
                message_content=f"测试消息 - {action['channel'].value}" if action['channel'] in [ChannelEnum.WHATSAPP, ChannelEnum.SMS, ChannelEnum.RCS] else None
            )
            self.db.add(record)
        
        print(f"   ✅ 生成 {len(actions)} 条通信记录")
    
    def generate_for_collector(self, collector: Collector):
        """为单个催员生成测试数据"""
        print(f"   生成催员数据: {collector.collector_name} (ID: {collector.id})")
        
        # 生成行为记录
        actions = self.generate_work_day_actions(collector.id)
        
        if actions:
            # 保存到数据库
            self.save_actions(collector.id, actions)
        else:
            print(f"   ⚠️  未生成数据（可能缺少案件或联系人）")
    
    def run(self, collector_count: int):
        """执行生成"""
        print(f"\n{'='*60}")
        print(f"🎲 开始生成测试数据")
        print(f"{'='*60}")
        print(f"甲方ID: {self.tenant_id}")
        print(f"测试日期: {self.test_date}")
        print(f"催员数量: {collector_count}")
        print(f"{'='*60}\n")
        
        # 获取催员
        collectors = self.get_or_create_collectors(collector_count)
        
        if not collectors:
            print("❌ 没有找到催员，请先创建催员数据")
            return
        
        # 为每个催员生成数据
        success_count = 0
        for collector in collectors:
            try:
                self.generate_for_collector(collector)
                success_count += 1
            except Exception as e:
                print(f"   ❌ 错误: {str(e)}")
                import traceback
                traceback.print_exc()
        
        # 提交事务
        try:
            self.db.commit()
            print(f"\n{'='*60}")
            print(f"✅ 生成完成！")
            print(f"{'='*60}")
            print(f"成功: {success_count}/{len(collectors)} 个催员")
            print(f"\n💡 下一步: 运行计算脚本")
            print(f"   python calculate_idle_data.py --tenant-id {self.tenant_id} --date {self.test_date}")
            print(f"{'='*60}\n")
        except Exception as e:
            self.db.rollback()
            print(f"\n❌ 保存失败: {str(e)}")
            raise


def main():
    """主函数"""
    parser = argparse.ArgumentParser(description='生成空闲监控测试数据')
    parser.add_argument('--tenant-id', type=int, required=True, help='甲方ID')
    parser.add_argument('--date', type=str, help='测试日期 (YYYY-MM-DD)，默认为今天')
    parser.add_argument('--collectors', type=int, default=5, help='催员数量，默认5个')
    
    args = parser.parse_args()
    
    # 确定测试日期
    if args.date:
        test_date = datetime.strptime(args.date, '%Y-%m-%d').date()
    else:
        test_date = date.today()
    
    # 获取数据库会话
    db = next(get_db())
    
    try:
        generator = TestDataGenerator(db, args.tenant_id, test_date)
        generator.run(args.collectors)
    except Exception as e:
        print(f"\n❌ 生成失败: {str(e)}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
    finally:
        db.close()


if __name__ == '__main__':
    main()

