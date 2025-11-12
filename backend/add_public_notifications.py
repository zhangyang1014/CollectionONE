"""
添加公共通知mock数据
"""
import sys
import os
from datetime import datetime
import json

# 添加项目根目录到路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from app.core.config import settings
from app.models.public_notification import PublicNotification

def main():
    """添加公共通知数据"""
    print("="*60)
    print("添加公共通知mock数据")
    print("="*60)
    
    # 创建数据库引擎
    engine = create_engine(settings.DATABASE_URL)
    SessionLocal = sessionmaker(bind=engine)
    session = SessionLocal()
    
    try:
        # 检查是否已存在
        existing1 = session.query(PublicNotification).filter(
            PublicNotification.title == "元旦放假通知"
        ).first()
        
        existing2 = session.query(PublicNotification).filter(
            PublicNotification.title == "禁止爆催通知"
        ).first()
        
        if existing1:
            print("⚠️  元旦放假通知已存在，跳过")
        else:
            # 元旦放假通知
            notification1 = PublicNotification(
                tenant_id=None,  # 全局通知
                agency_id=None,
                title="元旦放假通知",
                h5_content="<html><body><h1>元旦放假通知</h1><p>各位同事：</p><p>根据国家法定节假日安排，元旦假期为2025年1月1日（星期三），共1天。</p><p>请各部门提前安排好工作，确保假期期间业务正常运行。</p><p>祝大家元旦快乐！</p></body></html>",
                h5_content_type="html",
                carousel_interval_seconds=30,
                is_forced_read=True,
                is_enabled=True,
                effective_start_time=datetime(2024, 12, 25, 0, 0, 0),
                effective_end_time=datetime(2025, 1, 5, 23, 59, 59),
                notify_roles=json.dumps(['collector', 'team_leader', 'agency_admin', 'tenant_admin']),
                sort_order=1
            )
            session.add(notification1)
            session.flush()  # flush后id会被自动生成
            print("✅ 已添加：元旦放假通知")
        
        if existing2:
            print("⚠️  禁止爆催通知已存在，跳过")
        else:
            # 禁止爆催通知
            notification2 = PublicNotification(
                tenant_id=None,  # 全局通知
                agency_id=None,
                title="禁止爆催通知",
                h5_content="<html><body><h1>禁止爆催通知</h1><p>重要提醒：</p><p>根据相关法律法规和公司规定，严禁对客户进行爆催行为，包括但不限于：</p><ul><li>频繁骚扰客户</li><li>使用威胁、恐吓等不当手段</li><li>在非工作时间联系客户</li><li>泄露客户隐私信息</li></ul><p>请所有催员严格遵守催收规范，文明催收，保护客户合法权益。</p><p>如有违规行为，将严肃处理。</p></body></html>",
                h5_content_type="html",
                carousel_interval_seconds=45,
                is_forced_read=True,
                is_enabled=True,
                effective_start_time=None,  # 永久有效
                effective_end_time=None,
                notify_roles=json.dumps(['collector', 'team_leader', 'agency_admin']),
                sort_order=2
            )
            session.add(notification2)
            session.flush()  # flush后id会被自动生成
            print("✅ 已添加：禁止爆催通知")
        
        session.commit()
        
        # 显示所有公共通知
        all_notifications = session.query(PublicNotification).all()
        print(f"\n当前公共通知总数: {len(all_notifications)}")
        for n in all_notifications:
            print(f"  - {n.title} (ID: {n.id}, 启用: {n.is_enabled})")
        
        print("\n" + "="*60)
        print("✅ 完成！")
        print("="*60)
        
    except Exception as e:
        print(f"\n❌ 添加失败: {e}")
        import traceback
        traceback.print_exc()
        session.rollback()
    finally:
        session.close()


if __name__ == "__main__":
    main()

