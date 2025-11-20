"""为每个小组创建3个不同角色的管理员Mock数据"""
import sys
import os
from datetime import datetime
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

# 添加项目路径到sys.path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from app.models import TeamAdminAccount, CollectionTeam, CollectionAgency, Tenant

def get_password_hash(password: str) -> str:
    """生成密码哈希 - 使用一个已知的bcrypt哈希值（password123）"""
    # 这是 "password123" 的bcrypt哈希值
    return "$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5xyMQhKfYQr3a"

def create_team_admins_mock_data():
    """为每个小组创建3个不同角色的管理员"""
    # 直接使用SQLite数据库
    db_path = os.path.join(os.path.dirname(__file__), "cco_test.db")
    DATABASE_URL = f"sqlite:///{db_path}"
    
    engine = create_engine(DATABASE_URL)
    SessionLocal = sessionmaker(bind=engine)
    db = SessionLocal()
    
    try:
        print("=" * 60)
        print("开始为每个小组创建管理员Mock数据...")
        print("=" * 60)
        
        # 1. 获取第一个甲方
        tenant = db.query(Tenant).first()
        if not tenant:
            print("❌ 错误: 未找到甲方数据")
            return False
        
        print(f"\n✓ 找到甲方: {tenant.tenant_name} (ID: {tenant.id})")
        
        # 2. 获取所有机构
        agencies = db.query(CollectionAgency).filter(
            CollectionAgency.tenant_id == tenant.id
        ).all()
        
        if not agencies:
            print("❌ 错误: 未找到机构数据")
            return False
        
        print(f"✓ 找到机构: {len(agencies)} 个")
        
        # 3. 获取所有小组
        teams = db.query(CollectionTeam).filter(
            CollectionTeam.tenant_id == tenant.id
        ).all()
        
        if not teams:
            print("❌ 错误: 未找到小组数据")
            return False
        
        print(f"✓ 找到小组: {len(teams)} 个\n")
        
        # 4. 定义三种角色
        roles = [
            {
                "role": "supervisor",
                "role_name": "主管",
                "suffix": "supervisor"
            },
            {
                "role": "team_leader", 
                "role_name": "组长",
                "suffix": "leader"
            },
            {
                "role": "quality_inspector",
                "role_name": "质检员",
                "suffix": "inspector"
            }
        ]
        
        # 5. 为每个小组创建3个不同角色的管理员
        created_count = 0
        
        for i, team in enumerate(teams):
            print(f"\n{'=' * 60}")
            print(f"小组 {i+1}: {team.team_name} (ID: {team.id})")
            print(f"{'=' * 60}")
            
            # 为每个角色创建一个管理员
            for j, role_info in enumerate(roles):
                # 生成唯一的登录ID
                login_id = f"team{team.id}_{role_info['suffix']}"
                
                # 检查登录ID是否已存在
                existing = db.query(TeamAdminAccount).filter(
                    TeamAdminAccount.login_id == login_id
                ).first()
                
                if existing:
                    print(f"  ⚠️  跳过：{role_info['role_name']} - 登录ID已存在: {login_id}")
                    continue
                
                # 中文名字列表
                names = [
                    ["王五", "赵六", "孙七"],
                    ["周八", "吴九", "郑十"]
                ]
                
                name = names[i][j] if i < len(names) else f"管理员{i+1}-{j+1}"
                
                # 创建管理员账号
                admin = TeamAdminAccount(
                    tenant_id=tenant.id,
                    agency_id=team.agency_id,
                    team_id=team.id,
                    team_group_id=team.team_group_id,  # 继承小组的team_group_id
                    account_code=f"ADMIN_{login_id}",
                    account_name=name,
                    login_id=login_id,
                    password_hash=get_password_hash("password123"),
                    role=role_info['role'],
                    mobile=f"138{str(created_count + 10).zfill(8)}",
                    email=f"{login_id}@example.com",
                    remark=f"{team.team_name}-{role_info['role_name']}",
                    is_active=True
                )
                
                db.add(admin)
                db.flush()
                
                print(f"  ✓ 创建{role_info['role_name']}: {name}")
                print(f"    - 登录ID: {login_id}")
                print(f"    - 角色: {role_info['role']}")
                print(f"    - 邮箱: {admin.email}")
                print(f"    - 手机: {admin.mobile}")
                
                created_count += 1
        
        # 6. 提交所有更改
        db.commit()
        
        print("\n" + "=" * 60)
        print("✅ Mock数据创建成功！")
        print("=" * 60)
        
        # 7. 统计信息
        print("\n📊 数据统计:")
        for i, team in enumerate(teams):
            admin_count = db.query(TeamAdminAccount).filter(
                TeamAdminAccount.team_id == team.id
            ).count()
            print(f"  {i+1}. {team.team_name}")
            print(f"     └─ 管理员数量: {admin_count} 个")
        
        print(f"\n✓ 本次新建管理员: {created_count} 个")
        
        # 8. 列出所有创建的管理员
        print("\n" + "=" * 60)
        print("🔐 小组管理员登录信息")
        print("=" * 60)
        
        for i, team in enumerate(teams):
            print(f"\n【{team.team_name}】")
            admins = db.query(TeamAdminAccount).filter(
                TeamAdminAccount.team_id == team.id
            ).all()
            
            for admin in admins:
                role_name = {
                    "supervisor": "主管",
                    "team_leader": "组长", 
                    "quality_inspector": "质检员"
                }.get(admin.role, admin.role)
                
                print(f"  {role_name} - {admin.account_name}")
                print(f"    登录ID: {admin.login_id}")
                print(f"    密码: password123")
                print(f"    邮箱: {admin.email}")
        
        print("\n🎯 下一步:")
        print("  1. 刷新前端页面")
        print("  2. 在'人员与机构管理 → 小组管理员管理'查看新创建的管理员")
        print("  3. 使用任意管理员账号登录测试")
        
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
    success = create_team_admins_mock_data()
    sys.exit(0 if success else 1)

