"""
测试甲方字段展示配置功能
"""
import requests
import json
from typing import Dict, Any

BASE_URL = "http://localhost:8000/api/v1"

def print_section(title: str):
    """打印分节标题"""
    print("\n" + "=" * 60)
    print(f"  {title}")
    print("=" * 60)

def print_result(result: Dict[str, Any], success: bool = True):
    """打印结果"""
    status = "✅ 成功" if success else "❌ 失败"
    print(f"\n{status}")
    print(json.dumps(result, indent=2, ensure_ascii=False))

def test_get_available_fields():
    """测试获取可用字段列表"""
    print_section("测试1: 获取可用字段列表")
    
    response = requests.get(
        f"{BASE_URL}/field-display-configs/available-fields",
        params={"tenant_id": 1}
    )
    
    if response.status_code == 200:
        data = response.json()
        print(f"✅ 获取成功，共 {len(data)} 个可用字段")
        
        # 显示前5个字段
        print("\n前5个字段示例:")
        for field in data[:5]:
            print(f"  - {field['field_name']} ({field['field_key']})")
            print(f"    类型: {field['field_type']}, 来源: {field['field_source']}")
            print(f"    分组: {field.get('field_group_name', '-')}")
        
        return data
    else:
        print(f"❌ 失败: {response.status_code}")
        print(response.text)
        return []

def test_get_scene_types():
    """测试获取场景类型"""
    print_section("测试2: 获取场景类型")
    
    response = requests.get(f"{BASE_URL}/field-display-configs/scene-types")
    
    if response.status_code == 200:
        data = response.json()
        print_result(data)
        return data
    else:
        print(f"❌ 失败: {response.status_code}")
        print(response.text)
        return []

def test_create_field_display_config(available_fields):
    """测试创建字段展示配置"""
    print_section("测试3: 创建字段展示配置")
    
    if not available_fields:
        print("⚠️  跳过：没有可用字段")
        return None
    
    # 选择第一个标准字段
    field = next((f for f in available_fields if f['field_source'] == 'standard'), available_fields[0])
    
    config_data = {
        "tenant_id": 1,
        "scene_type": "admin_case_list",
        "scene_name": "控台案件管理列表",
        "field_key": field['field_key'],
        "field_name": field['field_name'],
        "field_data_type": field['field_type'],
        "field_source": field['field_source'],
        "sort_order": 1,
        "display_width": 150,
        "color_type": "normal",
        "is_searchable": field['field_type'] in ['String', 'Text'],
        "is_filterable": field['field_type'] == 'Enum',
        "hide_for_queues": [],
        "hide_for_agencies": [],
        "hide_for_teams": []
    }
    
    print(f"\n创建字段配置: {field['field_name']} ({field['field_key']})")
    
    response = requests.post(
        f"{BASE_URL}/field-display-configs",
        json=config_data
    )
    
    if response.status_code == 200:
        data = response.json()
        print_result(data)
        return data
    else:
        print(f"❌ 失败: {response.status_code}")
        print(response.text)
        return None

def test_get_field_display_configs():
    """测试获取字段展示配置列表"""
    print_section("测试4: 获取字段展示配置列表")
    
    response = requests.get(
        f"{BASE_URL}/field-display-configs",
        params={
            "tenant_id": 1,
            "scene_type": "admin_case_list"
        }
    )
    
    if response.status_code == 200:
        data = response.json()
        print(f"✅ 获取成功，共 {len(data)} 个配置")
        
        # 显示所有配置
        print("\n配置列表:")
        for config in data:
            print(f"  - {config['field_name']} ({config['field_key']})")
            print(f"    类型: {config.get('field_data_type', '-')}, 来源: {config.get('field_source', '-')}")
            print(f"    宽度: {config['display_width']}, 可搜索: {config.get('is_searchable', False)}, 可筛选: {config.get('is_filterable', False)}")
        
        return data
    else:
        print(f"❌ 失败: {response.status_code}")
        print(response.text)
        return []

