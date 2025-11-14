# 甲方A的App和Product Mock数据

**创建日期**: 2025-01-11  
**甲方**: 示例甲方A (ID: 1, TENANT001)

---

## 一、App列表（5个）

| ID | App编码 | App名称 | 英文名 | 类型 | 平台 | 状态 |
|----|---------|---------|--------|------|------|------|
| 1 | APP001 | 快贷通 | QuickLoan | mobile | iOS | ✅ 启用 |
| 2 | APP002 | 信用钱包 | CreditWallet | mobile | Android | ✅ 启用 |
| 3 | APP003 | 现金贷 | CashLoan | web | Web | ✅ 启用 |
| 4 | APP004 | 分期购 | InstallmentBuy | mobile | iOS | ✅ 启用 |
| 5 | APP005 | 微贷助手 | MicroLoanHelper | mobile | Android | ❌ 停用 |

### App详情

#### 1. 快贷通 (APP001)
- **App编码**: APP001
- **App名称**: 快贷通
- **英文名**: QuickLoan
- **类型**: 移动应用 (mobile)
- **平台**: iOS
- **状态**: 启用
- **创建时间**: 2024-01-01

#### 2. 信用钱包 (APP002)
- **App编码**: APP002
- **App名称**: 信用钱包
- **英文名**: CreditWallet
- **类型**: 移动应用 (mobile)
- **平台**: Android
- **状态**: 启用
- **创建时间**: 2024-02-01

#### 3. 现金贷 (APP003)
- **App编码**: APP003
- **App名称**: 现金贷
- **英文名**: CashLoan
- **类型**: Web应用 (web)
- **平台**: Web
- **状态**: 启用
- **创建时间**: 2024-03-01

#### 4. 分期购 (APP004)
- **App编码**: APP004
- **App名称**: 分期购
- **英文名**: InstallmentBuy
- **类型**: 移动应用 (mobile)
- **平台**: iOS
- **状态**: 启用
- **创建时间**: 2024-04-01

#### 5. 微贷助手 (APP005)
- **App编码**: APP005
- **App名称**: 微贷助手
- **英文名**: MicroLoanHelper
- **类型**: 移动应用 (mobile)
- **平台**: Android
- **状态**: 停用
- **创建时间**: 2023-12-01
- **停用时间**: 2024-11-01

---

## 二、产品列表（6个）

| ID | 产品编码 | 产品名称 | 英文名 | 产品类型 | 金额范围 | 利率 | 期限 | 状态 |
|----|---------|---------|--------|---------|---------|------|------|------|
| 1 | PROD001 | 极速贷 | ExpressLoan | 现金贷 | 1,000-50,000 | 5% | 30天 | ✅ 启用 |
| 2 | PROD002 | 分期贷 | InstallmentLoan | 分期贷 | 5,000-100,000 | 8% | 90天 | ✅ 启用 |
| 3 | PROD003 | 信用贷 | CreditLoan | 信用贷 | 10,000-200,000 | 6% | 180天 | ✅ 启用 |
| 4 | PROD004 | 小额贷 | SmallLoan | 现金贷 | 500-5,000 | 12% | 14天 | ✅ 启用 |
| 5 | PROD005 | 大额贷 | LargeLoan | 信用贷 | 50,000-500,000 | 4% | 365天 | ✅ 启用 |
| 6 | PROD006 | 学生贷 | StudentLoan | 分期贷 | 2,000-20,000 | 3% | 60天 | ✅ 启用 |

### 产品详情

#### 1. 极速贷 (PROD001)
- **产品编码**: PROD001
- **产品名称**: 极速贷
- **英文名**: ExpressLoan
- **产品类型**: 现金贷 (cash_loan)
- **金额范围**: 1,000 - 50,000 元
- **利率**: 5% (0.05)
- **期限**: 30天
- **状态**: 启用
- **创建时间**: 2024-01-01

#### 2. 分期贷 (PROD002)
- **产品编码**: PROD002
- **产品名称**: 分期贷
- **英文名**: InstallmentLoan
- **产品类型**: 分期贷 (installment)
- **金额范围**: 5,000 - 100,000 元
- **利率**: 8% (0.08)
- **期限**: 90天
- **状态**: 启用
- **创建时间**: 2024-01-15

#### 3. 信用贷 (PROD003)
- **产品编码**: PROD003
- **产品名称**: 信用贷
- **英文名**: CreditLoan
- **产品类型**: 信用贷 (credit_loan)
- **金额范围**: 10,000 - 200,000 元
- **利率**: 6% (0.06)
- **期限**: 180天
- **状态**: 启用
- **创建时间**: 2024-02-01

