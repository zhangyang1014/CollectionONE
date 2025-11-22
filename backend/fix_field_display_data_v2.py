"""
修复字段展示配置数据 v2：填充 field_data_type 和 field_source
包含字段映射规则
"""
import sqlite3
from datetime import datetime

# 字段映射规则：旧field_key -> 新field_key
FIELD_MAPPING = {
    'mobile': 'mobile_number',
    'case_code': 'loan_id',  # 案件编号通常用贷款编号
    'loan_amount': 'avg_loan_amount',
}

# 虚拟字段定义（不在标准字段表中，但需要配置的关联字段或计算字段）
VIRTUAL_FIELDS = {
    # 自定义字段（custom）- 关联字段和业务记录字段
    'queue_name': {'type': 'Enum', 'source': 'custom', 'name': '所属队列'},
    'agency_name': {'type': 'Enum', 'source': 'custom', 'name': '催收机构'},
    'team_name': {'type': 'Enum', 'source': 'custom', 'name': '催收小组'},
    'collector_name': {'type': 'Enum', 'source': 'custom', 'name': '催员姓名'},
    'assigned_at': {'type': 'Datetime', 'source': 'custom', 'name': '分配时间'},
    'due_date': {'type': 'Date', 'source': 'custom', 'name': '应还日期'},
    'loan_date': {'type': 'Date', 'source': 'custom', 'name': '放款日期'},
    'email': {'type': 'String', 'source': 'custom', 'name': '邮箱'},
    'emergency_contact_name': {'type': 'String', 'source': 'custom', 'name': '紧急联系人'},
    'emergency_contact_phone': {'type': 'String', 'source': 'custom', 'name': '紧急联系人电话'},
    
    # 系统字段（system）- 自动计算和统计的字段
    # 催收操作统计
    'days_assigned': {'type': 'Integer', 'source': 'system', 'name': '已分发天数'},
    'last_contact_time': {'type': 'Datetime', 'source': 'system', 'name': '最后联系时间'},
    'contact_count': {'type': 'Integer', 'source': 'system', 'name': '联系次数'},
    'view_phone_count': {'type': 'Integer', 'source': 'system', 'name': '查看本人联系电话次数'},
    'call_count': {'type': 'Integer', 'source': 'system', 'name': '电话拨打次数'},
    'call_connected_count': {'type': 'Integer', 'source': 'system', 'name': '电话拨通次数'},
    'is_phone_reachable': {'type': 'Boolean', 'source': 'system', 'name': '本人电话是否可联'},
    'whatsapp_sent_count': {'type': 'Integer', 'source': 'system', 'name': 'WA发送次数'},
    'whatsapp_reply_count': {'type': 'Integer', 'source': 'system', 'name': 'WA回复次数'},
    'is_whatsapp_reachable': {'type': 'Boolean', 'source': 'system', 'name': '本人WA是否可联'},
    'rcs_sent_count': {'type': 'Integer', 'source': 'system', 'name': 'RCS发送次数'},
    'rcs_reply_count': {'type': 'Integer', 'source': 'system', 'name': 'RCS回复次数'},
    'sms_sent_count': {'type': 'Integer', 'source': 'system', 'name': '短信发送次数'},
    # 客户历史统计
    'total_loan_count': {'type': 'Integer', 'source': 'system', 'name': '历史借款总笔数'},
    'cleared_loan_count': {'type': 'Integer', 'source': 'system', 'name': '已结清笔数'},
    'overdue_loan_count': {'type': 'Integer', 'source': 'system', 'name': '历史逾期笔数'},
    'max_overdue_days': {'type': 'Integer', 'source': 'system', 'name': '历史最大逾期天数'},
    'avg_loan_amount': {'type': 'Decimal', 'source': 'system', 'name': '平均借款金额'},
}

