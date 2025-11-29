#!/bin/bash

# 组织架构管理数据持久化功能测试脚本
# 测试内容：
# 1. 创建甲方（同时创建管理员账号）
# 2. 查询甲方列表和详情
# 3. 更新甲方
# 4. 创建小组管理员
# 5. 查询小组管理员列表和详情
# 6. 更新小组管理员
# 7. 重置小组管理员密码
# 8. 配置机构作息时间
# 9. 查询机构作息时间
# 10. 业务规则检查（营业时间）

BASE_URL="http://localhost:8080"
API_PREFIX="/api/v1"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 测试结果统计
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 存储测试中创建的ID
TENANT_ID=""
TENANT_ADMIN_ID=""
TEAM_ADMIN_ID=""
AGENCY_ID=1  # 假设机构ID为1（如果不存在，需要先创建）

echo "=========================================="
echo "组织架构管理数据持久化功能测试"
echo "=========================================="
echo ""
echo "测试环境: ${BASE_URL}"
echo "测试时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo ""

# 辅助函数：打印测试标题
print_test() {
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -e "${BLUE}【测试${TOTAL_TESTS}】${1}${NC}"
    echo "----------------------------------------"
}

# 辅助函数：检查响应
check_response() {
    local response="$1"
    local expected_code="${2:-200}"
    
    if echo "$response" | jq -e ".code == ${expected_code}" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ 测试通过${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        return 0
    else
        echo -e "${RED}✗ 测试失败${NC}"
        echo "响应内容:"
        echo "$response" | jq '.' 2>/dev/null || echo "$response"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
}

# 辅助函数：提取ID
extract_id() {
    echo "$1" | jq -r '.data.id // .data.tenant_id // empty' 2>/dev/null
}

# ============================================
# 测试1: 创建甲方（同时创建管理员账号）
# ============================================
print_test "创建甲方（同时创建管理员账号）"

RESPONSE=$(curl -s -X POST "${BASE_URL}${API_PREFIX}/tenants" \
  -H "Content-Type: application/json" \
  -d '{
    "tenant_code": "TEST_TENANT_'$(date +%s)'",
    "tenant_name": "测试甲方",
    "tenant_name_en": "Test Tenant",
    "country_code": "CN",
    "timezone": 8,
    "currency_code": "CNY",
    "admin_info": {
      "username": "test_admin_'$(date +%s)'",
      "name": "测试管理员",
      "email": "test_admin@example.com",
      "password": "password123"
    }
  }')

if check_response "$RESPONSE" 200; then
    TENANT_ID=$(extract_id "$RESPONSE")
    echo "创建的甲方ID: ${TENANT_ID}"
    
    # 检查响应中是否包含管理员信息
    if echo "$RESPONSE" | jq -e '.data.admin != null' > /dev/null 2>&1; then
        TENANT_ADMIN_ID=$(echo "$RESPONSE" | jq -r '.data.admin.id // empty')
        echo "创建的管理员ID: ${TENANT_ADMIN_ID}"
        echo -e "${GREEN}✓ 管理员账号已同时创建${NC}"
    else
        echo -e "${YELLOW}⚠ 响应中未包含管理员信息${NC}"
    fi
else
    echo "响应内容:"
    echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
fi
echo ""

# ============================================
# 测试2: 查询甲方列表
# ============================================
print_test "查询甲方列表"

RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PREFIX}/tenants?keyword=测试&isActive=true")

if check_response "$RESPONSE" 200; then
    COUNT=$(echo "$RESPONSE" | jq '.data | length' 2>/dev/null || echo "0")
    echo "查询到的甲方数量: ${COUNT}"
    if [ "$COUNT" -gt 0 ]; then
        echo "第一个甲方信息:"
        echo "$RESPONSE" | jq '.data[0] | {id, tenant_code, tenant_name, is_active}' 2>/dev/null
    fi
else
    echo "响应内容:"
    echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
fi
echo ""

