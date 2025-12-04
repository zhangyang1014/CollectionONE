# AI项目工程提示词规范

> **目标**: 通过标准化的提示词格式，提高AI编码的一次性成功率，减少返工次数。

---

## 📋 提示词标准模板

### 基础模板

```markdown
请实现【功能名称】。

【功能描述】
- 功能目标：...
- 用户场景：...
- 核心需求：...

【参考实现】
- 参考功能：... （指定项目中已有的类似功能）
- 后端参考：... （具体文件路径）
- 前端参考：... （具体文件路径）

【技术约定】
- 后端路径格式：...
- API响应格式：...
- 前端调用方式：...
- 数据库设计：...

【实施标准】
1. 代码能成功编译（Java 17）
2. 使用curl测试接口返回正确
3. 前端页面无500/404错误
4. 遵循项目代码规范

【完成确认】
请在完成后告诉我：
- 你参考了哪些文件
- 你检查了哪些方面
- 你测试了哪些接口
```

---

## 🏗️ 项目技术栈

### 后端技术栈

| 技术 | 版本/配置 | 说明 |
|------|----------|------|
| **Java** | **17** (强制) | ⚠️ 不能使用Java 25 |
| **Spring Boot** | 3.3.5 | 主框架 |
| **MyBatis-Plus** | 3.5.8 | ORM框架 |
| **MySQL** | 5.7+ | 数据库 |
| **端口** | **8080** (固定) | ⚠️ Python后端8000已废弃 |
| **API路径前缀** | `/api/v1` | 所有接口必须包含 |

### 前端技术栈

| 技术 | 版本/配置 | 说明 |
|------|----------|------|
| **Vue** | 3.x | 框架 |
| **TypeScript** | 5.x | 语言 |
| **Element Plus** | 最新 | UI组件库 |
| **Vite** | 最新 | 构建工具 |
| **端口** | 5173 | 开发服务器 |
| **baseURL** | `http://localhost:8080` | 后端地址（不含/api/v1） |

---

## 🎯 完整功能实现模式（参考示例）

### 示例：还款渠道管理功能

**功能结构：**
```
backend-java/src/main/java/com/cco/controller/
  └─ PaymentChannelController.java (或 MockXXXController.java)
     ├─ GET  /api/im/payment-channels (获取列表)
     ├─ POST /api/im/payment-channels (创建)
     ├─ PUT  /api/im/payment-channels/{id} (更新)
     └─ DELETE /api/im/payment-channels/{id} (删除)

frontend/src/views/payment/
  └─ PaymentChannelManagement.vue
     ├─ 列表展示（el-table）
     ├─ 筛选器（el-radio-group, el-select）
     ├─ 表单对话框（el-dialog + el-form）
     └─ CRUD操作（增删改查）
```

**后端实现关键点：**
```java
// 1. Controller注解
@Slf4j
@RestController
@RequestMapping("/api/v1")  // ✅ 必须包含/api/v1前缀
public class MockMessageTemplateController {

    // 2. 响应格式（统一使用ResponseData）
    @GetMapping("/console/message-templates")
    public ResponseData<PageResult<MessageTemplateVO>> getTemplateList(...) {
        // ... 业务逻辑 ...
        return ResponseData.success(result);  // ✅ 注意：参数顺序是(data)或(message, data)
    }

    // 3. 字段格式：统一使用snake_case
    Map<String, Object> data = new HashMap<>();
    data.put("template_name", "模板名称");      // ✅ 正确
    data.put("is_enabled", true);              // ✅ 正确
    // data.put("templateName", "模板名称");   // ❌ 错误：不要用驼峰
    
    // 4. 可选字段：不设置而非null
    if (agencyIds != null) {
        data.put("agency_ids", agencyIds);     // ✅ 只在有值时设置
    }
    // data.put("agency_ids", null);           // ❌ 错误：不要设置为null
}
```

**前端实现关键点：**
```typescript
// 1. Import位置：必须在文件顶部
import { ref, reactive, onMounted } from 'vue'         // ✅ 正确
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

// ❌ 错误：不要在函数内import
// function loadData() {
//   import request from '@/utils/request'  // 语法错误！
// }

// 2. API调用：必须包含完整路径（含/api/v1）
const loadTemplates = async () => {
  const response = await request({
    url: '/api/v1/console/message-templates',  // ✅ 正确：含/api/v1
    method: 'get',
    params: { tenantId: 1, page: 1 }
  })
}

// ❌ 错误示例
// url: '/console/message-templates'  // 缺少/api/v1前缀，会导致500错误

// 3. 响应数据处理（兼容Java后端格式）
const result = response.data || response  // Java返回{code, message, data}
const list = result.list || []
```

