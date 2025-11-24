#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
从CSV文件生成标准字段管理的SQL初始化脚本
包括：字段分组、标准字段、Mock甲方数据
"""

import csv
import json
import os
from pathlib import Path

# CSV文件路径
CSV_DIR = Path(__file__).parent.parent / "参考内容"

# 输出SQL文件路径
OUTPUT_SQL = Path(__file__).parent.parent / "backend-java" / "src" / "main" / "resources" / "db" / "data" / "init_standard_fields.sql"

def parse_enum_type(enum_str):
    """解析枚举类型字符串，返回枚举选项列表"""
    if not enum_str or enum_str.strip() == '':
        return None
    
    # 处理格式如: "Male / Female" 或 "待还款 / 已还清"
    options = [opt.strip() for opt in enum_str.split('/') if opt.strip()]
    if not options:
        return None
    
    # 返回JSON格式的枚举选项
    enum_list = []
    for opt in options:
        enum_list.append({
            "standard_name": opt,
            "standard_id": opt.lower().replace(' ', '_'),
            "tenant_name": opt,
            "tenant_id": opt.lower().replace(' ', '_')
        })
    
    return json.dumps(enum_list, ensure_ascii=False)

def escape_sql_string(s):
    """转义SQL字符串中的特殊字符"""
    if not s:
        return ''
    # 转义单引号
    return s.replace("'", "''").replace("\\", "\\\\")

def parse_csv_file(csv_path):
    """解析CSV文件，返回字段列表"""
    fields = []
    current_category = None
    
    with open(csv_path, 'r', encoding='utf-8-sig') as f:
        reader = csv.DictReader(f)
        for row in reader:
            # 检查分类列是否有值（即使字段名称也有值，也要更新分类）
            category_value = row.get('分类', '').strip()
            if category_value:
                current_category = category_value
            
            field_name = row.get('字段名称', '').strip()
            if not field_name:
                continue
            
            field_key = row.get('英文名', '').strip()
            field_type = row.get('类型', '').strip()
            example = row.get('示例', '').strip()
            description = row.get('说明', '').strip()
            is_extended = row.get('拓展字段', '').strip() == '是'
            
            # 解析枚举值
            enum_options = None
            if field_type == 'Enum':
                enum_str = example if example else ''
                enum_options = parse_enum_type(enum_str)
            
            fields.append({
                'field_name': escape_sql_string(field_name),
                'field_key': field_key,
                'field_type': field_type,
                'example_value': escape_sql_string(example),
                'description': escape_sql_string(description),
                'is_extended': is_extended,
                'enum_options': enum_options,
                'category': current_category
            })
    
    return fields

def generate_field_groups_sql():
    """生成字段分组的SQL"""
    sql = """
-- ============================================
-- 字段分组初始化
-- ============================================

-- 一级分组
INSERT INTO `field_groups` (`id`, `group_key`, `group_name`, `group_name_en`, `parent_id`, `sort_order`, `is_active`, `created_at`, `updated_at`) VALUES
(1, 'customer_basic', '客户基础信息', 'Customer Basic Information', NULL, 1, 1, NOW(), NOW()),
(2, 'loan_details', '贷款详情', 'Loan Details', NULL, 2, 1, NOW(), NOW()),
(3, 'borrowing_records', '借款记录', 'Borrowing Records', NULL, 3, 1, NOW(), NOW()),
(4, 'repayment_records', '还款记录', 'Repayment Records', NULL, 4, 1, NOW(), NOW()),
(5, 'installment_details', '分期详情', 'Installment Details', NULL, 5, 1, NOW(), NOW());

-- 二级分组（客户基础信息的子分组）
INSERT INTO `field_groups` (`id`, `group_key`, `group_name`, `group_name_en`, `parent_id`, `sort_order`, `is_active`, `created_at`, `updated_at`) VALUES
(11, 'identity_info', '基础身份信息', 'Identity Information', 1, 1, 1, NOW(), NOW()),
(12, 'education', '教育信息', 'Education', 1, 2, 1, NOW(), NOW()),
(13, 'employment', '职业信息', 'Employment', 1, 3, 1, NOW(), NOW()),
(14, 'user_behavior', '用户行为与信用', 'User Behavior & Credit', 1, 4, 1, NOW(), NOW()),
(15, 'contact_info', '联系方式', 'Contact Information', 1, 5, 1, NOW(), NOW());

