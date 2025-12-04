# 智能Chatting状态判断优化功能 PRD

## 一、产品需求（Product Requirements）

### 1. 项目背景与目标（Background & Goals）

智能Chatting状态判断优化是WhatsApp信息收发功能的重要性能优化，通过引入三级缓存机制（wa_relation表缓存 → message表历史记录 → CWA API实时查询），大幅减少对CWA checkPhone接口的调用频率，降低WhatsApp官方服务端风控风险，同时提升系统性能。

**业务痛点**：
- 每次点击联系人都调用CWA checkPhone接口，导致频繁查询
- 触发WhatsApp官方服务端风控，可能被限制API调用
- 查询响应慢，影响用户体验
- 大量重复查询相同手机号，浪费资源

**预期影响的核心指标**：
- CWA API调用减少率：≥90%
- Chatting状态缓存命中率：≥85%
- 查询响应时间：≤500ms（缓存命中）
- WhatsApp风控风险：显著降低

---

### 2. 业务场景与用户画像（Business Scenario & User）

#### 2.1 典型使用场景

**场景1：首次点击联系人（无缓存）**
- **入口**：催员工作台 → 选择联系人（本人）
- **触发时机**：催员首次点击该联系人，系统无缓存数据
- **所在页面**：IM面板 - 联系人列表
- **流程节点**：
  1. 点击联系人（本人）
  2. 执行查询1：查询`wa_relation`表缓存 → 未命中
  3. 执行查询2：查询`message`表历史回复记录 → 未命中
  4. 执行查询3：调用CWA checkPhone接口 → 获取结果
  5. 将结果存入`wa_relation`表（缓存）
  6. 返回Chatting状态
- **响应时间**：约1-2秒（需调用CWA API）

**场景2：7天内重复点击联系人（缓存命中）**
- **入口**：催员工作台 → 选择联系人（本人）
- **触发时机**：7天内再次点击同一联系人
- **所在页面**：IM面板 - 联系人列表
- **流程节点**：
  1. 点击联系人（本人）
  2. 执行查询1：查询`wa_relation`表缓存 → 命中（7天内记录）
  3. 直接使用缓存的Chatting状态
  4. 更新`last_used_at`字段（记录最后使用时间）
  5. 返回Chatting状态
- **响应时间**：约100-200ms（数据库查询）
- **优化效果**：不调用CWA API，减少API调用

**场景3：有消息往来的联系人（消息记录命中）**
- **入口**：催员工作台 → 选择联系人（本人）
- **触发时机**：7天内该联系人通过WhatsApp回复过消息
- **所在页面**：IM面板 - 联系人列表
- **流程节点**：
  1. 点击联系人（本人）
  2. 执行查询1：查询`wa_relation`表缓存 → 未命中（超过7天或无记录）
  3. 执行查询2：查询`message`表历史回复记录 → 命中（7天内有WhatsApp回复）
  4. 判定为Chatting状态（有回复记录，说明已注册）
  5. 在`wa_relation`表中插入新记录（source='message_history'）
  6. 返回Chatting状态
- **响应时间**：约150-300ms（数据库查询）
- **优化效果**：不调用CWA API，利用历史数据自动建立缓存

**场景4：跨甲方缓存复用**
- **入口**：催员工作台 → 选择联系人（本人）
- **触发时机**：其他甲方条线已查询过该手机号
- **所在页面**：IM面板 - 联系人列表
- **流程节点**：
  1. 点击联系人（本人）
  2. 执行查询1：查询`wa_relation`表缓存（允许跨甲方，tenant_id = NULL）→ 命中
  3. 直接使用跨甲方缓存数据
  4. 返回Chatting状态
- **优化效果**：全局缓存，最大化缓存命中率

#### 2.2 主要用户类型

| 用户类型 | 角色标识 | 核心诉求 | 使用场景 |
|---------|---------|---------|---------|
| 催员 | Collector | 快速获取联系人WhatsApp注册状态 | 日常催收沟通 |

---

### 3. 关键业务流程（Business Flow）

