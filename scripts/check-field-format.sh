#!/bin/bash
# 检查Controller中是否有驼峰格式字段

echo "🔍 检查字段格式..."

# 检查是否有驼峰格式字段（排除变量名，只检查put方法中的字符串字面量）
ERROR_COUNT=$(grep -r "put(\"[^\"]*[A-Z][a-z]" backend-java/src/main/java/com/cco/controller/ 2>/dev/null | \
  grep -E "put\(\"(fieldKey|fieldName|fieldType|fieldGroupId|isRequired|sortOrder|tenantCode|tenantName|countryCode|currencyCode|groupKey|groupName|parentId|createdAt|updatedAt|agencyCode|agencyName|teamCode|teamName|queueCode|queueName)" | \
  grep -v "//" | \
  wc -l | tr -d ' ')

if [ "$ERROR_COUNT" -eq 0 ]; then
    echo "✅ 未发现驼峰格式字段"
    exit 0
else
    echo "❌ 发现 $ERROR_COUNT 处驼峰格式字段，请修复："
    echo ""
    grep -r "put(\"[^\"]*[A-Z][a-z]" backend-java/src/main/java/com/cco/controller/ 2>/dev/null | \
      grep -E "put\(\"(fieldKey|fieldName|fieldType|fieldGroupId|isRequired|sortOrder|tenantCode|tenantName|countryCode|currencyCode|groupKey|groupName|parentId|createdAt|updatedAt|agencyCode|agencyName|teamCode|teamName|queueCode|queueName)" | \
      grep -v "//"
    echo ""
    echo "请参考开发规范文档：说明文档/开发规范/API接口开发规范.md"
    exit 1
fi

