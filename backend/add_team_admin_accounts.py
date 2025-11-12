"""
添加小组管理员账号mock数据
"""
import sys
import os
from datetime import datetime
import hashlib

# 添加项目根目录到路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from app.core.config import settings
from app.models.team_admin_account import TeamAdminAccount
from app.models.collection_team import CollectionTeam

def hash_password(password: str) -> str:
    """简单的密码哈希（生产环境应使用bcrypt）"""
    return hashlib.sha256(password.encode()).hexdigest()

def main():
    """添加小组管理员账号数据"""
    print("="*60)
    print("添加小组管理员账号mock数据")
    print("="*60)
    
    # 创建数据库引擎
    engine = create_engine(settings.DATABASE_URL)
    SessionLocal = sessionmaker(bind=engine)
    session = SessionLocal()
    
    try:
        # 获取所有小组
        teams = session.query(CollectionTeam).filter(
            CollectionTeam.is_active == True
        ).all()
        
        print(f"找到 {len(teams)} 个活跃小组")
        
        # 为每个小组创建1-2个管理员账号
        roles = ['team_leader', 'quality_inspector', 'statistician']
        account_count = 0
        
        for team in teams:
            # 检查是否已有账号
            existing_count = session.query(TeamAdminAccount).filter(
                TeamAdminAccount.team_id == team.id
            ).count()
            
            if existing_count > 0:
                print(f"⚠️  小组 {team.team_name} (ID: {team.id}) 已有 {existing_count} 个账号，跳过")
                continue
            
            # 为每个小组创建2个账号：1个小组长，1个质检员或统计员
            for idx, role in enumerate(['team_leader', 'quality_inspector']):
                account_code = f"ADMIN{team.tenant_id}{team.agency_id}{team.id}{idx+1}"
                login_id = f"admin{team.tenant_id}{team.agency_id}{team.id}{idx+1}"
                
                # 检查账号编码是否已存在
                existing = session.query(TeamAdminAccount).filter(
                    TeamAdminAccount.account_code == account_code
                ).first()
                
                if existing:
                    continue
                
                role_name_map = {
                    'team_leader': '小组长',
                    'quality_inspector': '质检员',
                    'statistician': '统计员'
                }
                
                account = TeamAdminAccount(
                    tenant_id=team.tenant_id,
                    agency_id=team.agency_id,
                    team_id=team.id,
                    account_code=account_code,
                    account_name=f"{team.team_name}{role_name_map[role]}",
                    login_id=login_id,
                    password_hash=hash_password("123456"),
                    role=role,
                    mobile=f"138{team.tenant_id}{team.agency_id}{team.id}{idx+1:02d}",
                    email=f"{login_id}@example.com",
                    remark=f"{role_name_map[role]}账号",
                    is_active=True
                )
                session.add(account)
                account_count += 1
                print(f"✅ 已添加：{account.account_name} (登录ID: {account.login_id})")
        
        session.commit()
        
        # 显示所有账号
        all_accounts = session.query(TeamAdminAccount).all()
        print(f"\n当前小组管理员账号总数: {len(all_accounts)}")
        for acc in all_accounts:
            print(f"  - {acc.account_name} (ID: {acc.id}, 小组: {acc.team_id}, 角色: {acc.role})")
        
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