#### 3.1 智能Chatting状态判断流程（三级缓存）

```
催员点击联系人（本人）
    ↓
触发Chatting状态判断
    ↓
==== 查询1：检查wa_relation表缓存（第一级缓存）====
    ↓
SQL查询：
SELECT * FROM wa_relation
WHERE phone_number = '{联系人手机号}'
  AND status = 'chatting'
  AND DATEDIFF(NOW(), checked_at) <= 7
  AND tenant_id IS NULL  -- 允许跨甲方条线查询
LIMIT 1
    ↓
[命中缓存]
    → 直接使用缓存的chatting状态
    → 更新last_used_at字段：
       UPDATE wa_relation 
       SET last_used_at = NOW() 
       WHERE id = {record_id}
    → 返回数据：
       {
         "phoneNumber": "+919876543210",
         "status": "chatting",
         "source": "cache",
         "checkedAt": "2025-01-18T10:30:25Z",
         "cacheAge": 2  // 缓存天数
       }
    → 结束流程，不再继续查询
    ↓
[未命中缓存] → 继续查询2
    ↓
==== 查询2：检查message表历史回复记录（第二级缓存）====
    ↓
SQL查询：
SELECT * FROM messages
WHERE contact_phone_number = '{联系人手机号}'
  AND channel = 'whatsapp'
  AND sender_type = 'customer'  -- 客户回复的消息
  AND DATEDIFF(NOW(), sent_at) <= 7
ORDER BY sent_at DESC
LIMIT 1
    ↓
[命中消息记录]
    → 判定为chatting状态（有WhatsApp回复记录，说明已注册）
    → 在wa_relation表中插入新记录：
       INSERT INTO wa_relation (
         phone_number,
         status,
         checked_at,
         last_used_at,
         source,
         tenant_id
       ) VALUES (
         '{联系人手机号}',
         'chatting',
         NOW(),
         NOW(),
         'message_history',
         NULL  -- 跨甲方缓存
       )
    → 返回数据：
       {
         "phoneNumber": "+919876543210",
         "status": "chatting",
         "source": "message_history",
         "lastReplyAt": "2025-01-19T15:20:10Z"
       }
    → 结束流程
    ↓
[未命中消息记录] → 继续查询3
    ↓
==== 查询3：调用CWA checkPhone接口（第三级，实时查询）====
    ↓
调用API：POST /api/v1/cwa/checkPhone
请求参数：
{
  "phoneNumber": "+919876543210"
}
    ↓
CWA返回查询结果：
{
  "phoneNumber": "+919876543210",
  "registered": true,  // 是否注册WhatsApp
  "chatting": true     // 是否可以聊天
}
    ↓
将结果存入wa_relation表（缓存）：
INSERT INTO wa_relation (
  phone_number,
  status,
  checked_at,
  last_used_at,
  source,
  tenant_id
) VALUES (
  '{联系人手机号}',
  '{chatting ? "chatting" : "not_registered"}',
  NOW(),
  NOW(),
  'cwa_api',
  NULL  -- 跨甲方缓存
)
    ↓
返回数据：
{
  "phoneNumber": "+919876543210",
  "status": "chatting",
  "source": "cwa_api",
  "registered": true,
  "chatting": true,
  "checkedAt": "2025-01-20T10:35:30Z"
}
    ↓
结束流程
```

#### 3.2 缓存清理流程（定时任务）

```
定时任务（每天凌晨3点执行）
    ↓
清理过期缓存：
DELETE FROM wa_relation
WHERE DATEDIFF(NOW(), checked_at) > 30
    ↓
记录清理结果：
    - 清理数量：{deletedCount}
    - 执行时间：{executionTime}
    ↓
发送清理报告（可选）
```

---

### 4. 业务规则与边界（Business Rules & Scope）

#### 4.1 缓存规则

**缓存有效期**：
- 7天（DATEDIFF(NOW(), checked_at) <= 7）
- 超过7天的缓存不再使用，需要重新查询

