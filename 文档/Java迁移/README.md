# CCO Backend System - Java Spring Boot 实现

这是 CCO 催收系统后端的 Java Spring Boot 实现，从 Python FastAPI 完全重写而来。

## 技术栈

- **Java 17+**
- **Spring Boot 3.2.0**
- **MyBatis Plus 3.5.5** - ORM 框架
- **MySQL 8.0+** - 数据库
- **Spring Security + JWT** - 认证授权
- **Maven** - 构建工具
- **Lombok** - 减少样板代码

## 项目结构

```
backend-java/
├── src/main/
│   ├── java/com/cco/
│   │   ├── CcoApplication.java          # 主应用入口
│   │   ├── common/                       # 公共模块
│   │   │   ├── config/                   # 配置类
│   │   │   │   ├── CorsConfig.java       # 跨域配置
│   │   │   │   ├── MyBatisConfig.java    # MyBatis 配置
│   │   │   │   ├── SecurityConfig.java   # 安全配置
│   │   │   │   └── WebConfig.java        # Web 配置
│   │   │   ├── constant/                 # 常量定义
│   │   │   ├── exception/                # 异常处理
│   │   │   └── response/                 # 统一响应
│   │   ├── model/                        # 数据模型
│   │   │   ├── entity/                   # 实体类
│   │   │   ├── dto/                      # 数据传输对象
│   │   │   └── vo/                       # 视图对象
│   │   ├── mapper/                       # MyBatis Mapper
│   │   ├── service/                      # 业务逻辑
│   │   ├── controller/                   # 控制器
│   │   └── security/                     # 安全相关
│   └── resources/
│       ├── application.yml               # 主配置文件
│       ├── application-dev.yml           # 开发环境配置
│       ├── application-prod.yml          # 生产环境配置
│       └── mapper/                       # MyBatis XML
└── src/test/                             # 测试代码
```

## 快速开始

### 前置要求

1. **Java 17 或更高版本**
   ```bash
   java -version
   ```

2. **Maven 3.8+**
   ```bash
   mvn -version
   ```

3. **MySQL 8.0+**
   - 创建数据库: `cco_system`
   - 配置用户名和密码（默认：root/root）

### 安装依赖

```bash
cd backend-java
mvn clean install
```

### 配置数据库

修改 `src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cco_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

### 运行应用

```bash
mvn spring-boot:run
```

或者使用 IDE (IntelliJ IDEA / Eclipse) 直接运行 `CcoApplication.java`

应用将在 `http://localhost:8080` 启动

## API 文档

所有 API 都以 `/api/v1` 为前缀，与原 Python 后端保持一致。

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 健康检查

```bash
curl http://localhost:8080/health
```

## 开发指南

### 添加新的实体类

1. 在 `model/entity` 包下创建实体类
2. 使用 `@TableName` 注解指定表名
3. 使用 Lombok 注解减少代码

```java
@Data
@TableName("tenants")
public class Tenant {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String tenantCode;
    private String tenantName;
    // ...
}
```

### 添加新的 API

1. 创建 Mapper 接口（继承 `BaseMapper`）
2. 创建 Service 接口和实现类
3. 创建 Controller 类

```java
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/tenants")
public class TenantController {
    @Autowired
    private ITenantService tenantService;
    
    @GetMapping
    public ResponseData<List<Tenant>> list() {
        return ResponseData.success(tenantService.list());
    }
}
```

## 与 Python 后端的兼容性

- ✅ API 路径完全一致
- ✅ 请求/响应格式一致
- ✅ 日期时间格式一致（ISO 8601）
- ✅ JWT Token 格式兼容
- ✅ 分页参数兼容
- ✅ 错误响应格式一致

## 部署

### 打包

```bash
mvn clean package -DskipTests
```

生成的 JAR 文件位于 `target/cco-backend-1.0.0.jar`

### 运行

```bash
java -jar target/cco-backend-1.0.0.jar --spring.profiles.active=prod
```

### Docker 部署

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/cco-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 测试

```bash
mvn test
```

## 迁移进度

- [x] 项目骨架搭建
- [x] 统一响应格式
- [x] 全局异常处理
- [x] CORS 配置
- [ ] 数据库表迁移
- [ ] 认证授权模块
- [ ] 字段管理模块
- [ ] 甲方组织架构模块
- [ ] 案件队列管理模块
- [ ] 通知系统模块
- [ ] 数据看板模块

## 许可证

Copyright © 2025 CCO Team

