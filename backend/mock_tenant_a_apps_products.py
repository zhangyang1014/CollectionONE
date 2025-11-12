"""
为甲方A创建App和Product的Mock数据
"""
import json

# 甲方A的App和Product数据
TENANT_A_APPS = [
    {
        "id": 1,
        "tenant_id": 1,
        "app_code": "APP001",
        "app_name": "快贷通",
        "app_name_en": "QuickLoan",
        "app_type": "mobile",  # mobile/web
        "platform": "iOS",  # iOS/Android/Web
        "is_active": True,
        "created_at": "2024-01-01T00:00:00",
        "updated_at": "2024-01-01T00:00:00"
    },
    {
        "id": 2,
        "tenant_id": 1,
        "app_code": "APP002",
        "app_name": "信用钱包",
        "app_name_en": "CreditWallet",
        "app_type": "mobile",
        "platform": "Android",
        "is_active": True,
        "created_at": "2024-02-01T00:00:00",
        "updated_at": "2024-02-01T00:00:00"
    },
    {
        "id": 3,
        "tenant_id": 1,
        "app_code": "APP003",
        "app_name": "现金贷",
        "app_name_en": "CashLoan",
        "app_type": "web",
        "platform": "Web",
        "is_active": True,
        "created_at": "2024-03-01T00:00:00",
        "updated_at": "2024-03-01T00:00:00"
    },
    {
        "id": 4,
        "tenant_id": 1,
        "app_code": "APP004",
        "app_name": "分期购",
        "app_name_en": "InstallmentBuy",
        "app_type": "mobile",
        "platform": "iOS",
        "is_active": True,
        "created_at": "2024-04-01T00:00:00",
        "updated_at": "2024-04-01T00:00:00"
    },
    {
        "id": 5,
        "tenant_id": 1,
        "app_code": "APP005",
        "app_name": "微贷助手",
        "app_name_en": "MicroLoanHelper",
        "app_type": "mobile",
        "platform": "Android",
        "is_active": False,  # 已停用
        "created_at": "2023-12-01T00:00:00",
        "updated_at": "2024-11-01T00:00:00"
    }
]

TENANT_A_PRODUCTS = [
    {
        "id": 1,
        "tenant_id": 1,
        "product_code": "PROD001",
        "product_name": "极速贷",
        "product_name_en": "ExpressLoan",
        "product_type": "cash_loan",  # cash_loan/installment/credit_card
        "min_amount": 1000,
        "max_amount": 50000,
        "interest_rate": 0.05,  # 5%
        "term_days": 30,
        "is_active": True,
        "created_at": "2024-01-01T00:00:00",
        "updated_at": "2024-01-01T00:00:00"
    },
    {
        "id": 2,
        "tenant_id": 1,
        "product_code": "PROD002",
        "product_name": "分期贷",
        "product_name_en": "InstallmentLoan",
        "product_type": "installment",
        "min_amount": 5000,
        "max_amount": 100000,
        "interest_rate": 0.08,
        "term_days": 90,
        "is_active": True,
        "created_at": "2024-01-15T00:00:00",
        "updated_at": "2024-01-15T00:00:00"
    },
    {
        "id": 3,
        "tenant_id": 1,
        "product_code": "PROD003",
        "product_name": "信用贷",
        "product_name_en": "CreditLoan",
        "product_type": "credit_loan",
        "min_amount": 10000,
        "max_amount": 200000,
        "interest_rate": 0.06,
        "term_days": 180,
        "is_active": True,
        "created_at": "2024-02-01T00:00:00",
        "updated_at": "2024-02-01T00:00:00"
    },
    {
        "id": 4,
        "tenant_id": 1,
        "product_code": "PROD004",
        "product_name": "小额贷",
        "product_name_en": "SmallLoan",
        "product_type": "cash_loan",
        "min_amount": 500,
        "max_amount": 5000,
        "interest_rate": 0.12,
        "term_days": 14,
        "is_active": True,
        "created_at": "2024-03-01T00:00:00",
        "updated_at": "2024-03-01T00:00:00"
    },
    {
        "id": 5,
        "tenant_id": 1,
        "product_code": "PROD005",
        "product_name": "大额贷",
        "product_name_en": "LargeLoan",
        "product_type": "credit_loan",
        "min_amount": 50000,
        "max_amount": 500000,
        "interest_rate": 0.04,
        "term_days": 365,
        "is_active": True,
        "created_at": "2024-04-01T00:00:00",
        "updated_at": "2024-04-01T00:00:00"
    },
    {
        "id": 6,
        "tenant_id": 1,
        "product_code": "PROD006",
        "product_name": "学生贷",
        "product_name_en": "StudentLoan",
        "product_type": "installment",
        "min_amount": 2000,
        "max_amount": 20000,
        "interest_rate": 0.03,
        "term_days": 60,
        "is_active": True,
        "created_at": "2024-05-01T00:00:00",
        "updated_at": "2024-05-01T00:00:00"
    }
]

# 导出为JSON格式（用于API返回）
TENANT_A_APPS_JSON = json.dumps(TENANT_A_APPS, ensure_ascii=False, indent=2)
TENANT_A_PRODUCTS_JSON = json.dumps(TENANT_A_PRODUCTS, ensure_ascii=False, indent=2)

if __name__ == "__main__":
    print("=" * 60)
    print("甲方A的App数据")
    print("=" * 60)
    print(TENANT_A_APPS_JSON)
    print("\n" + "=" * 60)
    print("甲方A的Product数据")
    print("=" * 60)
    print(TENANT_A_PRODUCTS_JSON)