**缓存清理**：
- 定时任务：每天凌晨3点执行
- 清理策略：删除checked_at距今 > 30天的记录
- 保留期限：30天历史数据

**缓存来源**：
- `cwa_api`：来自CWA checkPhone接口查询
- `message_history`：来自历史消息回复记录

**跨甲方缓存**：
- `wa_relation`表查询：允许跨甲方条线（tenant_id = NULL）
- `message`表查询：允许跨甲方条线
- 目的：最大化缓存命中率，全局共享缓存

#### 4.2 查询优先级规则

**优先级顺序**（从高到低）：
1. **查询1**：wa_relation表缓存（7天内）- 最快，约100ms
2. **查询2**：message表历史回复（7天内）- 较快，约200ms
3. **查询3**：CWA checkPhone接口 - 较慢，约1-2秒

**查询停止条件**：
- 任一级查询命中，立即返回结果，不再继续查询
- 优先使用缓存，减少API调用

#### 4.3 状态枚举

| 状态码 | 状态名称 | 说明 |
|--------|---------|------|
| chatting | 可聊天 | 手机号已注册WhatsApp，可以发送消息 |
| not_registered | 未注册 | 手机号未注册WhatsApp，无法发送消息 |

#### 4.4 范围边界

**本次需求范围内**：
- ✅ 三级缓存机制（wa_relation表 → message表 → CWA API）
- ✅ wa_relation表缓存层（7天有效期）
- ✅ message表历史回复记录查询
- ✅ 跨甲方缓存查询
- ✅ 缓存自动更新（last_used_at）
- ✅ 定时缓存清理（30天）
- ✅ CWA API调用优化（减少90%+）

**本次需求范围外**：
- ❌ 手动刷新缓存（待实现）
- ❌ 缓存预热（待实现）
- ❌ 缓存统计报表（待实现）

---

### 5. 数据字段与口径（Data Definition）

#### 5.1 wa_relation表结构

```sql
CREATE TABLE wa_relation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  phone_number VARCHAR(20) NOT NULL COMMENT '手机号（国际格式，如+919876543210）',
  status VARCHAR(20) NOT NULL COMMENT '状态：chatting/not_registered',
  checked_at DATETIME NOT NULL COMMENT '查询时间（用于判断缓存有效期）',
  last_used_at DATETIME COMMENT '最后使用时间（缓存命中时更新）',
  source VARCHAR(20) NOT NULL COMMENT '来源：cwa_api/message_history',
  tenant_id BIGINT COMMENT '甲方ID（NULL表示跨甲方）',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_phone_status (phone_number, status, checked_at),
  INDEX idx_checked_at (checked_at)
) COMMENT='WhatsApp手机号关系缓存表';
```

#### 5.2 数据字段说明

| 字段名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | BIGINT | 是 | 主键ID | 1 |
| phone_number | VARCHAR(20) | 是 | 手机号（国际格式） | "+919876543210" |
| status | VARCHAR(20) | 是 | 状态：chatting/not_registered | "chatting" |
| checked_at | DATETIME | 是 | 查询时间（用于判断7天有效期） | "2025-01-18T10:30:25Z" |
| last_used_at | DATETIME | 否 | 最后使用时间（缓存命中时更新） | "2025-01-20T10:35:30Z" |
| source | VARCHAR(20) | 是 | 来源：cwa_api/message_history | "cwa_api" |
| tenant_id | BIGINT | 否 | 甲方ID（NULL表示跨甲方） | NULL |
| created_at | DATETIME | 是 | 创建时间 | "2025-01-18T10:30:25Z" |
| updated_at | DATETIME | 是 | 更新时间 | "2025-01-20T10:35:30Z" |

#### 5.3 索引说明

| 索引名称 | 索引字段 | 用途 |
|---------|---------|------|
| idx_phone_status | phone_number, status, checked_at | 支持快速查询缓存（查询1） |
| idx_checked_at | checked_at | 支持定时清理任务 |

---

### 6. 交互与信息展示（UX & UI Brief）

#### 6.1 用户感知

