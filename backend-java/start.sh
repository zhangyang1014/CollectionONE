#!/bin/bash
# Java后端启动脚本

# 设置Java 17 - 使用固定路径
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH=$JAVA_HOME/bin:$PATH

echo "使用Java版本:"
$JAVA_HOME/bin/java -version

cd "$(dirname "$0")"

echo ""
echo "正在启动Java后端..."
echo ""

# 使用Java 17编译并启动
JAVA_HOME=/opt/homebrew/opt/openjdk@17 PATH=$JAVA_HOME/bin:$PATH mvn spring-boot:run -Dspring-boot.run.profiles=dev


