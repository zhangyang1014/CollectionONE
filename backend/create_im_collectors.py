"""
创建IM端测试催员
将 collectors_data.json 中的催员导入到数据库
"""
import json
from app.core.database import SessionLocal
from app.models.collector import Collector
from app.core.security import get_password_hash


def create_im_collectors():
    """创建IM端测试催员"""
    db = SessionLocal()
    
    try:
        print("=" * 60)
        print("创建IM端测试催员")
        print("=" * 60)
        
        # 读取 collectors_data.json
        with open('collectors_data.json', 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        collectors_data = data['collectors']
        print(f"\n找到 {len(collectors_data)} 个催员数据")
        
        # 使用SHA256密码哈希（123456）- bcrypt模块有问题，使用SHA256作为替代
        import hashlib
        password_hash = hashlib.sha256("123456".encode()).hexdigest()
        print(f"使用SHA256密码哈希: {password_hash}")
        
        created_count = 0
        updated_count = 0
        
        for collector_data in collectors_data:
            collector_id = collector_data['collectorId']
            tenant_id = int(collector_data['tenantId'])
            
            print(f"\n处理催员: {collector_id} ({collector_data['collectorName']})")
            
            # 检查催员是否已存在
            existing = db.query(Collector).filter(
                Collector.login_id == collector_id
            ).first()
            
            if existing:
                print(f"  - 催员已存在，跳过")
                updated_count += 1
                continue
            
            # 获取第一个机构和小组（简化处理）
            # 实际应该根据tenant_id查询对应的机构和小组
            from app.models.collection_agency import CollectionAgency
            from app.models.collection_team import CollectionTeam
            
            agency = db.query(CollectionAgency).filter(
                CollectionAgency.tenant_id == tenant_id
            ).first()
            
            if not agency:
                print(f"  - 错误：找不到甲方ID {tenant_id} 的机构")
                continue
            
            team = db.query(CollectionTeam).filter(
                CollectionTeam.agency_id == agency.id
            ).first()
            
            if not team:
                print(f"  - 错误：找不到机构ID {agency.id} 的小组")
                continue
            
            # 创建催员
            collector = Collector(
                tenant_id=tenant_id,
                agency_id=agency.id,
                team_id=team.id,
                collector_code=collector_id,  # 使用 collectorId 作为 collector_code
                collector_name=collector_data['collectorName'],
                login_id=collector_id,  # 使用 collectorId 作为 login_id
                password_hash=password_hash,
                mobile=collector_data.get('phone', ''),
                email=collector_data.get('email', ''),
                collector_level=collector_data.get('roleName', '催员'),
                status='active',
                is_active=True
            )
            
            db.add(collector)
            created_count += 1
            print(f"  - ✅ 创建成功")
        
        # 提交更改
        db.commit()
        
        print("\n" + "=" * 60)
        print(f"完成！")
        print(f"  - 新创建: {created_count} 个")
        print(f"  - 已存在: {updated_count} 个")
        print("=" * 60)
        
        # 显示可用的登录账号
        print("\n可用的IM端登录账号：")
        print("-" * 60)
        collectors = db.query(Collector).filter(
            Collector.login_id.in_(['BTQ001', 'BTQ002', 'BTQ003', 'BTSK001', 'BTSK002', 'BTSK003'])
        ).all()
        
        for c in collectors:
            print(f"机构ID: {c.tenant_id} | 催员ID: {c.login_id} | 姓名: {c.collector_name} | 密码: 123456")
        
    except Exception as e:
        print(f"\n❌ 错误: {str(e)}")
        import traceback
        traceback.print_exc()
        db.rollback()
    finally:
        db.close()


if __name__ == "__main__":
    create_im_collectors()

