# 下一步实施计划

## 🎯 当前完成情况

✅ **已完成（约25%）**:
- 完整的Spring Boot项目架构
- Maven依赖配置
- MySQL数据库设计（核心表）
- 20+个Java实体类
- 统一响应格式和异常处理
- CORS跨域配置
- Spring Security + JWT认证
- 认证API（登录/登出）
- 标准字段管理基础API

## 📋 剩余工作清单

### 1. 甲方和组织架构模块（预计8人天）

需要实现的文件：

#### Mapper接口
```java
- TenantMapper.java
- CollectionAgencyMapper.java
- CollectionTeamMapper.java
- TeamGroupMapper.java
- CollectorMapper.java (基础已有，需要完善)
```

#### Service接口和实现
```java
- ITenantService.java + TenantServiceImpl.java
- ICollectionAgencyService.java + CollectionAgencyServiceImpl.java
- ICollectionTeamService.java + CollectionTeamServiceImpl.java
- ITeamGroupService.java + TeamGroupServiceImpl.java
- ICollectorService.java + CollectorServiceImpl.java (基础已有，需要完善)
```

#### Controller
```java
- TenantController.java
- AgencyController.java
- TeamController.java
- TeamGroupController.java
- CollectorController.java
```

#### 参考Python文件
- `backend/app/api/tenants.py`
- `backend/app/api/agencies.py`
- `backend/app/api/teams.py`
- `backend/app/api/team_groups.py`

### 2. 案件和队列管理模块（预计10人天）

需要实现的文件：

#### Mapper接口
```java
- CaseMapper.java (基础已有，需要完善)
- CaseQueueMapper.java (基础已有，需要完善)
- CaseStandardFieldValueMapper.java
- CaseCustomFieldValueMapper.java
- CaseAssignmentHistoryMapper.java
- CaseContactMapper.java
```

#### Service和Controller
```java
- ICaseService.java + CaseServiceImpl.java + CaseController.java
- ICaseQueueService.java + CaseQueueServiceImpl.java + CaseQueueController.java
- 案件字段值相关Service
- 案件分配逻辑Service
```

#### 复杂业务逻辑
- 案件自动分配到队列（根据逾期天数）
- 案件分配给催员
- 案件搜索和筛选
- 案件详情查询（包含字段值）

#### 参考Python文件
- `backend/app/api/cases.py`
- `backend/app/api/channel.py`

### 3. 通知系统模块（预计5人天）

需要实现的文件：

#### Mapper、Service、Controller
```java
- NotificationTemplateMapper.java + Service + Controller (实体已有)
- NotificationConfigMapper.java + Service + Controller (实体已有)
- PublicNotificationMapper.java + Service + Controller (实体已有)
```

#### 参考Python文件
- `backend/app/api/notification_template.py`
- `backend/app/api/notification_config.py`
- `backend/app/api/public_notification.py`

### 4. 数据看板模块（预计12人天）

需要创建的实体类：
```java
- CommunicationRecord.java
- PTPRecord.java
- QualityInspectionRecord.java
- CollectorPerformanceStat.java
- CustomDimensionStat.java
- CollectorIdleRecord.java
- IdleMonitorConfig.java
```

需要实现的API：
```java
- CommunicationController.java (沟通记录统计)
- PtpController.java (PTP统计)
- QualityInspectionController.java (质检统计)
- PerformanceController.java (绩效统计)
- AnalyticsController.java (分析统计)
- AlertController.java (预警统计)
- IdleMonitorController.java (空闲催员监控)
```

#### 参考Python文件
- `backend/app/api/communications.py`
- `backend/app/api/ptp.py`
- `backend/app/api/quality_inspections.py`
- `backend/app/api/performance.py`
- `backend/app/api/analytics.py`
- `backend/app/api/alerts.py`
- `backend/app/api/idle_monitor.py`

### 5. IM端功能（预计3人天）

需要实现的API：
```java
- ImAuthController.java (IM端认证)
- FaceRecognitionController.java (人脸识别)
```

#### 参考Python文件
- `backend/app/api/im_auth.py`
- `backend/app/api/im_face.py`

### 6. 测试和优化（预计10人天）

- 单元测试（Service层）
- 集成测试（Controller层）
- API兼容性测试（与前端对接）
- 性能测试和优化
- 代码审查和重构

## 🔨 实施方法

### 标准实施流程（每个模块）

#### 步骤1: 创建或完善实体类
```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("table_name")
public class Entity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    // 其他字段...
}
```

#### 步骤2: 创建Mapper接口
```java
@Mapper
public interface EntityMapper extends BaseMapper<Entity> {
    // MyBatis Plus提供基础CRUD
    // 如需自定义SQL，在这里添加方法
}
```

