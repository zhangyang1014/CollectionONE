"""创建小组群和SPV管理员的Mock数据"""
import sys
import os
from datetime import datetime
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
# 添加项目路径到sys.path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from app.models import TeamGroup, TeamAdminAccount, CollectionTeam, CollectionAgency, CaseQueue, Tenant

def get_password_hash(password: str) -> str:
    """生成密码哈希 - 使用一个已知的bcrypt哈希值（password123）"""
    # 这是 "password123" 的bcrypt哈希值
    return "$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5xyMQhKfYQr3a"

def create_team_groups_mock_data():
    """创建小组群和SPV管理员的Mock数据"""
    # 直接使用SQLite数据库
    db_path = os.path.join(os.path.dirname(__file__), "cco_test.db")
    DATABASE_URL = f"sqlite:///{db_path}"
    
    engine = create_engine(DATABASE_URL)
    SessionLocal = sessionmaker(bind=engine)
    db = SessionLocal()
    
    try:
        print("=" * 60)
        print("开始创建小组群Mock数据...")
        print("=" * 60)
        
        # 1. 获取第一个甲方
        tenant = db.query(Tenant).first()
        if not tenant:
            print("❌ 错误: 未找到甲方数据")
            return False
        
        print(f"\n✓ 找到甲方: {tenant.tenant_name} (ID: {tenant.id})")
        
        # 2. 获取第一个机构
        agency = db.query(CollectionAgency).filter(
            CollectionAgency.tenant_id == tenant.id
        ).first()
        
        if not agency:
            print("❌ 错误: 未找到机构数据")
            return False
        
        print(f"✓ 找到机构: {agency.agency_name} (ID: {agency.id})")
        
        # 3. 获取或创建催收队列（用于小组关联）
        queue = db.query(CaseQueue).filter(
            CaseQueue.tenant_id == tenant.id
        ).first()
        
        if not queue:
            print("\n⚠️  未找到催收队列，创建默认队列...")
            queue = CaseQueue(
                tenant_id=tenant.id,
                queue_name="默认催收队列",
                queue_code="QUEUE_DEFAULT",
                description="系统默认催收队列",
                is_active=True
            )
            db.add(queue)
            db.flush()
            print(f"✓ 创建队列: {queue.queue_name} (ID: {queue.id})")
        else:
            print(f"✓ 找到队列: {queue.queue_name} (ID: {queue.id})")
        
        # 4. 检查是否已存在小组群
        existing_groups = db.query(TeamGroup).filter(
            TeamGroup.tenant_id == tenant.id,
            TeamGroup.agency_id == agency.id
        ).count()
        
        if existing_groups > 0:
            print(f"\n⚠️  已存在 {existing_groups} 个小组群")
            overwrite = input("是否删除现有数据重新创建? (y/n): ").lower()
            if overwrite == 'y':
                # 删除现有小组群和相关数据
                db.query(TeamAdminAccount).filter(
                    TeamAdminAccount.team_group_id.isnot(None)
                ).delete()
                db.query(TeamGroup).filter(
                    TeamGroup.tenant_id == tenant.id,
                    TeamGroup.agency_id == agency.id
                ).delete()
                db.commit()
                print("✓ 已删除现有小组群数据")
        
        print("\n" + "=" * 60)
        print("创建小组群 1: 高额案件组")
        print("=" * 60)
        
        # 5. 创建小组群1
        team_group_1 = TeamGroup(
            tenant_id=tenant.id,
            agency_id=agency.id,
            group_code="GROUP_HIGH_VALUE",
            group_name="高额案件组",
            group_name_en="High Value Cases Group",
            description="负责处理高额催收案件，包含高额逾期组和高额法务组",
            sort_order=1,
            is_active=True
        )
        db.add(team_group_1)
        db.flush()
        
        print(f"✓ 创建小组群: {team_group_1.group_name} (ID: {team_group_1.id})")
        
        # 6. 为小组群1创建SPV管理员
        spv_1 = TeamAdminAccount(
            tenant_id=tenant.id,
            agency_id=agency.id,
            team_group_id=team_group_1.id,
            account_code="SPV_zhangsan",
            account_name="张三",
            login_id="zhangsan",
            password_hash=get_password_hash("password123"),
            role="spv",
            mobile="13800138001",
            email="zhangsan@example.com",
            remark="高额案件组小组群长",
            is_active=True
        )
        db.add(spv_1)
        db.flush()
        
        print(f"✓ 创建SPV管理员: {spv_1.account_name} (登录ID: {spv_1.login_id})")
        print(f"  密码: password123")
        
        print("\n" + "=" * 60)
        print("创建小组群 2: 普通案件组")
        print("=" * 60)
        
        # 7. 创建小组群2
        team_group_2 = TeamGroup(
            tenant_id=tenant.id,
            agency_id=agency.id,
            group_code="GROUP_NORMAL",
            group_name="普通案件组",
            group_name_en="Normal Cases Group",
            description="负责处理普通催收案件，包含短期、中期、长期逾期组",
            sort_order=2,
            is_active=True
        )
        db.add(team_group_2)
        db.flush()
        
        print(f"✓ 创建小组群: {team_group_2.group_name} (ID: {team_group_2.id})")
        
        # 8. 为小组群2创建SPV管理员
        spv_2 = TeamAdminAccount(
            tenant_id=tenant.id,
            agency_id=agency.id,
            team_group_id=team_group_2.id,
            account_code="SPV_lisi",
            account_name="李四",
            login_id="lisi",
            password_hash=get_password_hash("password123"),
            role="spv",
            mobile="13800138002",
            email="lisi@example.com",
            remark="普通案件组小组群长",
            is_active=True
        )
        db.add(spv_2)
        db.flush()
        
        print(f"✓ 创建SPV管理员: {spv_2.account_name} (登录ID: {spv_2.login_id})")
        print(f"  密码: password123")
        
        # 9. 更新现有小组，关联到小组群
        print("\n" + "=" * 60)
        print("更新现有小组，关联到小组群...")
        print("=" * 60)
        
        teams = db.query(CollectionTeam).filter(
            CollectionTeam.tenant_id == tenant.id,
            CollectionTeam.agency_id == agency.id
        ).all()
        
        if not teams:
            print("⚠️  未找到现有小组，创建测试小组...")
            
            # 创建测试小组
            test_teams = [
                {
                    "team_code": "TEAM_HIGH_01",
                    "team_name": "高额逾期组",
                    "team_group_id": team_group_1.id,
                    "description": "处理高额逾期案件"
                },
                {
                    "team_code": "TEAM_HIGH_02",
                    "team_name": "高额法务组",
                    "team_group_id": team_group_1.id,
                    "description": "处理高额案件的法务事宜"
                },
                {
                    "team_code": "TEAM_NORMAL_01",
                    "team_name": "短期逾期组",
                    "team_group_id": team_group_2.id,
                    "description": "处理短期逾期案件"
                },
                {
                    "team_code": "TEAM_NORMAL_02",
                    "team_name": "中期逾期组",
                    "team_group_id": team_group_2.id,
                    "description": "处理中期逾期案件"
                },
                {
                    "team_code": "TEAM_NORMAL_03",
                    "team_name": "长期逾期组",
                    "team_group_id": team_group_2.id,
                    "description": "处理长期逾期案件"
                }
            ]
            
            for team_data in test_teams:
                team = CollectionTeam(
                    tenant_id=tenant.id,
                    agency_id=agency.id,
                    team_group_id=team_data["team_group_id"],
                    queue_id=queue.id,
                    team_code=team_data["team_code"],
                    team_name=team_data["team_name"],
                    description=team_data["description"],
                    max_case_count=100,
                    sort_order=0,
                    is_active=True
                )
                db.add(team)
                db.flush()
                
                group_name = team_group_1.group_name if team_data["team_group_id"] == team_group_1.id else team_group_2.group_name
                print(f"  ✓ 创建小组: {team.team_name} → {group_name}")
        
        else:
            # 更新现有小组
            updated_count = 0
            for i, team in enumerate(teams):
                # 前半部分分配给小组群1，后半部分分配给小组群2
                if i < len(teams) // 2:
                    team.team_group_id = team_group_1.id
                    group_name = team_group_1.group_name
                else:
                    team.team_group_id = team_group_2.id
                    group_name = team_group_2.group_name
                
                # 确保小组有队列ID
                if not team.queue_id:
                    team.queue_id = queue.id
                
                updated_count += 1
                print(f"  ✓ 更新小组: {team.team_name} → {group_name}")
            
            print(f"\n✓ 共更新 {updated_count} 个小组")
        
        # 10. 提交所有更改
        db.commit()
        
        print("\n" + "=" * 60)
        print("✅ Mock数据创建成功！")
        print("=" * 60)
        
        print("\n📊 数据统计:")
        print(f"  - 小组群数量: 2")
        print(f"  - SPV管理员数量: 2")
        print(f"  - 小组群1 ({team_group_1.group_name}):")
        team1_count = db.query(CollectionTeam).filter(
            CollectionTeam.team_group_id == team_group_1.id
        ).count()
        print(f"    └─ 包含小组: {team1_count} 个")
        print(f"  - 小组群2 ({team_group_2.group_name}):")
        team2_count = db.query(CollectionTeam).filter(
            CollectionTeam.team_group_id == team_group_2.id
        ).count()
        print(f"    └─ 包含小组: {team2_count} 个")
        
        print("\n🔐 SPV管理员登录信息:")
        print(f"  1. {spv_1.account_name}")
        print(f"     登录ID: {spv_1.login_id}")
        print(f"     密码: password123")
        print(f"     邮箱: {spv_1.email}")
        print(f"  2. {spv_2.account_name}")
        print(f"     登录ID: {spv_2.login_id}")
        print(f"     密码: password123")
        print(f"     邮箱: {spv_2.email}")
        
        print("\n🎯 下一步:")
        print("  1. 重启后端服务: ./restart_backend.sh")
        print("  2. 刷新前端页面")
        print("  3. 在'组织管理 → 小组群管理'查看新创建的小组群")
        print("  4. 在'组织管理 → 小组管理'查看更新后的小组")
        
        return True
        
    except Exception as e:
        db.rollback()
        print(f"\n❌ 错误: {e}")
        import traceback
        traceback.print_exc()
        return False
    finally:
        db.close()


if __name__ == "__main__":
    success = create_team_groups_mock_data()
    sys.exit(0 if success else 1)