---

## 📐 项目技术约定

### 1. 后端API约定

#### 1.1 路径格式
```
格式：/api/v1/[模块]/[资源]
示例：
  - /api/v1/console/message-templates       (控台端)
  - /api/v1/message-templates/variables     (通用接口)
  - /api/im/payment-channels                (IM端)
```

#### 1.2 ResponseData方法签名
```java
// ✅ 正确的调用方式
ResponseData.success()                    // 无参数
ResponseData.success(data)                // 只有data
ResponseData.success("message", data)     // message在前，data在后

// ❌ 错误的调用方式
ResponseData.success(data, "message")     // 参数顺序错误！
```

#### 1.3 字段命名规范
```java
// ✅ 强制使用snake_case
field.put("field_name", "字段名称");
field.put("is_enabled", true);
field.put("created_at", timestamp);

// ❌ 禁止使用camelCase
field.put("fieldName", "字段名称");      // 错误！
field.put("isEnabled", true);            // 错误！
```

#### 1.4 Mock数据存储
```java
// ✅ 使用内存Map存储Mock数据
private final Map<Long, MessageTemplate> templateStore = new HashMap<>();

// 初始化数据
public MockXXXController() {
    initMockData();
}

private void initMockData() {
    // 创建Mock数据...
    templateStore.put(1L, template1);
}
```

### 2. 前端开发约定

#### 2.1 API调用规范
```typescript
// ✅ 必须包含完整路径（/api/v1前缀）
await request({
  url: '/api/v1/console/message-templates',  // 正确
  method: 'get'
})

// ❌ 错误：缺少/api/v1前缀
await request({
  url: '/console/message-templates',         // 会导致500错误
  method: 'get'
})
```

#### 2.2 Import语句位置
```typescript
// ✅ import必须在文件顶部（所有代码之前）
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

// 然后才是组件定义
export default defineComponent({...})

// ❌ 错误：不能在函数内import
function loadData() {
  import request from '@/utils/request'  // 语法错误！
}
```

#### 2.3 响应数据处理
```typescript
// Java后端返回格式：{ code: 200, message: "success", data: {...} }

// ✅ 兼容处理
const response = await request({...})
const result = response.data || response  // 提取data字段
const list = result.list || []            // 再提取具体数据
```

### 3. 数据库设计约定

#### 3.1 字段命名
```sql
-- ✅ 使用snake_case
CREATE TABLE message_templates (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  template_name VARCHAR(200) NOT NULL,
  is_enabled TINYINT(1) DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ❌ 不要使用camelCase
-- templateName, isEnabled, createdAt  ❌
```

#### 3.2 通用字段
```sql
-- 每个表必须包含
id BIGINT PRIMARY KEY AUTO_INCREMENT
tenant_id BIGINT NOT NULL COMMENT '甲方ID'
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
created_by BIGINT COMMENT '创建人ID'
updated_by BIGINT COMMENT '更新人ID'
```

---

## ✅ 自测检查清单

### 实施前检查（必做）

- [ ] **查看参考实现**：找到项目中类似功能的实现
  ```bash
  # 查找类似Controller
  find backend-java/src -name "*Controller.java" | grep -i "payment\|channel\|template"
  
  # 查找类似Vue页面
  find frontend/src/views -name "*.vue" | grep -i "management"
  ```

- [ ] **确认ResponseData签名**
  ```bash
  grep -A 3 "public static.*success" backend-java/src/main/java/com/cco/common/response/ResponseData.java
  ```

- [ ] **确认request.ts的baseURL**
  ```bash
  grep "baseURL" frontend/src/utils/request.ts
  ```

- [ ] **确认其他页面的API调用方式**
  ```bash
  grep -r "await request" frontend/src/views --include="*.vue" | head -5
  ```

### 开发中检查（必做）

#### 后端代码
- [ ] Controller路径包含 `/api/v1` 前缀
- [ ] ResponseData调用参数顺序正确
- [ ] 所有字段使用snake_case格式
- [ ] 可选字段不设置而非null
- [ ] 日志输出清晰（使用log.info记录关键操作）

#### 前端代码
- [ ] import语句在文件顶部
- [ ] API调用URL包含 `/api/v1` 前缀
- [ ] 响应数据正确处理（提取data字段）
- [ ] 错误处理完善（try-catch + ElMessage）

### 完成后检查（必做）

