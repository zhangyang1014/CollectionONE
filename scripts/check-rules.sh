#!/bin/bash
# CollectionONE 项目规则检查脚本
# 在每次提交前运行此脚本确保代码符合规范

set -e

echo "🔍 CollectionONE 项目规则检查"
echo "================================"

ERRORS=0

# 1. 检查硬编码的8000端口
echo ""
echo "📍 检查1: 前端是否有硬编码的8000端口..."
HARDCODED_8000=$(grep -r "localhost:8000" frontend/src --include="*.ts" --include="*.vue" 2>/dev/null | wc -l | tr -d ' ')
if [ "$HARDCODED_8000" -gt 0 ]; then
  echo "❌ 发现硬编码的localhost:8000！"
  grep -r "localhost:8000" frontend/src --include="*.ts" --include="*.vue"
  ERRORS=$((ERRORS + 1))
else
  echo "✅ 通过：没有硬编码的8000端口"
fi

# 2. 检查硬编码的8080端口（除了config/api.ts）
echo ""
echo "📍 检查2: 前端是否有不当的硬编码8080端口..."
HARDCODED_8080=$(grep -r "localhost:8080" frontend/src --include="*.ts" --include="*.vue" | grep -v "config/api.ts" | grep -v "utils/request.ts" 2>/dev/null | wc -l | tr -d ' ')
if [ "$HARDCODED_8080" -gt 5 ]; then
  echo "⚠️  警告：发现多处硬编码的localhost:8080（建议使用@/config/api）"
  grep -r "localhost:8080" frontend/src --include="*.ts" --include="*.vue" | grep -v "config/api.ts" | grep -v "utils/request.ts" | head -5
else
  echo "✅ 通过：8080端口使用合理"
fi

# 3. 检查.bak备份文件
echo ""
echo "📍 检查3: 是否有.bak备份文件..."
BAK_FILES=$(find . -name "*.bak" 2>/dev/null | wc -l | tr -d ' ')
if [ "$BAK_FILES" -gt 0 ]; then
  echo "❌ 发现.bak备份文件（应该删除）："
  find . -name "*.bak"
  ERRORS=$((ERRORS + 1))
else
  echo "✅ 通过：没有.bak备份文件"
fi

# 4. 检查Java后端是否运行
echo ""
echo "📍 检查4: Java后端是否运行..."
JAVA_RUNNING=$(lsof -i :8080 2>/dev/null | grep LISTEN | wc -l | tr -d ' ')
if [ "$JAVA_RUNNING" -gt 0 ]; then
  echo "✅ 通过：Java后端正在运行（端口8080）"
else
  echo "⚠️  警告：Java后端未运行（需要启动：cd backend-java && ./start.sh）"
fi

# 5. 检查Python后端是否误启动
echo ""
echo "📍 检查5: Python后端是否误启动..."
PYTHON_RUNNING=$(lsof -i :8000 2>/dev/null | grep LISTEN | wc -l | tr -d ' ')
if [ "$PYTHON_RUNNING" -gt 0 ]; then
  echo "❌ Python后端正在运行（应该停止）："
  lsof -i :8000
  ERRORS=$((ERRORS + 1))
else
  echo "✅ 通过：Python后端未运行"
fi

# 6. 检查Java版本
echo ""
echo "📍 检查6: Java版本是否正确..."
if command -v java &> /dev/null; then
  JAVA_VERSION=$(java -version 2>&1 | grep "version" | awk '{print $3}' | tr -d '"')
  if [[ $JAVA_VERSION == 17.* ]]; then
    echo "✅ 通过：Java版本正确（$JAVA_VERSION）"
  else
    echo "⚠️  警告：Java版本不是17（当前：$JAVA_VERSION）"
    echo "   建议：export JAVA_HOME=/opt/homebrew/opt/openjdk@17"
  fi
else
  echo "⚠️  警告：未找到Java"
fi

# 7. 检查frontend/src/config/api.ts是否存在
echo ""
echo "📍 检查7: API配置文件是否存在..."
if [ -f "frontend/src/config/api.ts" ]; then
  echo "✅ 通过：API配置文件存在"
else
  echo "❌ API配置文件不存在（应该创建frontend/src/config/api.ts）"
  ERRORS=$((ERRORS + 1))
fi

# 8. 检查Token过期处理（修复"案件不见了"问题）
echo ""
echo "📍 检查8: Token过期处理是否正确..."
if [ -f "backend-java/src/main/java/com/cco/security/JwtAuthenticationFilter.java" ]; then
  if grep -q "SC_UNAUTHORIZED" backend-java/src/main/java/com/cco/security/JwtAuthenticationFilter.java; then
    echo "✅ 通过：Token过期处理已实现（返回401）"
  else
    echo "⚠️  警告：Token过期未正确返回401"
    echo "   可能导致：催员案件列表为空，用户不知道原因"
  fi
else
  echo "⚠️  警告：未找到JwtAuthenticationFilter.java"
fi

# 总结
echo ""
echo "================================"
if [ $ERRORS -eq 0 ]; then
  echo "✅ 所有检查通过！代码符合项目规范。"
  exit 0
else
  echo "❌ 发现 $ERRORS 个错误，请修复后再提交。"
  exit 1
fi

