# 快速启动Java后端

## 🚀 一键启动

```bash
cd /Users/zhangyang/Documents/GitHub/CollectionONE/backend-java

# 方法1: 使用启动脚本（推荐）
./start.sh

# 方法2: 手动指定Java 17
JAVA_HOME=/opt/homebrew/opt/openjdk@17 \
PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH \
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 验证启动成功

```bash
# 等待20秒后测试
sleep 20

# 测试API
curl http://localhost:8080/api/v1/cases?tenantId=1
curl "http://localhost:8080/api/v1/field-display-configs?tenantId=1&sceneType=collector_case_list"
```

## 前端已自动配置

前端已配置为连接 `http://localhost:8080`，启动Java后端后前端即可正常工作，404错误将永久消失。

## 详细文档

查看 `说明文档/后端/永久修复404错误-完整方案.md`


