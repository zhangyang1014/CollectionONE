"""
更新案件状态字段的枚举值
"""
from mock_field_data import STANDARD_FIELDS
import json

# 案件状态的枚举值配置
CASE_STATUS_ENUM_VALUES = [
    {
        "standard_name": "待还款",
        "standard_id": "pending",
        "tenant_name": "待还款",
        "tenant_id": "pending"
    },
    {
        "standard_name": "部分还款",
        "standard_id": "partial",
        "tenant_name": "部分还款",
        "tenant_id": "partial"
    },
    {
        "standard_name": "正常结清",
        "standard_id": "settled",
        "tenant_name": "正常结清",
        "tenant_id": "settled"
    },
    {
        "standard_name": "展期结清",
        "standard_id": "extended_settled",
        "tenant_name": "展期结清",
        "tenant_id": "extended_settled"
    }
]

# 更新所有case_status字段
updated_fields = []
for field in STANDARD_FIELDS:
    field_copy = field.copy()
    if field['field_key'] == 'case_status' or field['field_key'] == 'case_status_borrow':
        field_copy['enum_values'] = CASE_STATUS_ENUM_VALUES
        print(f"已更新字段: {field['field_name']} (id={field['id']}, field_key={field['field_key']})")
    updated_fields.append(field_copy)

# 生成新的mock_field_data.py
with open('mock_field_data.py', 'w', encoding='utf-8') as f:
    f.write('"""\n')
    f.write('Mock数据 - 标准字段（系统级通用字段）\n')
    f.write('仅包含标准字段，拓展字段已移至"甲方自定义字段"\n')
    f.write('"""\n\n')
    f.write('# 字段分组\n')
    f.write('FIELD_GROUPS = ')
    
    # 写入FIELD_GROUPS
    from mock_field_data import FIELD_GROUPS
    f.write(str(FIELD_GROUPS))
    f.write('\n\n')
    
    f.write('# 标准字段（系统级，所有甲方自动继承）\n')
    f.write('STANDARD_FIELDS = ')
    f.write(str(updated_fields))
    f.write('\n')

print("\n✅ 更新完成！案件状态字段已配置枚举值。")