# ============================================
# 测试3: 查询甲方详情
# ============================================
if [ -n "$TENANT_ID" ]; then
    print_test "查询甲方详情（ID: ${TENANT_ID}）"
    
    RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PREFIX}/tenants/${TENANT_ID}")
    
    if check_response "$RESPONSE" 200; then
        echo "甲方详情:"
        echo "$RESPONSE" | jq '.data | {id, tenant_code, tenant_name, country_code, timezone, currency_code, is_active}' 2>/dev/null
    else
        echo "响应内容:"
        echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
    fi
    echo ""
fi

# ============================================
# 测试4: 更新甲方
# ============================================
if [ -n "$TENANT_ID" ]; then
    print_test "更新甲方（ID: ${TENANT_ID}）"
    
    RESPONSE=$(curl -s -X PUT "${BASE_URL}${API_PREFIX}/tenants/${TENANT_ID}" \
      -H "Content-Type: application/json" \
      -d '{
        "tenant_name": "更新后的测试甲方",
        "country_code": "US",
        "timezone": -5,
        "currency_code": "USD"
      }')
    
    if check_response "$RESPONSE" 200; then
        echo "更新后的甲方信息:"
        echo "$RESPONSE" | jq '.data | {id, tenant_name, country_code, timezone, currency_code}' 2>/dev/null
    else
        echo "响应内容:"
        echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
    fi
    echo ""
fi

# ============================================
# 测试5: 创建小组管理员
# ============================================
if [ -n "$TENANT_ID" ]; then
    print_test "创建小组管理员"
    
    RESPONSE=$(curl -s -X POST "${BASE_URL}${API_PREFIX}/team-admins" \
      -H "Content-Type: application/json" \
      -d '{
        "tenant_id": '${TENANT_ID}',
        "agency_id": '${AGENCY_ID}',
        "team_id": 1,
        "account_name": "测试小组管理员",
        "login_id": "test_team_admin_'$(date +%s)'",
        "email": "team_admin@example.com",
        "mobile": "13800138000",
        "password": "password123",
        "role": "team_leader"
      }')
    
    if check_response "$RESPONSE" 200; then
        TEAM_ADMIN_ID=$(extract_id "$RESPONSE")
        echo "创建的小组管理员ID: ${TEAM_ADMIN_ID}"
        echo "管理员信息:"
        echo "$RESPONSE" | jq '.data | {id, account_name, login_id, role, is_active}' 2>/dev/null
    else
        echo "响应内容:"
        echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
    fi
    echo ""
fi

# ============================================
# 测试6: 查询小组管理员列表
# ============================================
if [ -n "$TENANT_ID" ]; then
    print_test "查询小组管理员列表（tenant_id: ${TENANT_ID}）"
    
    RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PREFIX}/team-admins?tenant_id=${TENANT_ID}&is_active=true")
    
    if check_response "$RESPONSE" 200; then
        COUNT=$(echo "$RESPONSE" | jq '.data | length' 2>/dev/null || echo "0")
        echo "查询到的小组管理员数量: ${COUNT}"
        if [ "$COUNT" -gt 0 ]; then
            echo "第一个管理员信息:"
            echo "$RESPONSE" | jq '.data[0] | {id, account_name, login_id, role, is_active}' 2>/dev/null
        fi
    else
        echo "响应内容:"
        echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
    fi
    echo ""
fi

# ============================================
# 测试7: 查询小组管理员详情
# ============================================
if [ -n "$TEAM_ADMIN_ID" ]; then
    print_test "查询小组管理员详情（ID: ${TEAM_ADMIN_ID}）"
    
    RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PREFIX}/team-admins/${TEAM_ADMIN_ID}")
    
    if check_response "$RESPONSE" 200; then
        echo "管理员详情:"
        echo "$RESPONSE" | jq '.data | {id, account_name, login_id, email, mobile, role, is_active}' 2>/dev/null
    else
        echo "响应内容:"
        echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
    fi
    echo ""
fi