#### 1. 编译检查
```bash
cd backend-java
JAVA_HOME=/opt/homebrew/opt/openjdk@17 \
PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH \
mvn clean compile

# 应该显示：BUILD SUCCESS
```

#### 2. 接口测试（必须测试所有接口）
```bash
# 测试列表接口
curl -s "http://localhost:8080/api/v1/console/message-templates?page=1&pageSize=20&tenantId=1" | jq '.'

# 应该返回：
# {
#   "code": 200,
#   "message": "success",
#   "data": { "total": ..., "list": [...] }
# }

# 测试其他接口...
curl -s "http://localhost:8080/api/v1/message-templates/variables" | jq '.'
```

#### 3. 前端测试
```bash
# 检查控制台是否有错误
# 打开浏览器开发者工具（F12）
# Network标签页应该显示200状态码
# Console标签页应该无红色错误
```

#### 4. 字段格式检查
```bash
# 运行项目的字段格式检查脚本
./scripts/check-field-format.sh

# 应该返回：✅ 未发现驼峰格式字段
```

---

## 🎯 完整提示词示例

### 示例1：新增功能（完整版）

```markdown
请实现【消息模板配置管理】功能。

【功能描述】
- 功能目标：为控台管理员提供消息模板的CRUD管理
- 用户场景：管理员在控台配置模板，催员在IM端使用模板发送消息
- 核心需求：支持组织模板、个人模板，支持变量替换，按机构控制可见性

【参考实现】
请先研究以下文件的实现模式：
- 后端参考：backend-java/src/main/java/com/cco/controller/PaymentChannelController.java
- 前端参考：frontend/src/views/payment/PaymentChannelManagement.vue
- API工具：frontend/src/utils/request.ts
- 响应格式：backend-java/src/main/java/com/cco/common/response/ResponseData.java

【技术约定】
必须遵循以下约定：
1. **后端路径格式**：/api/v1/console/message-templates
2. **ResponseData调用**：ResponseData.success(message, data) 或 ResponseData.success(data)
3. **字段命名**：强制使用snake_case（如template_name, is_enabled）
4. **前端API调用**：必须包含完整路径，如 `/api/v1/console/message-templates`
5. **Java版本**：强制使用Java 17
6. **端口**：后端8080，前端5173

【数据库设计】
参考PRD文档中的表结构，必须包含：
- 基础字段：id, tenant_id, created_at, updated_at, created_by, updated_by
- 业务字段：template_name, template_type, agency_ids(JSON), content, variables(JSON)
- 状态字段：is_enabled, sort_order, usage_count

【实施标准】
完成后必须通过以下检查：
1. ✅ Java代码编译成功（mvn clean compile）
2. ✅ 使用curl测试接口，返回正确的JSON
3. ✅ 前端页面无500/404错误
4. ✅ 字段格式检查通过（./scripts/check-field-format.sh）

【完成确认】
请在完成后告诉我：
1. 你参考了哪些文件
2. 你发现了哪些约定（如ResponseData的参数顺序）
3. 你测试了哪些接口（提供curl命令和返回结果）
4. 你检查了哪些方面
```

### 示例2：Bug修复（简化版）

```markdown
修复【消息模板列表500错误】。

【问题描述】
前端请求 /console/message-templates 返回500错误

【检查要求】
1. 确认后端接口路径是否包含 /api/v1 前缀
2. 确认前端调用是否包含完整路径
3. 确认ResponseData调用参数顺序是否正确
4. 参考PaymentChannelController的实现方式

【完成标准】
- curl测试接口返回200
- 前端页面正常显示数据
```

### 示例3：功能优化（针对性）

```markdown
优化【模板变量插入】功能。

【当前问题】
点击变量标签无法插入到光标位置

【参考实现】
查看frontend/src/components/IMPanel.vue中的变量插入实现

【技术要求】
1. 获取textarea的光标位置
2. 在光标位置插入变量
3. 插入后光标移动到变量后面

【测试要求】
1. 光标在开头、中间、末尾都能正确插入
2. 连续点击多个变量都能正确插入
```

---

## 🚨 常见错误及避免方法

### 错误1：API路径缺少前缀
```typescript
// ❌ 错误
url: '/console/message-templates'

// ✅ 正确
url: '/api/v1/console/message-templates'

// 🔧 避免方法：在提示词中明确要求
"前端API调用必须包含完整路径（含/api/v1前缀），参考PaymentChannelManagement的实现"
```