"""
    return sql

def generate_standard_fields_sql():
    """生成标准字段的SQL"""
    sql = "\n-- ============================================\n-- 标准字段初始化\n-- ============================================\n\n"
    
    # 分组ID映射
    group_mapping = {
        '一、基础身份信息（Identity Information）': 11,
        '二、教育（Education）': 12,
        '三、职业信息（Employment）': 13,
        '四、用户行为与信用（User Behavior & Credit）': 14,
        '五、联系方式（Contact Information）': 15,  # 联系方式分组
    }
    
    # 处理客户基础信息CSV
    customer_csv = CSV_DIR / "CCO催收字段集合-客户基础信息.csv"
    if customer_csv.exists():
        fields = parse_csv_file(customer_csv)
        field_id = 1
        sort_order = 1
        
        for field in fields:
            category = field.get('category', '')
            group_id = group_mapping.get(category, 11)  # 默认基础身份信息
            
            # 判断是否必填（根据字段名称判断）
            is_required = field['field_key'] in ['user_id', 'user_name']
            
            enum_sql = 'NULL'
            if field['enum_options']:
                # 转义JSON字符串中的单引号
                enum_json_escaped = escape_sql_string(field['enum_options'])
                enum_sql = f"'{enum_json_escaped}'"
            
            sql += f"""INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
({field_id}, '{field['field_key']}', '{field['field_name']}', '{field['field_key']}', '{field['field_type']}', {group_id}, {1 if is_required else 0}, {1 if field['is_extended'] else 0}, '{field['description']}', '{field['example_value']}', {enum_sql}, {sort_order}, 1, 0, NOW(), NOW());

"""
            field_id += 1
            sort_order += 1
    
    # 处理贷款详情CSV
    loan_csv = CSV_DIR / "CCO催收字段集合-贷款详情.csv"
    if loan_csv.exists():
        fields = parse_csv_file(loan_csv)
        sort_order = 1
        
        for field in fields:
            is_required = field['field_key'] in ['loan_id']
            
            enum_sql = 'NULL'
            if field['enum_options']:
                enum_json_escaped = escape_sql_string(field['enum_options'])
                enum_sql = f"'{enum_json_escaped}'"
            
            sql += f"""INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
({field_id}, '{field['field_key']}', '{field['field_name']}', '{field['field_key']}', '{field['field_type']}', 2, {1 if is_required else 0}, {1 if field['is_extended'] else 0}, '{field['description']}', '{field['example_value']}', {enum_sql}, {sort_order}, 1, 0, NOW(), NOW());

"""
            field_id += 1
            sort_order += 1
    
    # 处理借款记录CSV
    borrowing_csv = CSV_DIR / "CCO催收字段集合-借款记录.csv"
    if borrowing_csv.exists():
        fields = parse_csv_file(borrowing_csv)
        sort_order = 1
        
        for field in fields:
            is_required = field['field_key'] in ['loan_id', 'user_id']
            
            enum_sql = 'NULL'
            if field['enum_options']:
                enum_sql = f"'{field['enum_options']}'"
            
            sql += f"""INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
({field_id}, '{field['field_key']}', '{field['field_name']}', '{field['field_key']}', '{field['field_type']}', 3, {1 if is_required else 0}, {1 if field['is_extended'] else 0}, '{field['description']}', '{field['example_value']}', {enum_sql}, {sort_order}, 1, 0, NOW(), NOW());

"""
            field_id += 1
            sort_order += 1
    
    # 处理分期详情CSV
    installment_csv = CSV_DIR / "CCO催收字段集合-分期详情.csv"
    if installment_csv.exists():
        fields = parse_csv_file(installment_csv)
        sort_order = 1
        
        for field in fields:
            is_required = False
            
            enum_sql = 'NULL'
            if field['enum_options']:
                enum_sql = f"'{field['enum_options']}'"
            
            sql += f"""INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
({field_id}, '{field['field_key']}', '{field['field_name']}', '{field['field_key']}', '{field['field_type']}', 5, {1 if is_required else 0}, {1 if field['is_extended'] else 0}, '{field['description']}', '{field['example_value']}', {enum_sql}, {sort_order}, 1, 0, NOW(), NOW());

