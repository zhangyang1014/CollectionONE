# Java Spring Boot 实施指南

## 开发人员指南

### 快速添加新模块

#### 1. 创建实体类

```java
package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("your_table_name")
public class YourEntity extends BaseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String fieldName;
    
    // 其他字段...
}
```

#### 2. 创建 Mapper 接口

```java
package com.cco.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cco.model.entity.YourEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface YourEntityMapper extends BaseMapper<YourEntity> {
    // MyBatis Plus 自动提供基础CRUD方法
    // 如需自定义SQL，在这里添加方法声明
}
```

#### 3. 创建 Service 接口

```java
package com.cco.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cco.model.entity.YourEntity;

public interface IYourEntityService extends IService<YourEntity> {
    // 添加自定义业务方法
}
```

#### 4. 创建 Service 实现

```java
package com.cco.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cco.mapper.YourEntityMapper;
import com.cco.model.entity.YourEntity;
import com.cco.service.IYourEntityService;
import org.springframework.stereotype.Service;

@Service
public class YourEntityServiceImpl 
        extends ServiceImpl<YourEntityMapper, YourEntity> 
        implements IYourEntityService {
    
    // 实现业务逻辑
}
```

#### 5. 创建 Controller

```java
package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.entity.YourEntity;
import com.cco.service.IYourEntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/your-path")
public class YourEntityController {

    @Autowired
    private IYourEntityService yourEntityService;

    @GetMapping
    public ResponseData<List<YourEntity>> list() {
        return ResponseData.success(yourEntityService.list());
    }

    @GetMapping("/{id}")
    public ResponseData<YourEntity> getById(@PathVariable Long id) {
        return ResponseData.success(yourEntityService.getById(id));
    }

    @PostMapping
    public ResponseData<YourEntity> create(@RequestBody YourEntity entity) {
        yourEntityService.save(entity);
        return ResponseData.success("创建成功", entity);
    }

    @PutMapping("/{id}")
    public ResponseData<YourEntity> update(@PathVariable Long id, @RequestBody YourEntity entity) {
        entity.setId(id);
        yourEntityService.updateById(entity);
        return ResponseData.success("更新成功", entity);
    }

    @DeleteMapping("/{id}")
    public ResponseData<Void> delete(@PathVariable Long id) {
        yourEntityService.removeById(id);
        return ResponseData.success("删除成功", null);
    }
}
```

### 常用 MyBatis Plus 操作

#### 查询示例

```java
// 查询所有
List<Entity> list = service.list();

// 条件查询
LambdaQueryWrapper<Entity> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Entity::getFieldName, value)
       .like(Entity::getName, keyword)
       .orderByAsc(Entity::getSortOrder);
List<Entity> list = service.list(wrapper);

// 分页查询
Page<Entity> page = new Page<>(pageNum, pageSize);
Page<Entity> result = service.page(page, wrapper);

// 查询单个
Entity entity = service.getOne(wrapper);
```

#### 更新示例

```java
// 根据ID更新
Entity entity = new Entity();
entity.setId(id);
entity.setFieldName(value);
service.updateById(entity);

// 条件更新
LambdaUpdateWrapper<Entity> wrapper = new LambdaUpdateWrapper<>();
wrapper.eq(Entity::getId, id)
       .set(Entity::getFieldName, value);
service.update(wrapper);
```

#### 删除示例

```java
// 根据ID删除
service.removeById(id);

// 批量删除
service.removeByIds(Arrays.asList(1L, 2L, 3L));

// 条件删除
LambdaQueryWrapper<Entity> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Entity::getFieldName, value);
service.remove(wrapper);
```

### 事务管理

```java
@Service
public class YourService {
    
    @Transactional(rollbackFor = Exception.class)
    public void complexOperation() {
        // 多个数据库操作
        // 如果抛出异常，所有操作都会回滚
    }
}
```

### 异常处理

```java
// 业务异常
throw new BusinessException("错误信息");
throw new BusinessException(ResponseCode.DATA_NOT_FOUND);

// 全局异常处理器会自动捕获并返回统一格式
```

### 权限控制

```java
// 方法级权限控制
@PreAuthorize("hasRole('SUPER_ADMIN')")
@GetMapping("/admin-only")
public ResponseData<?> adminOnly() {
    // 只有SuperAdmin可以访问
}

// 多角色
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
@GetMapping("/admin-access")
public ResponseData<?> adminAccess() {
    // SuperAdmin和TenantAdmin都可以访问
}
```

## 测试指南

### 单元测试

```java
@SpringBootTest
class YourServiceTest {
    
    @Autowired
    private IYourEntityService service;
    
    @Test
    void testCreate() {
        YourEntity entity = new YourEntity();
        entity.setFieldName("test");
        service.save(entity);
        assertNotNull(entity.getId());
    }
}
```

### 集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class YourControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testList() throws Exception {
        mockMvc.perform(get("/api/v1/your-path"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.code").value(200));
    }
}
```

## 性能优化建议

### 1. 查询优化

- 使用索引
- 避免SELECT *
- 使用分页查询
- 合理使用缓存

### 2. 批量操作

```java
// 批量插入
service.saveBatch(entityList);

// 批量更新
service.updateBatchById(entityList);
```

### 3. 异步处理

```java
@Async
public CompletableFuture<Result> asyncTask() {
    // 异步执行的任务
    return CompletableFuture.completedFuture(result);
}
```

### 4. 缓存使用

```java
@Cacheable(value = "entity", key = "#id")
public Entity getById(Long id) {
    return baseMapper.selectById(id);
}

@CacheEvict(value = "entity", key = "#id")
public boolean deleteById(Long id) {
    return baseMapper.deleteById(id) > 0;
}
```

## 常见问题

### Q: 如何处理循环依赖？
A: 使用 `@Lazy` 注解或重构代码结构。

### Q: 如何处理大文件上传？
A: 配置 `spring.servlet.multipart.max-file-size` 和使用流式处理。

### Q: 如何实现软删除？
A: 使用 MyBatis Plus 的逻辑删除功能，在实体类添加 `@TableLogic` 注解。

### Q: 如何处理JSON字段？
A: 使用 `@TableField(typeHandler = JacksonTypeHandler.class)` 注解。

---

**持续更新中...**