### 错误2：ResponseData参数顺序错误
```java
// ❌ 错误
return ResponseData.success(null, "删除成功");

// ✅ 正确
return ResponseData.success("删除成功", null);

// 🔧 避免方法：在提示词中明确要求
"先检查ResponseData.success()的方法签名，确认参数顺序"
```

### 错误3：import语句位置错误
```typescript
// ❌ 错误
function loadData() {
  import request from '@/utils/request'  // 语法错误
}

// ✅ 正确
import request from '@/utils/request'  // 在文件顶部

function loadData() {
  // 使用request
}

// 🔧 避免方法：在提示词中明确要求
"确保所有import语句在文件顶部，参考项目中其他Vue组件的写法"
```

### 错误4：字段命名不规范
```java
// ❌ 错误
map.put("templateName", "模板");  // camelCase

// ✅ 正确
map.put("template_name", "模板");  // snake_case

// 🔧 避免方法：在提示词中明确要求
"所有字段必须使用snake_case格式，完成后运行./scripts/check-field-format.sh检查"
```

---

## 📝 提示词写作检查清单

在发送提示词前，确认包含以下要素：

- [ ] **功能描述清晰**：说明做什么、为什么、给谁用
- [ ] **指定参考文件**：给出具体的文件路径作为参考
- [ ] **明确技术约定**：列出API路径格式、响应格式、字段命名等
- [ ] **提供自测标准**：说明如何验证功能正确性
- [ ] **要求反馈确认**：让AI告诉你检查了什么

---

## 🎓 提示词质量评分标准

| 级别 | 描述 | 一次成功率 | 示例 |
|------|------|-----------|------|
| ⭐ 差 | 只说做什么，没有任何约束 | < 30% | "实现消息模板功能" |
| ⭐⭐ 一般 | 有功能描述，无技术约定 | 30-50% | "实现消息模板的增删改查" |
| ⭐⭐⭐ 良好 | 有参考文件，有部分约定 | 50-70% | "参考PaymentChannel实现消息模板" |
| ⭐⭐⭐⭐ 优秀 | 参考+约定+自测要求 | 70-90% | 包含参考实现、技术约定、测试标准 |
| ⭐⭐⭐⭐⭐ 完美 | 完整标准模板 | > 90% | 本文档的"完整提示词示例" |

---

## 🔄 迭代改进流程

### 第一次实现失败时

1. **分析错误原因**
   - 是技术约定问题？→ 补充到提示词中
   - 是参考不足？→ 指定更具体的参考文件
   - 是自测缺失？→ 要求AI先自测再告诉你

2. **更新提示词模板**
   - 将新发现的约定写入规范
   - 更新检查清单
   - 记录到"常见错误"章节

3. **验证改进效果**
   - 用相似功能测试新提示词
   - 对比成功率提升

---

## 📚 附录：快速参考

### 项目关键文件路径

**后端：**
```
backend-java/src/main/java/com/cco/
├── controller/              # Controller层
│   ├── PaymentChannelController.java      (参考：IM端接口)
│   └── MockMessageTemplateController.java (参考：Mock实现)
├── common/response/
│   └── ResponseData.java   # 统一响应格式
└── model/entity/           # 实体类
```

**前端：**
```
frontend/src/
├── views/                  # 页面组件
│   ├── payment/PaymentChannelManagement.vue  (参考：完整CRUD)
│   └── console/MessageTemplateList.vue        (参考：列表+表单)
├── utils/
│   └── request.ts          # API请求工具
└── config/
    └── api.ts              # API配置
```

### 常用检查命令

```bash
# 检查Java版本
java -version  # 应显示17.0.x

# 编译后端
cd backend-java && mvn clean compile

# 测试接口
curl -s "http://localhost:8080/api/v1/console/message-templates?tenantId=1" | jq '.'

# 检查字段格式
./scripts/check-field-format.sh

# 检查硬编码URL
grep -r "localhost:8000\|localhost:8080" frontend/src --include="*.ts" --include="*.vue" | grep -v "config/api.ts"
```

---

**文档版本**: 1.0.0  
**最后更新**: 2025-12-03  
**作者**: CCO开发团队  
**适用项目**: CollectionONE

---

## 💡 使用建议

1. **首次使用**：先用"完整提示词示例"测试一个功能
2. **熟练后**：可以简化，但必须保留"参考实现"和"技术约定"
3. **复杂功能**：使用完整模板，宁可啰嗦不要遗漏
4. **简单修复**：使用简化版，但明确检查要求

**记住**：好的提示词 = 清晰的需求 + 明确的约定 + 可验证的标准 ✅