#### 4. 小额贷 (PROD004)
- **产品编码**: PROD004
- **产品名称**: 小额贷
- **英文名**: SmallLoan
- **产品类型**: 现金贷 (cash_loan)
- **金额范围**: 500 - 5,000 元
- **利率**: 12% (0.12)
- **期限**: 14天
- **状态**: 启用
- **创建时间**: 2024-03-01

#### 5. 大额贷 (PROD005)
- **产品编码**: PROD005
- **产品名称**: 大额贷
- **英文名**: LargeLoan
- **产品类型**: 信用贷 (credit_loan)
- **金额范围**: 50,000 - 500,000 元
- **利率**: 4% (0.04)
- **期限**: 365天
- **状态**: 启用
- **创建时间**: 2024-04-01

#### 6. 学生贷 (PROD006)
- **产品编码**: PROD006
- **产品名称**: 学生贷
- **英文名**: StudentLoan
- **产品类型**: 分期贷 (installment)
- **金额范围**: 2,000 - 20,000 元
- **利率**: 3% (0.03)
- **期限**: 60天
- **状态**: 启用
- **创建时间**: 2024-05-01

---

## 三、API接口

### 3.1 获取甲方A的App列表

**接口**: `GET /api/v1/tenants/1/apps`

**响应示例**:
```json
{
  "data": [
    {
      "id": 1,
      "tenant_id": 1,
      "app_code": "APP001",
      "app_name": "快贷通",
      "app_name_en": "QuickLoan",
      "app_type": "mobile",
      "platform": "iOS",
      "is_active": true,
      "created_at": "2024-01-01T00:00:00",
      "updated_at": "2024-01-01T00:00:00"
    },
    ...
  ]
}
```

### 3.2 获取甲方A的产品列表

**接口**: `GET /api/v1/tenants/1/products`

**响应示例**:
```json
{
  "data": [
    {
      "id": 1,
      "tenant_id": 1,
      "product_code": "PROD001",
      "product_name": "极速贷",
      "product_name_en": "ExpressLoan",
      "product_type": "cash_loan",
      "min_amount": 1000,
      "max_amount": 50000,
      "interest_rate": 0.05,
      "term_days": 30,
      "is_active": true,
      "created_at": "2024-01-01T00:00:00",
      "updated_at": "2024-01-01T00:00:00"
    },
    ...
  ]
}
```

---

## 四、数据结构说明

### 4.1 App数据结构

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | App唯一ID |
| tenant_id | Integer | 所属甲方ID |
| app_code | String | App编码（唯一标识） |
| app_name | String | App名称（中文） |
| app_name_en | String | App名称（英文） |
| app_type | String | App类型：mobile/web |
| platform | String | 平台：iOS/Android/Web |
| is_active | Boolean | 是否启用 |
| created_at | DateTime | 创建时间 |
| updated_at | DateTime | 更新时间 |

### 4.2 Product数据结构

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 产品唯一ID |
| tenant_id | Integer | 所属甲方ID |
| product_code | String | 产品编码（唯一标识） |
| product_name | String | 产品名称（中文） |
| product_name_en | String | 产品名称（英文） |
| product_type | String | 产品类型：cash_loan/installment/credit_loan |
| min_amount | Decimal | 最小金额 |
| max_amount | Decimal | 最大金额 |
| interest_rate | Decimal | 利率（0.05表示5%） |
| term_days | Integer | 期限（天数） |
| is_active | Boolean | 是否启用 |
| created_at | DateTime | 创建时间 |
| updated_at | DateTime | 更新时间 |

---

## 五、测试命令

### 5.1 查询App列表

```bash
curl http://localhost:8000/api/v1/tenants/1/apps
```

### 5.2 查询产品列表

```bash
curl http://localhost:8000/api/v1/tenants/1/products
```

---

## 六、数据文件位置

- **Mock数据定义**: `backend/simple_server.py`
- **独立数据文件**: `backend/mock_tenant_a_apps_products.py`

---

## 七、使用说明

这些mock数据已集成到 `simple_server.py` 中，可以通过以下API访问：

1. **获取App列表**: `GET /api/v1/tenants/1/apps`
2. **获取产品列表**: `GET /api/v1/tenants/1/products`

**注意**: 目前只有甲方A（tenant_id=1）有App和Product数据，其他甲方返回空数组。

---

**数据统计**:
- ✅ App数量: 5个（4个启用，1个停用）
- ✅ Product数量: 6个（全部启用）
- ✅ 覆盖平台: iOS、Android、Web
- ✅ 产品类型: 现金贷、分期贷、信用贷

