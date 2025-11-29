#!/bin/bash

# 甲方字段JSON上传功能测试脚本

BASE_URL="http://localhost:8080"
API_PREFIX="/api/v1"
TENANT_ID=1

echo "=========================================="
echo "甲方字段JSON上传功能测试"
echo "=========================================="
echo ""

# 1. 测试获取当前版本（应该返回空或Mock数据）
echo "【测试1】获取当前版本JSON数据"
echo "----------------------------------------"
curl -s -X GET "${BASE_URL}${API_PREFIX}/tenants/${TENANT_ID}/fields-json" \
  -H "Content-Type: application/json" | jq '.' || echo "响应不是JSON格式"
echo ""
echo ""

# 2. 测试校验JSON格式 - 缺少必填字段
echo "【测试2】校验JSON格式 - 缺少必填字段（应该失败）"
echo "----------------------------------------"
curl -s -X POST "${BASE_URL}${API_PREFIX}/tenants/${TENANT_ID}/fields-json/validate" \
  -F "file=@test-data/test1-invalid-missing-version.json" | jq '.' || echo "响应不是JSON格式"
echo ""
echo ""

# 3. 测试校验JSON格式 - 字段类型无效
echo "【测试3】校验JSON格式 - 字段类型无效（应该失败）"
echo "----------------------------------------"
curl -s -X POST "${BASE_URL}${API_PREFIX}/tenants/${TENANT_ID}/fields-json/validate" \
  -F "file=@test-data/test2-invalid-field-type.json" | jq '.' || echo "响应不是JSON格式"
echo ""
echo ""

# 4. 测试校验JSON格式 - Enum类型缺少enum_values
echo "【测试4】校验JSON格式 - Enum类型缺少enum_values（应该失败）"
echo "----------------------------------------"
curl -s -X POST "${BASE_URL}${API_PREFIX}/tenants/${TENANT_ID}/fields-json/validate" \
  -F "file=@test-data/test3-invalid-enum-missing-values.json" | jq '.' || echo "响应不是JSON格式"
echo ""
echo ""

# 5. 测试校验JSON格式 - 格式正确
echo "【测试5】校验JSON格式 - 格式正确（应该成功）"
echo "----------------------------------------"
curl -s -X POST "${BASE_URL}${API_PREFIX}/tenants/${TENANT_ID}/fields-json/validate" \
  -F "file=@test-data/test4-valid-basic.json" | jq '.' || echo "响应不是JSON格式"
echo ""
echo ""

# 6. 测试版本对比 - 首次上传（应该都是新增字段）
echo "【测试6】版本对比 - 首次上传（应该都是新增字段）"
echo "----------------------------------------"
COMPARE_DATA=$(cat test-data/test4-valid-basic.json)
curl -s -X POST "${BASE_URL}${API_PREFIX}/tenants/${TENANT_ID}/fields-json/compare" \
  -H "Content-Type: application/json" \
  -d "{\"fieldsJson\": ${COMPARE_DATA}}" | jq '.' || echo "响应不是JSON格式"
echo ""
echo ""

# 7. 测试上传并保存JSON文件
echo "【测试7】上传并保存JSON文件（首次）"
echo "----------------------------------------"
UPLOAD_DATA=$(cat test-data/test4-valid-basic.json | jq '{version: .version, syncTime: .sync_time, fields: .fields}')
curl -s -X POST "${BASE_URL}${API_PREFIX}/tenants/${TENANT_ID}/fields-json/upload" \
  -H "Content-Type: application/json" \
  -d "${UPLOAD_DATA}" | jq '.' || echo "响应不是JSON格式"
echo ""
echo ""

# 8. 再次获取当前版本（应该返回刚上传的数据）
echo "【测试8】获取当前版本JSON数据（应该返回刚上传的数据）"
echo "----------------------------------------"
curl -s -X GET "${BASE_URL}${API_PREFIX}/tenants/${TENANT_ID}/fields-json" \
  -H "Content-Type: application/json" | jq '.' || echo "响应不是JSON格式"
echo ""
echo ""

# 9. 测试版本对比 - 新增字段
echo "【测试9】版本对比 - 新增字段"
echo "----------------------------------------"
COMPARE_DATA2=$(cat test-data/test5-valid-with-new-field.json)
curl -s -X POST "${BASE_URL}${API_PREFIX}/tenants/${TENANT_ID}/fields-json/compare" \
  -H "Content-Type: application/json" \
  -d "{\"fieldsJson\": ${COMPARE_DATA2}}" | jq '.' || echo "响应不是JSON格式"
echo ""
echo ""

# 10. 测试版本对比 - 修改字段
echo "【测试10】版本对比 - 修改字段"
echo "----------------------------------------"
COMPARE_DATA3=$(cat test-data/test6-valid-modified-field.json)
curl -s -X POST "${BASE_URL}${API_PREFIX}/tenants/${TENANT_ID}/fields-json/compare" \
  -H "Content-Type: application/json" \
  -d "{\"fieldsJson\": ${COMPARE_DATA3}}" | jq '.' || echo "响应不是JSON格式"
echo ""
echo ""

# 11. 测试版本对比 - 枚举值变化
echo "【测试11】版本对比 - 枚举值变化"
echo "----------------------------------------"
COMPARE_DATA4=$(cat test-data/test7-valid-enum-changes.json)
curl -s -X POST "${BASE_URL}${API_PREFIX}/tenants/${TENANT_ID}/fields-json/compare" \
  -H "Content-Type: application/json" \
  -d "{\"fieldsJson\": ${COMPARE_DATA4}}" | jq '.' || echo "响应不是JSON格式"
echo ""
echo ""

# 12. 测试获取历史版本列表
echo "【测试12】获取历史版本列表"
echo "----------------------------------------"
curl -s -X GET "${BASE_URL}${API_PREFIX}/tenants/${TENANT_ID}/fields-json/history" \
  -H "Content-Type: application/json" | jq '.' || echo "响应不是JSON格式"
echo ""
echo ""

echo "=========================================="
echo "测试完成"
echo "=========================================="

