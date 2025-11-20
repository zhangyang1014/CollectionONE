"""创建小组群表和更新相关表结构"""
import sys
from sqlalchemy import create_engine, text
from app.core.config import settings
from app.core.database import Base
from app.models import TeamGroup, CollectionTeam

def create_team_groups_table():
    """创建小组群表并更新小组表结构"""
    engine = create_engine(settings.DATABASE_URL)
    
    print("正在创建小组群相关表结构...")
    
    try:
        with engine.connect() as conn:
            # 创建小组群表
            print("\n1. 创建小组群表 (team_groups)...")
            Base.metadata.tables['team_groups'].create(engine, checkfirst=True)
            print("✓ 小组群表创建成功")
            
            # 检查collection_teams表是否已有team_group_id字段
            print("\n2. 检查小组表 (collection_teams) 是否需要添加team_group_id字段...")
            result = conn.execute(text("PRAGMA table_info(collection_teams)"))
            columns = [row[1] for row in result.fetchall()]
            
            if 'team_group_id' not in columns:
                print("   添加team_group_id字段到collection_teams表...")
                conn.execute(text("""
                    ALTER TABLE collection_teams 
                    ADD COLUMN team_group_id INTEGER REFERENCES team_groups(id)
                """))
                conn.commit()
                print("✓ team_group_id字段添加成功")
            else:
                print("✓ team_group_id字段已存在")
            
            print("\n✓ 所有表结构更新完成！")
            print("\n说明:")
            print("- 小组群表 (team_groups) 已创建")
            print("- 小组表 (collection_teams) 已添加 team_group_id 字段")
            print("- SPV角色说明：SPV是小组群长，可以通过在创建小组群时指定spv_id来设置")
            print("- SPV可以是任何Collector，建议在催员管理中将其collector_level设置为'SPV'以便区分")
            
    except Exception as e:
        print(f"\n❌ 错误: {e}")
        import traceback
        traceback.print_exc()
        return False
    
    return True

if __name__ == "__main__":
    success = create_team_groups_table()
    sys.exit(0 if success else 1)

