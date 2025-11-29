#!/bin/bash
# 测试字段展示配置批量保存功能

echo "=========================================="
echo "测试字段展示配置批量保存功能"
echo "=========================================="

# 检查后端服务是否运行
if ! lsof -ti :8080 > /dev/null 2>&1; then
    echo "❌ 后端服务未运行，请先启动后端服务"
    echo "   运行: cd backend-java && ./start.sh"
    exit 1
fi

echo "✅ 后端服务正在运行"

# 测试数据
TENANT_ID=1
SCENE_TYPE="admin_case_list"

echo ""
echo "1. 测试获取配置列表..."
RESPONSE=$(curl -s -X GET "http://localhost:8080/api/v1/field-display-configs?tenant_id=${TENANT_ID}&scene_type=${SCENE_TYPE}" \
  -H "Authorization: Bearer test-token" \
  -H "Content-Type: application/json")

echo "响应: $RESPONSE" | head -c 500
echo ""

# 解析响应获取配置列表
CONFIGS=$(echo "$RESPONSE" | grep -o '"data":\[.*\]' | sed 's/"data"://' || echo "[]")

echo ""
echo "2. 测试批量保存（修改mobile字段的sort_order为4）..."

# 创建测试数据 - 将mobile字段移到第4位
TEST_DATA=$(cat <<EOF
{
  "tenant_id": ${TENANT_ID},
  "scene_type": "${SCENE_TYPE}",
  "configs": [
    {"id": 1, "field_key": "case_code", "field_name": "案件编号", "sort_order": 1},
    {"id": 2, "field_key": "user_name", "field_name": "客户姓名", "sort_order": 2},
    {"id": 3, "field_key": "loan_amount", "field_name": "贷款金额", "sort_order": 3},
    {"id": 4, "field_key": "mobile", "field_name": "手机号码", "sort_order": 4},
    {"id": 5, "field_key": "outstanding_amount", "field_name": "未还金额", "sort_order": 5}
  ]
}
EOF
)

SAVE_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/v1/field-display-configs/batch" \
  -H "Authorization: Bearer test-token" \
  -H "Content-Type: application/json" \
  -d "$TEST_DATA")

echo "保存响应: $SAVE_RESPONSE"

echo ""
echo "3. 再次获取配置列表，验证排序是否保存..."
RESPONSE2=$(curl -s -X GET "http://localhost:8080/api/v1/field-display-configs?tenant_id=${TENANT_ID}&scene_type=${SCENE_TYPE}" \
  -H "Authorization: Bearer test-token" \
  -H "Content-Type: application/json")

echo "响应: $RESPONSE2" | head -c 500
echo ""

# 检查mobile字段的sort_order是否为4
if echo "$RESPONSE2" | grep -q '"field_key":"mobile".*"sort_order":4'; then
    echo ""
    echo "✅ 测试通过！mobile字段的sort_order已保存为4"
else
    echo ""
    echo "❌ 测试失败！mobile字段的sort_order未正确保存"
    echo "   请检查后端日志查看详细信息"
fi

echo ""
echo "=========================================="
echo "测试完成"
echo "=========================================="




