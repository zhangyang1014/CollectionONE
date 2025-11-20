"""
更新公共通知表字段
添加非强制阅读时的配置字段
"""
import sys
from sqlalchemy import text
from app.core.database import engine


def update_public_notification_fields():
    """更新 public_notifications 表，添加新字段"""
    
    with engine.connect() as conn:
        try:
            # 1. 修改 h5_content 字段为可空
            print("1. 修改 h5_content 字段为可空...")
            conn.execute(text("""
                ALTER TABLE public_notifications 
                MODIFY COLUMN h5_content TEXT NULL COMMENT 'H5链接地址（可选）'
            """))
            conn.commit()
            print("   ✓ h5_content 字段已更新")
            
            # 2. 添加重复提醒间隔字段
            print("2. 添加 repeat_interval_minutes 字段...")
            try:
                conn.execute(text("""
                    ALTER TABLE public_notifications 
                    ADD COLUMN repeat_interval_minutes INT NULL COMMENT '重复提醒时间间隔（分钟）'
                    AFTER is_enabled
                """))
                conn.commit()
                print("   ✓ repeat_interval_minutes 字段已添加")
            except Exception as e:
                if "Duplicate column name" in str(e):
                    print("   - repeat_interval_minutes 字段已存在，跳过")
                else:
                    raise
            
            # 3. 添加最大提醒次数字段
            print("3. 添加 max_remind_count 字段...")
            try:
                conn.execute(text("""
                    ALTER TABLE public_notifications 
                    ADD COLUMN max_remind_count INT NULL COMMENT '最大提醒次数'
                    AFTER repeat_interval_minutes
                """))
                conn.commit()
                print("   ✓ max_remind_count 字段已添加")
            except Exception as e:
                if "Duplicate column name" in str(e):
                    print("   - max_remind_count 字段已存在，跳过")
                else:
                    raise
            
            # 4. 添加通知时间范围开始字段
            print("4. 添加 notify_time_start 字段...")
            try:
                conn.execute(text("""
                    ALTER TABLE public_notifications 
                    ADD COLUMN notify_time_start VARCHAR(5) NULL COMMENT '通知时间范围开始（HH:MM）'
                    AFTER max_remind_count
                """))
                conn.commit()
                print("   ✓ notify_time_start 字段已添加")
            except Exception as e:
                if "Duplicate column name" in str(e):
                    print("   - notify_time_start 字段已存在，跳过")
                else:
                    raise
            
            # 5. 添加通知时间范围结束字段
            print("5. 添加 notify_time_end 字段...")
            try:
                conn.execute(text("""
                    ALTER TABLE public_notifications 
                    ADD COLUMN notify_time_end VARCHAR(5) NULL COMMENT '通知时间范围结束（HH:MM）'
                    AFTER notify_time_start
                """))
                conn.commit()
                print("   ✓ notify_time_end 字段已添加")
            except Exception as e:
                if "Duplicate column name" in str(e):
                    print("   - notify_time_end 字段已存在，跳过")
                else:
                    raise
            
            print("\n✅ 所有字段更新完成！")
            
        except Exception as e:
            print(f"\n❌ 更新失败: {str(e)}")
            conn.rollback()
            sys.exit(1)


if __name__ == "__main__":
    print("=" * 60)
    print("开始更新 public_notifications 表字段")
    print("=" * 60)
    update_public_notification_fields()
    print("=" * 60)