#### 步骤3: 创建Service接口
```java
public interface IEntityService extends IService<Entity> {
    // 定义业务方法
}
```

#### 步骤4: 创建Service实现
```java
@Service
public class EntityServiceImpl 
        extends ServiceImpl<EntityMapper, Entity> 
        implements IEntityService {
    // 实现业务逻辑
}
```

#### 步骤5: 创建Controller
```java
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/path")
public class EntityController {
    
    @Autowired
    private IEntityService entityService;
    
    @GetMapping
    public ResponseData<List<Entity>> list() {
        return ResponseData.success(entityService.list());
    }
    
    // 其他CRUD方法...
}
```

#### 步骤6: 对照Python代码验证
1. 检查API路径是否一致
2. 检查请求参数格式
3. 检查响应数据结构
4. 验证业务逻辑

#### 步骤7: 编写测试
```java
@SpringBootTest
class EntityServiceTest {
    @Autowired
    private IEntityService service;
    
    @Test
    void testList() {
        List<Entity> list = service.list();
        assertNotNull(list);
    }
}
```

## 📊 工作量估算

| 任务 | 工作量 | 说明 |
|------|--------|------|
| 甲方和组织架构 | 8人天 | 5个模块，每个约1.5人天 |
| 案件和队列管理 | 10人天 | 复杂业务逻辑较多 |
| 通知系统 | 5人天 | 3个模块，相对简单 |
| 数据看板 | 12人天 | 7个模块，统计逻辑复杂 |
| IM端功能 | 3人天 | 2个模块 |
| 完善字段管理 | 2人天 | 自定义字段、字段分组 |
| 测试和优化 | 10人天 | 全面测试 |
| **总计** | **50人天** | 约2-3个月（2-3人团队） |

## 🎯 里程碑

### Milestone 1: 基础功能完整（Week 1-4）
- ✅ 项目架构（已完成）
- ✅ 认证授权（已完成）
- [ ] 字段管理完整实现
- [ ] 甲方和组织架构模块

### Milestone 2: 核心业务功能（Week 5-8）
- [ ] 案件管理
- [ ] 队列管理
- [ ] 催员管理
- [ ] 通知系统

### Milestone 3: 数据看板和测试（Week 9-12）
- [ ] 数据看板7个子模块
- [ ] IM端功能
- [ ] 全面测试
- [ ] 性能优化

## 🚀 快速上手

### 从最简单的模块开始

推荐从 **甲方管理（Tenant）** 开始，因为：
1. 业务逻辑简单
2. 没有复杂关联
3. CRUD操作标准
4. 可作为其他模块的模板

### 实施Tenant模块示例

1. **创建Mapper**（已有实体类Tenant.java）
```java
@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {
}
```

2. **创建Service**
```java
public interface ITenantService extends IService<Tenant> {
    List<Tenant> listActive();
}

@Service
public class TenantServiceImpl extends ServiceImpl<TenantMapper, Tenant> implements ITenantService {
    @Override
    public List<Tenant> listActive() {
        return list(new LambdaQueryWrapper<Tenant>().eq(Tenant::getIsActive, true));
    }
}
```

3. **创建Controller**
```java
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/tenants")
public class TenantController {
    @Autowired
    private ITenantService tenantService;
    
    @GetMapping
    public ResponseData<List<Tenant>> list() {
        return ResponseData.success(tenantService.listActive());
    }
    
    // 参考 backend/app/api/tenants.py 实现其他方法
}
```

4. **测试**
```bash
curl http://localhost:8080/api/v1/tenants \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 📚 参考资源

- **已完成代码**: 参考 `StandardFieldController.java`
- **Python原始代码**: `backend/app/api/` 目录
- **实体类**: `backend-java/src/main/java/com/cco/model/entity/`
- **实施指南**: `backend-java/IMPLEMENTATION_GUIDE.md`

## ⚠️ 注意事项

1. **API路径必须与Python版本一致**
2. **响应格式必须使用 `ResponseData<T>`**
3. **日期时间格式: `yyyy-MM-dd HH:mm:ss`**
4. **所有接口需要JWT认证（除了登录接口）**
5. **使用 `@Transactional` 确保事务一致性**
6. **查询尽量使用Lambda条件构造器**
7. **避免N+1查询问题**

## 📞 支持

遇到问题请参考：
1. `README.md` - 项目说明
2. `IMPLEMENTATION_GUIDE.md` - 实施指南
3. `MIGRATION_SUMMARY.md` - 迁移总结
4. Python原始代码 - `backend/app/api/`

---

**开始实施**: 建议从Tenant模块开始  
**估计完成时间**: 2-3个月（2-3人团队）  
**当前进度**: ~25%

