#!/bin/bash
# Java后端重启脚本

echo "正在停止Java后端服务..."

# 查找并停止所有运行在8080端口的Java进程
PIDS=$(lsof -ti :8080)
if [ -n "$PIDS" ]; then
    echo "找到运行中的进程: $PIDS"
    kill -9 $PIDS
    echo "已停止所有Java后端进程"
    sleep 2
else
    echo "没有找到运行中的Java后端进程"
fi

echo ""
echo "正在启动Java后端..."
echo ""

# 设置Java 17 - 使用固定路径
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH=$JAVA_HOME/bin:$PATH

cd "$(dirname "$0")"

# 使用Java 17启动
JAVA_HOME=/opt/homebrew/opt/openjdk@17 PATH=$JAVA_HOME/bin:$PATH mvn spring-boot:run -Dspring-boot.run.profiles=dev