**对催员的影响**：
- 响应速度显著提升（缓存命中时从1-2秒降到100-200ms）
- 无需任何操作变化，完全透明
- 不影响现有功能和交互

**状态提示**（可选，调试用）：
- 在联系人列表或聊天窗口显示缓存来源标识：
  - "✓ 已缓存"（来自wa_relation表）
  - "✓ 消息记录"（来自message表）
  - "⟳ 实时查询"（来自CWA API）

#### 6.2 错误处理

**查询失败提示**：
- 如果所有三级查询都失败，显示提示："Unable to verify WhatsApp status. Please try again."
- 不影响其他功能，催员可以继续使用系统

---

## 二、数据需求（Data Requirements）

### 1. 埋点需求（Tracking Requirements）

| 触发时间点/条件 | 埋点中文说明 | 埋点英文ID | 关键属性 |
|----------------|------------|-----------|---------|
| Chatting状态查询 | Chatting状态查询 | chatting_status_check | phoneNumber: 手机号, source: 数据来源（cache/message_history/cwa_api） |
| 缓存命中 | 缓存命中 | cache_hit | phoneNumber: 手机号, cacheAge: 缓存天数 |
| 消息记录命中 | 消息记录命中 | message_history_hit | phoneNumber: 手机号, lastReplyAt: 最后回复时间 |
| CWA API调用 | CWA API调用 | cwa_api_call | phoneNumber: 手机号, apiType: checkPhone, responseTime: 响应时间 |
| 查询失败 | 查询失败 | chatting_check_failed | phoneNumber: 手机号, errorMessage: 错误信息 |

### 2. 统计口径（Metrics）

| 指标名称 | 计算方式 | 统计周期 |
|---------|---------|---------|
| Chatting状态缓存命中率 | 从缓存获取的查询数 / 总查询数 | 按自然日统计 |
| CWA API调用减少率 | (总查询数 - CWA API调用数) / 总查询数 | 按自然日统计 |
| 平均查询响应时间 | 所有查询响应时间总和 / 总查询数 | 按自然日统计 |
| 缓存数据量 | wa_relation表总记录数 | 实时统计 |

---

## 三、技术部分描述（Technical Requirements / TRD）

### 1. 接口设计（API Design）

#### 1.1 检查手机号WhatsApp状态接口

**接口路径**：`POST /api/v1/wa/checkPhoneStatus`

**请求参数**：
```json
{
  "phoneNumber": "+919876543210"
}
```

**响应数据**（命中缓存）：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "phoneNumber": "+919876543210",
    "status": "chatting",
    "source": "cache",
    "checkedAt": "2025-01-18T10:30:25Z",
    "cacheAge": 2
  }
}
```

**响应数据**（命中消息记录）：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "phoneNumber": "+919876543210",
    "status": "chatting",
    "source": "message_history",
    "lastReplyAt": "2025-01-19T15:20:10Z"
  }
}
```

**响应数据**（调用CWA API）：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "phoneNumber": "+919876543210",
    "status": "chatting",
    "source": "cwa_api",
    "registered": true,
    "chatting": true,
    "checkedAt": "2025-01-20T10:35:30Z"
  }
}
```

**响应数据**（未注册WhatsApp）：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "phoneNumber": "+919876543210",
    "status": "not_registered",
    "source": "cwa_api",
    "registered": false,
    "chatting": false,
    "checkedAt": "2025-01-20T10:35:30Z"
  }
}
```

---

### 2. 后端实现细节（Backend Implementation）

#### 2.1 三级查询实现（Java）