"""
            field_id += 1
            sort_order += 1
    
    # 处理还款记录CSV
    repayment_csv = CSV_DIR / "CCO催收字段集合-还款记录.csv"
    if repayment_csv.exists():
        fields = parse_csv_file(repayment_csv)
        sort_order = 1
        
        for field in fields:
            is_required = field['field_key'] in ['repayment_time', 'repayment_amount']
            
            enum_sql = 'NULL'
            if field['enum_options']:
                enum_sql = f"'{field['enum_options']}'"
            
            sql += f"""INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
({field_id}, '{field['field_key']}', '{field['field_name']}', '{field['field_key']}', '{field['field_type']}', 4, {1 if is_required else 0}, {1 if field['is_extended'] else 0}, '{field['description']}', '{field['example_value']}', {enum_sql}, {sort_order}, 1, 0, NOW(), NOW());

"""
            field_id += 1
            sort_order += 1
    
    return sql

def generate_mock_tenant_sql():
    """生成Mock甲方数据的SQL"""
    sql = """
-- ============================================
-- Mock甲方数据初始化
-- ============================================

-- 创建Mock甲方
INSERT INTO `tenants` (`id`, `tenant_code`, `tenant_name`, `tenant_name_en`, `country_code`, `timezone`, `currency_code`, `is_active`, `created_at`, `updated_at`) VALUES
(1, 'MOCK_TENANT_A', 'Mock甲方A', 'Mock Tenant A', 'PH', 8, 'PHP', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `tenant_name` = VALUES(`tenant_name`);

-- 为Mock甲方启用所有标准字段
INSERT INTO `tenant_field_configs` (`tenant_id`, `field_id`, `field_type`, `is_enabled`, `is_required`, `is_readonly`, `is_visible`, `sort_order`, `created_at`, `updated_at`)
SELECT 
    1 as `tenant_id`,
    `id` as `field_id`,
    'standard' as `field_type`,
    1 as `is_enabled`,
    `is_required`,
    0 as `is_readonly`,
    1 as `is_visible`,
    `sort_order`,
    NOW() as `created_at`,
    NOW() as `updated_at`
FROM `standard_fields`
WHERE `is_deleted` = 0 AND `is_active` = 1
ON DUPLICATE KEY UPDATE 
    `is_enabled` = VALUES(`is_enabled`),
    `is_required` = VALUES(`is_required`),
    `is_visible` = VALUES(`is_visible`),
    `sort_order` = VALUES(`sort_order`),
    `updated_at` = NOW();

"""
    return sql

def main():
    """主函数：生成完整的SQL初始化脚本"""
    print("=" * 60)
    print("开始生成标准字段管理SQL初始化脚本...")
    print("=" * 60)
    
    # 确保输出目录存在
    OUTPUT_SQL.parent.mkdir(parents=True, exist_ok=True)
    
    # 生成SQL内容
    sql_content = """-- ============================================
-- 标准字段管理初始化SQL
-- 基于CSV配置文件生成
-- 生成时间: """ + str(Path(__file__).stat().st_mtime) + """
-- ============================================

USE `cco_system`;

-- 清空现有数据（可选，谨慎使用）
-- DELETE FROM `tenant_field_configs` WHERE `tenant_id` = 1;
-- DELETE FROM `standard_fields`;
-- DELETE FROM `field_groups` WHERE `id` IN (11, 12, 13, 14);
-- DELETE FROM `field_groups` WHERE `id` IN (1, 2, 3, 4, 5);

"""
    
    # 添加字段分组SQL
    sql_content += generate_field_groups_sql()
    
    # 添加标准字段SQL
    sql_content += generate_standard_fields_sql()
    
    # 添加Mock甲方数据SQL
    sql_content += generate_mock_tenant_sql()
    
    sql_content += "\n-- ============================================\n-- 初始化完成\n-- ============================================\n"
    
    # 写入文件
    with open(OUTPUT_SQL, 'w', encoding='utf-8') as f:
        f.write(sql_content)
    
    print(f"\n✅ SQL文件已生成: {OUTPUT_SQL}")
    print(f"   文件大小: {OUTPUT_SQL.stat().st_size} 字节")
    print("\n" + "=" * 60)
    print("✅ 生成完成！")
    print("=" * 60)

if __name__ == '__main__':
    main()

