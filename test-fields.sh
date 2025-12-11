#!/bin/bash

echo "🧪 开始测试案件列表标准字段..."

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查后端服务是否运行
echo ""
echo "🔍 检查后端服务..."
if ! lsof -i :8080 > /dev/null 2>&1; then
    echo -e "${RED}❌ 后端服务未运行，请先启动服务${NC}"
    echo "启动命令: cd backend-java && ./start.sh"
    exit 1
fi
echo -e "${GREEN}✅ 后端服务正在运行${NC}"

# 测试后端API
echo ""
echo "📡 测试后端API..."
response=$(curl -s http://localhost:8080/api/v1/standard-fields/case-list)

# 检查返回码
code=$(echo $response | jq -r '.code' 2>/dev/null)
if [ "$code" != "200" ]; then
    echo -e "${RED}❌ API返回错误，状态码: $code${NC}"
    echo "响应内容: $response"
    exit 1
fi
echo -e "${GREEN}✅ API响应正常${NC}"

# 检查字段数量
count=$(echo $response | jq '.data | length')
if [ "$count" -eq 19 ]; then
    echo -e "${GREEN}✅ 字段数量正确: $count${NC}"
else
    echo -e "${RED}❌ 字段数量错误: 预期19，实际$count${NC}"
    exit 1
fi

# 检查新增字段
echo ""
echo "🔍 检查新增字段..."
declare -A field_info=(
    ["user_id"]="用户id|String|true"
    ["collection_type"]="首复借类型|Enum|false"
    ["waived_amount"]="减免金额|Decimal|false"
    ["settlement_time"]="结清时间|Datetime|false"
    ["settlement_method"]="结清方式|Enum|false"
)

all_passed=true

for field in "${!field_info[@]}"; do
    IFS='|' read -r expected_name expected_type expected_required <<< "${field_info[$field]}"
    
    # 检查字段是否存在
    field_data=$(echo $response | jq ".data[] | select(.fieldKey==\"$field\")")
    
    if [ -z "$field_data" ]; then
        echo -e "${RED}❌ 字段缺失: $field${NC}"
        all_passed=false
        continue
    fi
    
    # 验证字段名称
    actual_name=$(echo $field_data | jq -r '.fieldName')
    if [ "$actual_name" != "$expected_name" ]; then
        echo -e "${RED}❌ $field 字段名称错误: 预期'$expected_name'，实际'$actual_name'${NC}"
        all_passed=false
    else
        echo -e "${GREEN}✅ $field: 字段名称正确 ($actual_name)${NC}"
    fi
    
    # 验证数据类型
    actual_type=$(echo $field_data | jq -r '.fieldDataType')
    if [ "$actual_type" != "$expected_type" ]; then
        echo -e "${RED}❌ $field 数据类型错误: 预期'$expected_type'，实际'$actual_type'${NC}"
        all_passed=false
    else
        echo -e "   └─ 数据类型: $actual_type"
    fi
    
    # 验证必填属性
    actual_required=$(echo $field_data | jq -r '.required')
    if [ "$actual_required" != "$expected_required" ]; then
        echo -e "${RED}❌ $field 必填属性错误: 预期'$expected_required'，实际'$actual_required'${NC}"
        all_passed=false
    else
        echo -e "   └─ 必填: $actual_required"
    fi
done

# 检查字段顺序
echo ""
echo "🔢 检查字段顺序..."

# 验证关键位置的字段
declare -A position_check=(
    [2]="user_id"
    [5]="collection_type"
    [8]="waived_amount"
    [18]="settlement_time"
    [19]="settlement_method"
)

for pos in "${!position_check[@]}"; do
    expected_key="${position_check[$pos]}"
    actual_key=$(echo $response | jq -r ".data[$((pos-1))].fieldKey")
    
    if [ "$actual_key" == "$expected_key" ]; then
        echo -e "${GREEN}✅ 位置 $pos: $actual_key${NC}"
    else
        echo -e "${RED}❌ 位置 $pos 错误: 预期'$expected_key'，实际'$actual_key'${NC}"
        all_passed=false
    fi
done

# 测试案件详情标准字段
echo ""
echo "📋 测试案件详情标准字段..."
detail_response=$(curl -s http://localhost:8080/api/v1/standard-fields/case-detail)
detail_count=$(echo $detail_response | jq '.data | length')

if [ "$detail_count" -eq 19 ]; then
    echo -e "${GREEN}✅ 案件详情字段数量正确: $detail_count${NC}"
else
    echo -e "${RED}❌ 案件详情字段数量错误: 预期19，实际$detail_count${NC}"
    all_passed=false
fi

# 检查detail_前缀
detail_fields=("detail_user_id" "detail_collection_type" "detail_waived_amount" "detail_settlement_time" "detail_settlement_method")
for field in "${detail_fields[@]}"; do
    exists=$(echo $detail_response | jq ".data[] | select(.fieldKey==\"$field\") | .fieldKey" -r)
    if [ "$exists" == "$field" ]; then
        echo -e "${GREEN}✅ 详情字段存在: $field${NC}"
    else
        echo -e "${RED}❌ 详情字段缺失: $field${NC}"
        all_passed=false
    fi
done

# 最终结果
echo ""
echo "================================"
if [ "$all_passed" = true ]; then
    echo -e "${GREEN}🎉 所有测试通过！${NC}"
    echo ""
    echo "✨ 新增字段列表："
    echo "   1. 用户id (user_id) - 必填"
    echo "   2. 首复借类型 (collection_type) - 选填"
    echo "   3. 减免金额 (waived_amount) - 选填"
    echo "   4. 结清时间 (settlement_time) - 选填"
    echo "   5. 结清方式 (settlement_method) - 选填"
    echo ""
    echo "📝 详细说明文档："
    echo "   ./说明文档/功能说明/案件列表标准字段新增说明.md"
    echo ""
    exit 0
else
    echo -e "${RED}❌ 测试失败，请检查上述错误${NC}"
    exit 1
fi
