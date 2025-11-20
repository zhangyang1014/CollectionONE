"""为机构2创建小组群和SPV管理员的Mock数据"""
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
    return "$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5xyMQhKfYQr3a"

def create_agency2_team_groups():
    """为机构2创建小组群和SPV管理员"""
    # 直接使用SQLite数据库
    db_path = os.path.join(os.path.dirname(__file__), "cco_test.db")
    DATABASE_URL = f"sqlite:///{db_path}"
    
    engine = create_engine(DATABASE_URL)
    SessionLocal = sessionmaker(bind=engine)
    db = SessionLocal()
    
    try:
        print("=" * 60)
        print("为机构2创建小组群Mock数据...")
        print("=" * 60)
        
        # 1. 获取甲方和机构2
        tenant = db.query(Tenant).first()
        if not tenant:
            print("❌ 错误: 未找到甲方数据")
            return False
        
        print(f"\n✓ 找到甲方: {tenant.tenant_name} (ID: {tenant.id})")
        
        agency = db.query(CollectionAgency).filter(
            CollectionAgency.tenant_id == tenant.id,
            CollectionAgency.id == 2
        ).first()
        
        if not agency:
            print("❌ 错误: 未找到机构2")
            return False
        
        print(f"✓ 找到机构: {agency.agency_name} (ID: {agency.id})")
        
        # 2. 获取或创建催收队列
        queue = db.query(CaseQueue).filter(
            CaseQueue.tenant_id == tenant.id
        ).first()
        
        if not queue:
            print("❌ 错误: 未找到催收队列")
            return False
        
        print(f"✓ 找到队列: {queue.queue_name} (ID: {queue.id})")
        
        # 3. 检查机构2是否已有小组群
        existing_groups = db.query(TeamGroup).filter(
            TeamGroup.tenant_id == tenant.id,
            TeamGroup.agency_id == agency.id
        ).count()
        
        if existing_groups > 0:
            print(f"\n⚠️  机构2已有 {existing_groups} 个小组群，跳过创建")
            return True
        
        print("\n" + "=" * 60)
        print("创建小组群 1: 优质客户组")
        print("=" * 60)
        
        # 4. 创建小组群1
        team_group_1 = TeamGroup(
            tenant_id=tenant.id,
            agency_id=agency.id,
            group_code="GROUP_VIP",
            group_name="优质客户组",
            group_name_en="VIP Customers Group",
            description="负责处理优质客户的催收业务",
            sort_order=1,
            is_active=True
        )
        db.add(team_group_1)
        db.flush()
        
        print(f"✓ 创建小组群: {team_group_1.group_name} (ID: {team_group_1.id})")
        
        # 5. 为小组群1创建SPV管理员
        spv_1 = TeamAdminAccount(
            tenant_id=tenant.id,
            agency_id=agency.id,
            team_group_id=team_group_1.id,
            account_code="SPV_wangwu",
            account_name="王五",
            login_id="wangwu",
            password_hash=get_password_hash("password123"),
            role="spv",
            mobile="13800138003",
            email="wangwu@example.com",
            remark="优质客户组小组群长",
            is_active=True
        )
        db.add(spv_1)
        db.flush()
        
        print(f"✓ 创建SPV管理员: {spv_1.account_name} (登录ID: {spv_1.login_id})")
        print(f"  密码: password123")
        
        print("\n" + "=" * 60)
        print("创建小组群 2: 一般客户组")
        print("=" * 60)
        
        # 6. 创建小组群2
        team_group_2 = TeamGroup(
            tenant_id=tenant.id,
            agency_id=agency.id,
            group_code="GROUP_REGULAR",
            group_name="一般客户组",
            group_name_en="Regular Customers Group",
            description="负责处理一般客户的催收业务",
            sort_order=2,
            is_active=True
        )
        db.add(team_group_2)
        db.flush()
        
        print(f"✓ 创建小组群: {team_group_2.group_name} (ID: {team_group_2.id})")
        
        # 7. 为小组群2创建SPV管理员
        spv_2 = TeamAdminAccount(
            tenant_id=tenant.id,
            agency_id=agency.id,
            team_group_id=team_group_2.id,
            account_code="SPV_zhaoliu",
            account_name="赵六",
            login_id="zhaoliu",
            password_hash=get_password_hash("password123"),
            role="spv",
            mobile="13800138004",
            email="zhaoliu@example.com",
            remark="一般客户组小组群长",
            is_active=True
        )
        db.add(spv_2)
        db.flush()
        
        print(f"✓ 创建SPV管理员: {spv_2.account_name} (登录ID: {spv_2.login_id})")
        print(f"  密码: password123")
        
        # 8. 更新机构2的小组，关联到小组群
        print("\n" + "=" * 60)
        print("更新机构2的小组，关联到小组群...")
        print("=" * 60)
        
        teams = db.query(CollectionTeam).filter(
            CollectionTeam.tenant_id == tenant.id,
            CollectionTeam.agency_id == agency.id
        ).all()
        
        if teams:
            updated_count = 0
            for i, team in enumerate(teams):
                # 前半部分分配给小组群1，后半部分分配给小组群2
                if i < len(teams) // 2 or len(teams) == 1:
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
        else:
            print("  ⚠️  机构2暂无小组")
        
        # 9. 提交所有更改
        db.commit()
        
        print("\n" + "=" * 60)
        print("✅ Mock数据创建成功！")
        print("=" * 60)
        
        print("\n📊 数据统计:")
        print(f"  - 机构: {agency.agency_name}")
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
        
        print("\n🎯 现在可以为机构2的小组选择小组群了！")
        
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
    success = create_agency2_team_groups()
    sys.exit(0 if success else 1)