def test_update_field_display_config(config_id: int):
    """测试更新字段展示配置"""
    print_section("测试5: 更新字段展示配置")
    
    update_data = {
        "display_width": 200,
        "color_type": "red",
        "is_searchable": True
    }
    
    print(f"\n更新配置 ID: {config_id}")
    print(f"更新内容: {json.dumps(update_data, ensure_ascii=False)}")
    
    response = requests.put(
        f"{BASE_URL}/field-display-configs/{config_id}",
        json=update_data
    )
    
    if response.status_code == 200:
        data = response.json()
        print_result(data)
        return data
    else:
        print(f"❌ 失败: {response.status_code}")
        print(response.text)
        return None

def test_batch_create_or_update_configs(available_fields):
    """测试批量创建或更新配置"""
    print_section("测试6: 批量创建或更新配置")
    
    if not available_fields:
        print("⚠️  跳过：没有可用字段")
        return []
    
    # 选择前3个字段
    fields_to_add = available_fields[:3]
    
    configs = []
    for idx, field in enumerate(fields_to_add):
        configs.append({
            "tenant_id": 1,
            "scene_type": "collector_case_list",
            "scene_name": "催员案件列表",
            "field_key": field['field_key'],
            "field_name": field['field_name'],
            "field_data_type": field['field_type'],
            "field_source": field['field_source'],
            "sort_order": idx + 1,
            "display_width": 0,  # 自动宽度
            "color_type": "normal",
            "is_searchable": field['field_type'] in ['String', 'Text'],
            "is_filterable": field['field_type'] == 'Enum',
            "hide_for_queues": [],
            "hide_for_agencies": [],
            "hide_for_teams": []
        })
    
    print(f"\n批量创建 {len(configs)} 个配置")
    
    response = requests.post(
        f"{BASE_URL}/field-display-configs/batch",
        params={
            "tenant_id": 1,
            "scene_type": "collector_case_list"
        },
        json=configs
    )
    
    if response.status_code == 200:
        data = response.json()
        print(f"✅ 批量创建成功，共 {len(data)} 个配置")
        for config in data:
            print(f"  - {config['field_name']} ({config['field_key']})")
        return data
    else:
        print(f"❌ 失败: {response.status_code}")
        print(response.text)
        return []

def test_delete_field_display_config(config_id: int):
    """测试删除字段展示配置"""
    print_section("测试7: 删除字段展示配置")
    
    print(f"\n删除配置 ID: {config_id}")
    
    response = requests.delete(f"{BASE_URL}/field-display-configs/{config_id}")
    
    if response.status_code == 200:
        data = response.json()
        print_result(data)
        return True
    else:
        print(f"❌ 失败: {response.status_code}")
        print(response.text)
        return False

def main():
    """主测试函数"""
    print("\n" + "=" * 60)
    print("  甲方字段展示配置功能测试")
    print("=" * 60)
    
    try:
        # 1. 获取可用字段
        available_fields = test_get_available_fields()
        
        # 2. 获取场景类型
        scene_types = test_get_scene_types()
        
        # 3. 创建字段展示配置
        created_config = test_create_field_display_config(available_fields)
        
        # 4. 获取配置列表
        configs = test_get_field_display_configs()
        
        # 5. 更新配置
        if created_config:
            test_update_field_display_config(created_config['id'])
        
        # 6. 批量创建或更新配置
        batch_configs = test_batch_create_or_update_configs(available_fields)
        
        # 7. 删除配置
        if created_config:
            test_delete_field_display_config(created_config['id'])
        
        # 测试完成
        print("\n" + "=" * 60)
        print("  ✅ 所有测试完成！")
        print("=" * 60)
        
        # 测试总结
        print("\n📊 测试总结:")
        print(f"  - 可用字段数: {len(available_fields)}")
        print(f"  - 场景类型数: {len(scene_types)}")
        print(f"  - 当前配置数: {len(configs)}")
        print(f"  - 批量创建数: {len(batch_configs)}")
        
    except Exception as e:
        print(f"\n❌ 测试失败: {str(e)}")
        import traceback
        traceback.print_exc()

if __name__ == '__main__':
    main()