# ============================================
# 测试8: 更新小组管理员
# ============================================
if [ -n "$TEAM_ADMIN_ID" ]; then
    print_test "更新小组管理员（ID: ${TEAM_ADMIN_ID}）"
    
    RESPONSE=$(curl -s -X PUT "${BASE_URL}${API_PREFIX}/team-admins/${TEAM_ADMIN_ID}" \
      -H "Content-Type: application/json" \
      -d '{
        "account_name": "更新后的测试小组管理员",
        "email": "updated_team_admin@example.com",
        "mobile": "13900139000",
        "role": "quality_inspector"
      }')
    
    if check_response "$RESPONSE" 200; then
        echo "更新后的管理员信息:"
        echo "$RESPONSE" | jq '.data | {id, account_name, email, mobile, role}' 2>/dev/null
    else
        echo "响应内容:"
        echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
    fi
    echo ""
fi

# ============================================
# 测试9: 重置小组管理员密码
# ============================================
if [ -n "$TEAM_ADMIN_ID" ]; then
    print_test "重置小组管理员密码（ID: ${TEAM_ADMIN_ID}）"
    
    RESPONSE=$(curl -s -X PUT "${BASE_URL}${API_PREFIX}/team-admins/${TEAM_ADMIN_ID}/password" \
      -H "Content-Type: application/json" \
      -d '{
        "new_password": "newpassword123"
      }')
    
    if check_response "$RESPONSE" 200; then
        echo "密码重置成功"
        echo "$RESPONSE" | jq '.data' 2>/dev/null
    else
        echo "响应内容:"
        echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
    fi
    echo ""
fi

# ============================================
# 测试10: 更新小组管理员状态
# ============================================
if [ -n "$TEAM_ADMIN_ID" ]; then
    print_test "更新小组管理员状态（ID: ${TEAM_ADMIN_ID}，禁用）"
    
    RESPONSE=$(curl -s -X PUT "${BASE_URL}${API_PREFIX}/team-admins/${TEAM_ADMIN_ID}/status" \
      -H "Content-Type: application/json" \
      -d '{
        "is_active": false
      }')
    
    if check_response "$RESPONSE" 200; then
        echo "状态更新成功"
        echo "$RESPONSE" | jq '.data' 2>/dev/null
    else
        echo "响应内容:"
        echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
    fi
    echo ""
fi

# ============================================
# 测试11: 配置机构作息时间
# ============================================
print_test "配置机构作息时间（agency_id: ${AGENCY_ID}）"

RESPONSE=$(curl -s -X PUT "${BASE_URL}${API_PREFIX}/agencies/${AGENCY_ID}/working-hours" \
  -H "Content-Type: application/json" \
  -d '{
    "working_hours": [
      {
        "day_of_week": 1,
        "start_time": "09:00",
        "end_time": "18:00",
        "is_active": true
      },
      {
        "day_of_week": 2,
        "start_time": "09:00",
        "end_time": "18:00",
        "is_active": true
      },
      {
        "day_of_week": 3,
        "start_time": "09:00",
        "end_time": "18:00",
        "is_active": true
      },
      {
        "day_of_week": 4,
        "start_time": "09:00",
        "end_time": "18:00",
        "is_active": true
      },
      {
        "day_of_week": 5,
        "start_time": "09:00",
        "end_time": "18:00",
        "is_active": true
      },
      {
        "day_of_week": 6,
        "start_time": null,
        "end_time": null,
        "is_active": false
      },
      {
        "day_of_week": 7,
        "start_time": null,
        "end_time": null,
        "is_active": false
      }
    ]
  }')

if check_response "$RESPONSE" 200; then
    COUNT=$(echo "$RESPONSE" | jq '.data | length' 2>/dev/null || echo "0")
    echo "配置的作息时间数量: ${COUNT}"
    echo "周一到周五的作息时间:"
    echo "$RESPONSE" | jq '.data[0:5] | .[] | {day_of_week, start_time, end_time, is_active}' 2>/dev/null
else
    echo "响应内容:"
    echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
fi
echo ""

# ============================================
# 测试12: 查询机构作息时间
# ============================================
print_test "查询机构作息时间（agency_id: ${AGENCY_ID}）"

RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PREFIX}/agencies/${AGENCY_ID}/working-hours")

if check_response "$RESPONSE" 200; then
    COUNT=$(echo "$RESPONSE" | jq '.data | length' 2>/dev/null || echo "0")
    echo "查询到的作息时间数量: ${COUNT}（应该为7天）"
    if [ "$COUNT" -gt 0 ]; then
        echo "作息时间详情:"
        echo "$RESPONSE" | jq '.data | .[] | {day_of_week, start_time, end_time, is_active}' 2>/dev/null
    fi
else
    echo "响应内容:"
    echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
fi
echo ""

# ============================================
# 测试13: 业务规则检查 - 营业时间内
# ============================================
print_test "业务规则检查 - 检查是否在营业时间内（工作日14:30）"

# 获取下周一14:30的时间（ISO格式）
NEXT_MONDAY=$(date -v+Mon -v14H -v30M -v0S '+%Y-%m-%dT%H:%M:%S' 2>/dev/null || date -d 'next monday 14:30' '+%Y-%m-%dT%H:%M:%S' 2>/dev/null || echo "2025-01-13T14:30:00")

RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PREFIX}/business-rules/working-hours/check?agency_id=${AGENCY_ID}&datetime=${NEXT_MONDAY}")

if check_response "$RESPONSE" 200; then
    IS_WORKING=$(echo "$RESPONSE" | jq -r '.data.is_working_hours' 2>/dev/null)
    echo "检查时间: ${NEXT_MONDAY}"
    echo "是否在营业时间内: ${IS_WORKING}"
    echo "详细信息:"
    echo "$RESPONSE" | jq '.data' 2>/dev/null
else
    echo "响应内容:"
    echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
fi
echo ""

# ============================================
# 测试14: 业务规则检查 - 非营业时间
# ============================================
print_test "业务规则检查 - 检查是否在营业时间内（周末10:00）"

# 获取下周六10:00的时间
NEXT_SATURDAY=$(date -v+Sat -v10H -v0M -v0S '+%Y-%m-%dT%H:%M:%S' 2>/dev/null || date -d 'next saturday 10:00' '+%Y-%m-%dT%H:%M:%S' 2>/dev/null || echo "2025-01-18T10:00:00")

RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PREFIX}/business-rules/working-hours/check?agency_id=${AGENCY_ID}&datetime=${NEXT_SATURDAY}")

if check_response "$RESPONSE" 200; then
    IS_WORKING=$(echo "$RESPONSE" | jq -r '.data.is_working_hours' 2>/dev/null)
    echo "检查时间: ${NEXT_SATURDAY}"
    echo "是否在营业时间内: ${IS_WORKING}（应该为false）"
    echo "详细信息:"
    echo "$RESPONSE" | jq '.data' 2>/dev/null
else
    echo "响应内容:"
    echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
fi
echo ""

# ============================================
# 测试15: 删除甲方（软删除）
# ============================================
if [ -n "$TENANT_ID" ]; then
    print_test "删除甲方（软删除，ID: ${TENANT_ID}）"
    
    RESPONSE=$(curl -s -X DELETE "${BASE_URL}${API_PREFIX}/tenants/${TENANT_ID}")
    
    if check_response "$RESPONSE" 200; then
        echo "甲方已删除（设置为禁用状态）"
        echo "$RESPONSE" | jq '.data' 2>/dev/null
    else
        echo "响应内容:"
        echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
    fi
    echo ""
fi

# ============================================
# 测试总结
# ============================================
echo "=========================================="
echo "测试总结"
echo "=========================================="
echo "总测试数: ${TOTAL_TESTS}"
echo -e "${GREEN}通过: ${PASSED_TESTS}${NC}"
echo -e "${RED}失败: ${FAILED_TESTS}${NC}"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}✓ 所有测试通过！${NC}"
    exit 0
else
    echo -e "${RED}✗ 有 ${FAILED_TESTS} 个测试失败${NC}"
    exit 1
fi