```java
@Service
public class ChattingStatusService {
    
    @Autowired
    private WaRelationMapper waRelationMapper;
    
    @Autowired
    private MessageMapper messageMapper;
    
    @Autowired
    private CwaApiClient cwaApiClient;
    
    /**
     * 检查手机号WhatsApp Chatting状态（三级缓存）
     */
    public ChattingStatusResponse checkChattingStatus(String phoneNumber) {
        // 查询1：检查wa_relation表缓存（7天内）
        WaRelation cacheRecord = waRelationMapper.findChatting(phoneNumber, 7);
        if (cacheRecord != null) {
            // 命中缓存，更新last_used_at
            waRelationMapper.updateLastUsedAt(cacheRecord.getId());
            
            return ChattingStatusResponse.builder()
                .phoneNumber(phoneNumber)
                .status("chatting")
                .source("cache")
                .checkedAt(cacheRecord.getCheckedAt())
                .cacheAge(calculateCacheAge(cacheRecord.getCheckedAt()))
                .build();
        }
        
        // 查询2：检查message表历史回复记录（7天内）
        Message recentMessage = messageMapper.findRecentCustomerReply(phoneNumber, "whatsapp", 7);
        if (recentMessage != null) {
            // 命中消息记录，插入新缓存
            WaRelation newRecord = WaRelation.builder()
                .phoneNumber(phoneNumber)
                .status("chatting")
                .checkedAt(LocalDateTime.now())
                .lastUsedAt(LocalDateTime.now())
                .source("message_history")
                .tenantId(null)  // 跨甲方缓存
                .build();
            waRelationMapper.insert(newRecord);
            
            return ChattingStatusResponse.builder()
                .phoneNumber(phoneNumber)
                .status("chatting")
                .source("message_history")
                .lastReplyAt(recentMessage.getSentAt())
                .build();
        }
        
        // 查询3：调用CWA checkPhone接口
        CwaCheckPhoneResponse cwaResponse = cwaApiClient.checkPhone(phoneNumber);
        
        // 存入缓存
        WaRelation newRecord = WaRelation.builder()
            .phoneNumber(phoneNumber)
            .status(cwaResponse.isChatting() ? "chatting" : "not_registered")
            .checkedAt(LocalDateTime.now())
            .lastUsedAt(LocalDateTime.now())
            .source("cwa_api")
            .tenantId(null)  // 跨甲方缓存
            .build();
        waRelationMapper.insert(newRecord);
        
        return ChattingStatusResponse.builder()
            .phoneNumber(phoneNumber)
            .status(cwaResponse.isChatting() ? "chatting" : "not_registered")
            .source("cwa_api")
            .registered(cwaResponse.isRegistered())
            .chatting(cwaResponse.isChatting())
            .checkedAt(LocalDateTime.now())
            .build();
    }
    
    /**
     * 计算缓存年龄（天数）
     */
    private int calculateCacheAge(LocalDateTime checkedAt) {
        return (int) ChronoUnit.DAYS.between(checkedAt, LocalDateTime.now());
    }
}
```

#### 2.2 定时缓存清理任务

```java
@Component
public class WaRelationCleanupTask {
    
    @Autowired
    private WaRelationMapper waRelationMapper;
    
    /**
     * 定时清理过期缓存（每天凌晨3点执行）
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredCache() {
        log.info("开始清理wa_relation过期缓存...");
        
        int deletedCount = waRelationMapper.deleteExpired(30);  // 删除30天前的记录
        
        log.info("wa_relation缓存清理完成，删除记录数：{}", deletedCount);
    }
}
```

#### 2.3 MyBatis Mapper实现

```xml
<!-- WaRelationMapper.xml -->
<mapper namespace="com.cco.mapper.WaRelationMapper">
    
    <!-- 查询缓存（7天内） -->
    <select id="findChatting" resultType="com.cco.entity.WaRelation">
        SELECT * FROM wa_relation
        WHERE phone_number = #{phoneNumber}
          AND status = 'chatting'
          AND DATEDIFF(NOW(), checked_at) &lt;= #{days}
          AND tenant_id IS NULL
        LIMIT 1
    </select>
    
    <!-- 更新last_used_at -->
    <update id="updateLastUsedAt">
        UPDATE wa_relation
        SET last_used_at = NOW()
        WHERE id = #{id}
    </update>
    
    <!-- 删除过期记录 -->
    <delete id="deleteExpired">
        DELETE FROM wa_relation
        WHERE DATEDIFF(NOW(), checked_at) &gt; #{days}
    </delete>
    
</mapper>

<!-- MessageMapper.xml -->
<mapper namespace="com.cco.mapper.MessageMapper">
    
    <!-- 查询最近的客户回复记录（7天内） -->
    <select id="findRecentCustomerReply" resultType="com.cco.entity.Message">
        SELECT * FROM messages
        WHERE contact_phone_number = #{phoneNumber}
          AND channel = #{channel}
          AND sender_type = 'customer'
          AND DATEDIFF(NOW(), sent_at) &lt;= #{days}
        ORDER BY sent_at DESC
        LIMIT 1
    </select>
    
</mapper>
```

