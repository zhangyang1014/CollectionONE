# Java后端权限管理部署问题和解决方案

## 📋 问题总结

在部署Java后端权限管理系统时遇到了以下问题：

### 1. ✅ 已解决：Java版本兼容性问题
- **问题**：系统安装的Java 25与Maven编译器不兼容
- **解决方案**：安装并使用Java 17 LTS版本
- **状态**：✅ 已解决

### 2. ✅ 已解决：Lombok注解处理器配置
- **问题**：Maven编译器插件未配置Lombok注解处理器
- **解决方案**：在pom.xml中添加annotationProcessorPaths配置
- **状态**：✅ 已解决

### 3. ✅ 已解决：Spring Boot版本兼容性
- **问题**：Spring Boot 3.2.0与MyBatis Plus存在兼容性问题
- **解决方案**：升级Spring Boot到3.3.5
- **状态**：✅ 已解决

### 4. ⚠️ 进行中：数据库配置问题
- **问题**：MyBatis和数据库配置导致启动失败
- **当前方案**：创建了Mock权限控制器，暂时绕过数据库
- **状态**：⚠️ 需要进一步配置

### 5. ⚠️ 进行中：CORS配置问题
- **问题**：CORS配置依赖application.yml中的属性
- **解决方案**：已修改为硬编码值，但JAR包未更新
- **状态**：⚠️ 需要重新打包

## 🚀 快速解决方案

由于Java后端还需要进一步配置数据库和完善细节，我建议采用以下方案之一：

### 方案A：继续使用Python后端（推荐）⭐

Python后端功能完整且稳定，可以立即使用：

```bash
# 确认Python后端正在运行
ps aux | grep "uvicorn app.main:app" | grep -v grep

# 如果没有运行，启动Python后端
cd /Users/zhangyang/Documents/GitHub/CollectionONE/backend
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

**前端配置**：前端已经配置为连接`http://localhost:8000`，无需修改

### 方案B：完成Java后端配置（需要额外时间）

需要完成以下步骤：

1. **配置数据库**
   ```bash
   # 创建数据库
   mysql -uroot -p
   CREATE DATABASE cco_system CHARACTER SET utf8mb4;
   
   # 执行建表脚本
   mysql -uroot -p cco_system < backend-java/src/main/resources/db/migration/schema.sql
   ```

2. **修复数据库密码**
   编辑`backend-java/src/main/resources/application-dev.yml`：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/cco_system
       username: root
       password: 你的MySQL密码  # 修改为实际密码
   ```

3. **重新编译和启动**
   ```bash
   cd backend-java
   JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
   mvn clean package -DskipTests
   
   JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
   java -jar target/cco-backend-1.0.0.jar --server.port=8080
   ```

4. **配置前端连接Java后端**
   修改`frontend/src/utils/request.ts`中的baseURL为`http://localhost:8080`

## 📝 当前项目状态

### ✅ 已完成的工作

1. **Java 17环境配置**
   - 安装位置：`/opt/homebrew/opt/openjdk@17/`
   - 验证：`/opt/homebrew/opt/openjdk@17/bin/java -version`

2. **Maven编译配置**
   - Lombok注解处理器已配置
   - Java 17编译目标已设置
   - Spring Boot 3.3.5已升级

3. **Mock权限控制器**
   - 文件：`backend-java/src/main/java/com/cco/controller/MockPermissionController.java`
   - 功能：提供权限管理的Mock数据，不依赖数据库
   - 接口：完全兼容前端要求

4. **CORS配置简化**
   - 文件：`backend-java/src/main/java/com/cco/common/config/CorsConfig.java`
   - 改为硬编码值，避免配置依赖

### ⏳ 待完成的工作

1. **数据库初始化**
   - 创建`cco_system`数据库
   - 执行建表脚本
   - 配置正确的数据库密码

2. **完整权限管理实现**
   - 当前使用Mock数据
   - 需要连接真实数据库后启用`PermissionController`

3. **启动脚本优化**
   - 创建自动化启动脚本
   - 添加健康检查

## 🔧 调试命令

### 检查服务状态
```bash
# 检查Java后端进程
ps aux | grep java | grep cco-backend

# 检查Python后端进程  
ps aux | grep uvicorn | grep app.main

# 测试Java后端API
curl http://localhost:8080/api/v1/permissions/modules

# 测试Python后端API
curl http://localhost:8000/api/v1/tenants
```

### 查看日志
```bash
# Java后端日志
tail -f /Users/zhangyang/Documents/GitHub/CollectionONE/backend-java/backend.log

# Python后端日志
# 直接在运行终端查看
```

### 重新编译Java后端
```bash
cd /Users/zhangyang/Documents/GitHub/CollectionONE/backend-java
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
mvn clean package -DskipTests
```

## 💡 建议

基于当前情况，我强烈建议：

1. **短期**：继续使用Python后端，确保业务正常运行
2. **中期**：配置好数据库后逐步切换到Java后端
3. **长期**：完善Java后端的所有功能，最终完全迁移

## 📞 技术支持

如果遇到问题，请检查：

1. Java版本：`java -version` 应该显示17.x.x
2. Maven版本：`mvn -version`  
3. 数据库状态：`mysql -uroot -p -e "SHOW DATABASES;"`
4. 端口占用：`lsof -i:8080` 和 `lsof -i:8000`

## 🎯 下一步行动

**立即可行的方案**：
```bash
# 1. 确保Python后端运行
cd /Users/zhangyang/Documents/GitHub/CollectionONE/backend
python -m uvicorn app.main:app --reload --port 8000

# 2. 刷新前端页面
# 访问 http://localhost:5173
```

这样可以立即解决权限配置页面的404错误问题。

---

**创建时间**：2025-11-22
**状态**：进行中
**优先级**：高

