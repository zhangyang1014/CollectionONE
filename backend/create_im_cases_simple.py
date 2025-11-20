"""
使用SQL直接插入方式为IM催员创建案件数据
避免SQLAlchemy 2.x的RETURNING子句问题
"""
import sqlite3
from datetime import datetime, timedelta
import random
import json

def create_im_cases():
    """使用SQL直接创建案件数据"""
    conn = sqlite3.connect('cco_test.db')
    cursor = conn.cursor()
    
    try:
        print("=" * 80)
        print("为IM催员创建案件数据（使用SQL直接插入）")
        print("=" * 80)
        
        # 1. 获取IM催员信息
        cursor.execute("""
            SELECT id, login_id, collector_name, tenant_id, agency_id, team_id
            FROM collectors
            WHERE login_id IN ('BTQ001', 'BTQ002', 'BTQ003', 'BTSK001', 'BTSK002', 'BTSK003')
        """)
        collectors = cursor.fetchall()
        
        print(f"\n找到 {len(collectors)} 个IM催员")
        
        # 2. 获取下一个可用的ID
        cursor.execute("SELECT MAX(id) FROM cases")
        max_case_id = cursor.fetchall()[0][0] or 0
        
        cursor.execute("SELECT MAX(id) FROM case_contacts")
        max_contact_id = cursor.fetchall()[0][0] or 0
        
        cursor.execute("SELECT MAX(id) FROM communication_records")
        max_comm_id = cursor.fetchall()[0][0] or 0
        
        total_cases = 0
        total_contacts = 0
        total_comms = 0
        
        # 3. 为每个催员创建案件
        for collector_id, login_id, collector_name, tenant_id, agency_id, team_id in collectors:
            print(f"\n处理催员: {login_id} ({collector_name})")
            
            # 获取该甲方的队列
            cursor.execute("""
                SELECT id FROM case_queues
                WHERE tenant_id = ?
                LIMIT 1
            """, (tenant_id,))
            queue = cursor.fetchone()
            
            if not queue:
                print(f"  ⚠️  甲方 {tenant_id} 没有队列，跳过")
                continue
            
            queue_id = queue[0]
            
            # 为每个催员创建10个案件
            for i in range(10):
                max_case_id += 1
                case_code = f"{login_id}_CASE_{i+1:03d}_{datetime.now().strftime('%Y%m%d')}"
                
                overdue_days = random.randint(1, 90)
                loan_amount = random.randint(5000, 50000)
                repaid_amount = random.randint(0, int(loan_amount * 0.8))
                outstanding_amount = loan_amount - repaid_amount
                
                case_status_options = ['pending_repayment', 'partial_repayment']
                case_status = random.choice(case_status_options)
                
                due_date = (datetime.now() - timedelta(days=overdue_days)).strftime('%Y-%m-%d %H:%M:%S')
                assigned_at = (datetime.now() - timedelta(days=random.randint(1, 30))).strftime('%Y-%m-%d %H:%M:%S')
                next_follow_up = (datetime.now() + timedelta(days=random.randint(1, 3))).strftime('%Y-%m-%d %H:%M:%S')
                now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                
                # 插入案件
                cursor.execute("""
                    INSERT INTO cases (
                        id, case_code, tenant_id, agency_id, team_id, collector_id, queue_id,
                        user_id, user_name, mobile, case_status, overdue_days,
                        loan_amount, repaid_amount, outstanding_amount,
                        due_date, assigned_at, next_follow_up_at, created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, (
                    max_case_id, case_code, tenant_id, agency_id, team_id, collector_id, queue_id,
                    f"USER_{tenant_id}_{i+1:05d}", f"用户{i+1:03d}",
                    f"+52 55 {random.randint(1000, 9999)} {random.randint(1000, 9999)}",
                    case_status, overdue_days,
                    loan_amount, repaid_amount, outstanding_amount,
                    due_date, assigned_at, next_follow_up, now, now
                ))
                
                total_cases += 1
                
                # 为案件创建2-3个联系人
                contact_count = random.randint(2, 3)
                for j in range(contact_count):
                    max_contact_id += 1
                    
                    contact_types = ['本人', '配偶', '朋友', '同事', '亲属']
                    contact_type = contact_types[j] if j < len(contact_types) else '其他'
                    
                    available_channels = json.dumps(['phone', 'sms'])
                    if random.random() < 0.6:
                        available_channels = json.dumps(['phone', 'sms', 'whatsapp'])
                    
                    cursor.execute("""
                        INSERT INTO case_contacts (
                            id, case_id, contact_name, phone_number, relation,
                            is_primary, available_channels, remark, created_at, updated_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, (
                        max_contact_id, max_case_id, f"用户{i+1:03d}的{contact_type}",
                        f"+52 55 {random.randint(1000, 9999)} {random.randint(1000, 9999)}",
                        contact_type, (j == 0), available_channels, f"{contact_type}联系方式", now, now
                    ))
                    
                    total_contacts += 1
                    
                    # 为该联系人创建1-2条沟通记录
                    comm_count = random.randint(1, 2)
                    for k in range(comm_count):
                        max_comm_id += 1
                        
                        channel_opts = ['phone', 'sms', 'whatsapp']
                        channel = random.choice(channel_opts)
                        
                        result_opts = ['answered', 'no_answer', 'busy']
                        result = random.choice(result_opts)
                        
                        is_connected = 1 if result == 'answered' else 0
                        call_duration = random.randint(30, 300) if is_connected else 0
                        
                        contacted_at = (datetime.now() - timedelta(days=random.randint(0, 15))).strftime('%Y-%m-%d %H:%M:%S')
                        
                        cursor.execute("""
                            INSERT INTO communication_records (
                                id, case_id, collector_id, contact_person_id,
                                channel, direction, contact_result,
                                is_connected, call_duration,
                                remark, contacted_at, created_at, updated_at
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """, (
                            max_comm_id, max_case_id, collector_id, max_contact_id,
                            channel, 'outbound', result,
                            is_connected if channel == 'phone' else None,
                            call_duration if channel == 'phone' else None,
                            f"第{k+1}次联系", contacted_at, now, now
                        ))
                        
                        total_comms += 1
            
            # 更新催员的当前案件数
            cursor.execute("""
                UPDATE collectors
                SET current_case_count = 10
                WHERE id = ?
            """, (collector_id,))
            
            print(f"  ✓ 完成: 创建了 10 个案件")
        
        # 提交事务
        conn.commit()
        
        print("\n" + "=" * 80)
        print("数据创建完成！")
        print("=" * 80)
        print(f"总计创建:")
        print(f"  - 案件: {total_cases} 个")
        print(f"  - 联系人: {total_contacts} 个")
        print(f"  - 沟通记录: {total_comms} 条")
        print("=" * 80)
        
        # 显示每个催员的案件数
        print("\n各催员案件分配情况:")
        print("-" * 80)
        cursor.execute("""
            SELECT c.login_id, c.collector_name, COUNT(ca.id) as case_count
            FROM collectors c
            LEFT JOIN cases ca ON c.id = ca.collector_id
            WHERE c.login_id IN ('BTQ001', 'BTQ002', 'BTQ003', 'BTSK001', 'BTSK002', 'BTSK003')
            GROUP BY c.id, c.login_id, c.collector_name
        """)
        results = cursor.fetchall()
        for login_id, collector_name, case_count in results:
            print(f"  {login_id} ({collector_name}): {case_count} 个案件")
        print("-" * 80)
        
    except Exception as e:
        print(f"\n❌ 错误: {str(e)}")
        import traceback
        traceback.print_exc()
        conn.rollback()
    finally:
        conn.close()


if __name__ == "__main__":
    create_im_cases()