def fix_field_display_data():
    """修复字段展示配置数据"""
    db_path = 'cco_test.db'
    
    # 备份数据库
    backup_path = f'cco_test.db.backup_fix_display_v2_{datetime.now().strftime("%Y%m%d_%H%M%S")}'
    import shutil
    shutil.copy2(db_path, backup_path)
    print(f"✅ 数据库已备份到: {backup_path}")
    
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    try:
        # 1. 获取所有需要修复的配置
        cursor.execute("""
            SELECT id, field_key, field_name
            FROM tenant_field_display_configs 
            WHERE field_data_type IS NULL OR field_source IS NULL
        """)
        configs = cursor.fetchall()
        
        print(f"\n📦 需要修复 {len(configs)} 条配置记录")
        
        fixed_count = 0
        mapped_count = 0
        virtual_count = 0
        not_found_count = 0
        not_found_fields = []
        
        for config_id, field_key, field_name in configs:
            # 2. 检查是否需要映射
            search_key = FIELD_MAPPING.get(field_key, field_key)
            
            # 3. 先从标准字段表查询
            cursor.execute("""
                SELECT field_type, is_extended 
                FROM standard_fields 
                WHERE field_key = ? AND is_active = 1 AND is_deleted = 0
            """, (search_key,))
            
            result = cursor.fetchone()
            
            if result:
                field_type, is_extended = result
                field_source = 'extended' if is_extended else 'standard'
                
                # 更新配置
                cursor.execute("""
                    UPDATE tenant_field_display_configs 
                    SET field_data_type = ?, field_source = ?
                    WHERE id = ?
                """, (field_type, field_source, config_id))
                
                fixed_count += 1
                if search_key != field_key:
                    mapped_count += 1
                    print(f"  ✅ 映射修复: {field_name} ({field_key} -> {search_key}) -> {field_type} ({field_source})")
                else:
                    print(f"  ✅ 修复: {field_name} ({field_key}) -> {field_type} ({field_source})")
            elif field_key in VIRTUAL_FIELDS:
                # 4. 处理虚拟字段
                virtual_info = VIRTUAL_FIELDS[field_key]
                field_type = virtual_info['type']
                field_source = virtual_info['source']
                
                cursor.execute("""
                    UPDATE tenant_field_display_configs 
                    SET field_data_type = ?, field_source = ?
                    WHERE id = ?
                """, (field_type, field_source, config_id))
                
                fixed_count += 1
                virtual_count += 1
                print(f"  ✅ 虚拟字段: {field_name} ({field_key}) -> {field_type} ({field_source})")
            else:
                # 5. 查询自定义字段表
                cursor.execute("""
                    SELECT field_type 
                    FROM custom_fields 
                    WHERE field_key = ? AND is_active = 1 AND is_deleted = 0
                    LIMIT 1
                """, (field_key,))
                
                custom_result = cursor.fetchone()
                
                if custom_result:
                    field_type = custom_result[0]
                    field_source = 'custom'
                    
                    cursor.execute("""
                        UPDATE tenant_field_display_configs 
                        SET field_data_type = ?, field_source = ?
                        WHERE id = ?
                    """, (field_type, field_source, config_id))
                    
                    fixed_count += 1
                    print(f"  ✅ 自定义字段: {field_name} ({field_key}) -> {field_type} (custom)")
                else:
                    not_found_count += 1
                    if f"{field_name} ({field_key})" not in not_found_fields:
                        not_found_fields.append(f"{field_name} ({field_key})")
                    print(f"  ⚠️  未找到字段: {field_name} ({field_key})")
        
        # 提交事务
        conn.commit()
        
        print("\n" + "=" * 60)
        print(f"✅ 数据修复完成！")
        print(f"  - 成功修复: {fixed_count} 条")
        print(f"  - 其中映射修复: {mapped_count} 条")
        print(f"  - 其中虚拟字段: {virtual_count} 条")
        print(f"  - 未找到字段: {not_found_count} 条")
        
        if not_found_fields:
            print("\n⚠️  未找到的字段列表（去重后）:")
            for field in sorted(set(not_found_fields)):
                print(f"    - {field}")
        
        print("=" * 60)
        
        # 验证修复结果
        print("\n🔍 验证修复结果...")
        cursor.execute("""
            SELECT 
                COUNT(*) as total,
                SUM(CASE WHEN field_data_type IS NOT NULL THEN 1 ELSE 0 END) as has_type,
                SUM(CASE WHEN field_source IS NOT NULL THEN 1 ELSE 0 END) as has_source
            FROM tenant_field_display_configs
        """)
        total, has_type, has_source = cursor.fetchone()
        print(f"  - 总配置数: {total}")
        print(f"  - 有字段类型: {has_type}")
        print(f"  - 有字段来源: {has_source}")
        
        if has_type == total and has_source == total:
            print("\n✅ 所有配置都已修复！")
        else:
            missing = total - has_type
            print(f"\n⚠️  还有 {missing} 条配置缺少字段信息")
            if missing > 0:
                print("\n💡 建议：这些字段可能需要手动删除或重新配置")
        
    except Exception as e:
        print(f"\n❌ 修复失败: {str(e)}")
        conn.rollback()
        raise
    finally:
        conn.close()

if __name__ == '__main__':
    print("=" * 60)
    print("开始修复字段展示配置数据 v2")
    print("=" * 60)
    fix_field_display_data()