---

## 四、测试用例（Test Cases）

### 1. 功能测试用例

| 测试用例ID | 测试场景 | 前置条件 | 测试步骤 | 预期结果 |
|----------|---------|---------|---------|---------|
| TC001 | 缓存命中-查询1 | wa_relation表有7天内的记录 | 1. 点击联系人（本人） | 直接使用缓存数据，不调用CWA API，更新last_used_at字段，响应时间<200ms |
| TC002 | 缓存过期-查询1失败 | wa_relation表记录超过7天 | 1. 点击联系人（本人） | 缓存失效，继续执行查询2 |
| TC003 | 消息记录命中-查询2 | message表有7天内的WhatsApp回复记录 | 1. 点击联系人（本人） | 判定为chatting状态，在wa_relation表插入记录，source='message_history' |
| TC004 | 消息记录过期-查询2失败 | message表回复记录超过7天 | 1. 点击联系人（本人） | 消息记录失效，继续执行查询3 |
| TC005 | 调用CWA API-查询3 | 查询1和查询2都未命中 | 1. 点击联系人（本人） | 调用CWA checkPhone接口，将结果存入wa_relation表，source='cwa_api' |
| TC006 | 未注册WhatsApp | CWA返回registered=false | 1. 点击联系人（本人） | 在wa_relation表插入记录，status='not_registered' |
| TC007 | 跨甲方缓存命中 | 其他甲方已查询过该手机号 | 1. 点击联系人（本人） | 使用跨甲方缓存数据（tenant_id=NULL的记录） |
| TC008 | 缓存数据更新 | 命中缓存后再次点击 | 1. 第一次点击<br>2. 第二次点击 | last_used_at字段更新为最新时间，checked_at保持不变 |
| TC009 | API调用减少验证 | 连续点击同一联系人10次 | 1. 快速点击联系人10次 | 只调用1次CWA API，其余9次使用缓存 |
| TC010 | 定时缓存清理 | wa_relation表有30天前的记录 | 1. 运行定时任务 | 删除30天前的记录，保留30天内的记录 |

### 2. 性能测试用例

| 测试用例ID | 测试场景 | 预期性能指标 |
|----------|---------|------------|
| PT001 | 缓存命中响应时间 | ≤200ms |
| PT002 | 消息记录查询响应时间 | ≤300ms |
| PT003 | CWA API调用响应时间 | ≤2s |
| PT004 | 缓存命中率（7天内） | ≥85% |
| PT005 | CWA API调用减少率 | ≥90% |

---

## 五、附录（Appendix）

### 1. 术语表（Glossary）

| 术语 | 英文 | 说明 |
|------|------|------|
| 三级缓存 | Three-Level Cache | wa_relation表 → message表 → CWA API |
| 缓存命中 | Cache Hit | 从缓存中获取数据，无需调用API |
| 跨甲方缓存 | Cross-Tenant Cache | 不同甲方条线共享缓存数据 |

### 2. 参考文档（References）

- 主需求文档：`PRD需求文档/CCO催员IM端/WhatsApp信息收发功能PRD.md`
- 发送消息：`PRD需求文档/CCO催员IM端/WhatsApp功能模块/1-催员端发送WA信息PRD.md`

---

**文档版本**：1.0.0  
**最后更新**：2025-01-20  
**文档作者**：CCO产品团队




