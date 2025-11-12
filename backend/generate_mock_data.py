#!/usr/bin/env python3
"""
从CSV文件生成完整的Mock数据
"""
import json

# 字段分组
field_groups = [
    {"id": 1, "group_key": "customer_basic_identity", "group_name": "客户基本信息-基础身份信息", "group_name_en": "Customer Basic - Identity", "parent_id": None, "sort_order": 1},
    {"id": 2, "group_key": "customer_basic_education", "group_name": "客户基本信息-教育", "group_name_en": "Customer Basic - Education", "parent_id": None, "sort_order": 2},
    {"id": 3, "group_key": "customer_basic_employment", "group_name": "客户基本信息-职业信息", "group_name_en": "Customer Basic - Employment", "parent_id": None, "sort_order": 3},
    {"id": 4, "group_key": "customer_basic_behavior", "group_name": "客户基本信息-用户行为与信用", "group_name_en": "Customer Basic - Behavior", "parent_id": None, "sort_order": 4},
    {"id": 5, "group_key": "loan_details", "group_name": "贷款详情", "group_name_en": "Loan Details", "parent_id": None, "sort_order": 5},
    {"id": 6, "group_key": "installment_details", "group_name": "分期详情", "group_name_en": "Installment Details", "parent_id": None, "sort_order": 6},
    {"id": 7, "group_key": "loan_record", "group_name": "借款记录", "group_name_en": "Loan Record", "parent_id": None, "sort_order": 7},
    {"id": 8, "group_key": "repayment_record", "group_name": "还款记录", "group_name_en": "Repayment Record", "parent_id": None, "sort_order": 8},
    {"id": 9, "group_key": "collection_record", "group_name": "催记", "group_name_en": "Collection Record", "parent_id": None, "sort_order": 9},
]

# 标准字段 - 完整数据
standard_fields = []
field_id = 1

# 客户基本信息-基础身份信息
identity_fields = [
    ("user_id", "用户编号", "String", False, "5983"),
    ("user_name", "用户姓名", "String", False, "张三"),
    ("gender", "性别", "Enum", False, "Male"),
    ("birth_date", "出生日期", "Date", False, "1990-01-01"),
    ("nationality", "国籍", "String", True, "中国"),
    ("marital_status", "婚姻状况", "Enum", False, "已婚"),
    ("id_type", "证件类型", "Enum", False, "身份证"),
    ("id_number", "证件号码", "String", False, "110101199001011234"),
    ("address", "居住地址", "String", False, "北京市朝阳区xxx"),
    ("years_at_address", "居住年限", "Integer", False, "5"),
    ("housing_type", "居住类型", "Enum", False, "自有"),
]
for field_key, field_name, field_type, is_extended, example in identity_fields:
    standard_fields.append({
        "id": field_id,
        "field_key": field_key,
        "field_name": field_name,
        "field_type": field_type,
        "field_group_id": 1,
        "is_required": field_key in ["user_id", "user_name"],
        "is_extended": is_extended,
        "example_value": example,
        "sort_order": field_id,
    })
    field_id += 1

# 客户基本信息-教育
education_fields = [
    ("education_level", "教育程度", "Enum", False, "本科"),
    ("school_name", "学校名称", "String", False, "清华大学"),
    ("major", "专业", "String", False, "计算机科学"),
]
for field_key, field_name, field_type, is_extended, example in education_fields:
    standard_fields.append({
        "id": field_id,
        "field_key": field_key,
        "field_name": field_name,
        "field_type": field_type,
        "field_group_id": 2,
        "is_required": False,
        "is_extended": is_extended,
        "example_value": example,
        "sort_order": field_id,
    })
    field_id += 1

# 客户基本信息-职业信息
employment_fields = [
    ("employment_status", "工作状态", "Enum", False, "在职"),
    ("company_name", "公司名称", "String", False, "ABC公司"),
    ("job_title", "职位", "String", False, "工程师"),
    ("industry", "行业类别", "String", False, "科技"),
    ("years_of_employment", "工作年限", "Integer", False, "3"),
    ("work_address", "工作地址", "String", False, "北京市海淀区"),
    ("company_phone", "公司联系电话", "String", False, "010-12345678"),
    ("income_type", "收入类型", "Enum", False, "月薪"),
    ("payday", "发薪日", "String", False, "每月15日"),
    ("income_range", "收入", "String", False, "10000-20000"),
    ("income_source", "收入来源", "Enum", False, "主要"),
]
for field_key, field_name, field_type, is_extended, example in employment_fields:
    standard_fields.append({
        "id": field_id,
        "field_key": field_key,
        "field_name": field_name,
        "field_type": field_type,
        "field_group_id": 3,
        "is_required": False,
        "is_extended": is_extended,
        "example_value": example,
        "sort_order": field_id,
    })
    field_id += 1

# 客户基本信息-用户行为与信用
behavior_fields = [
    ("last_app_open_time", "最近打开时间", "Datetime", False, "2025-01-01 12:00:00"),
    ("last_repayment_page_visit_time", "最近访问还款页时间", "Datetime", False, "2025-01-01 14:00:00"),
    ("total_loan_count", "历史借款总笔数", "Integer", False, "5"),
    ("cleared_loan_count", "已结清笔数", "Integer", False, "3"),
    ("overdue_loan_count", "历史逾期笔数", "Integer", False, "1"),
    ("max_overdue_days", "历史最大逾期天数", "Integer", False, "10"),
    ("avg_loan_amount", "平均借款金额", "Decimal", False, "5000"),
    ("credit_score_001", "001信用评分", "Enum", False, "A"),
    ("credit_score_002", "002信用评分", "Enum", False, "B"),
    ("credit_score_003", "003信用评分", "Enum", False, "C"),
]
for field_key, field_name, field_type, is_extended, example in behavior_fields:
    standard_fields.append({
        "id": field_id,
        "field_key": field_key,
        "field_name": field_name,
        "field_type": field_type,
        "field_group_id": 4,
        "is_required": False,
        "is_extended": is_extended,
        "example_value": example,
        "sort_order": field_id,
    })
    field_id += 1

# 贷款详情
loan_details_fields = [
    ("loan_id", "贷款编号", "String", False, "LOAN123"),
    ("case_status", "案件状态", "Enum", False, "未结清"),
    ("product_type", "产品类别", "String", False, "借款订单"),
    ("disbursement_time", "放款时间", "Datetime", False, "2025-01-01 10:00:00"),
    ("total_due_amount", "应还金额", "Decimal", False, "1460"),
    ("total_paid_amount", "已还金额", "Decimal", False, "975"),
    ("outstanding_amount", "应还未还金额", "Decimal", False, "485"),
    ("principal_due", "应收本金", "Decimal", False, "1000"),
    ("interest_due", "应收利息", "Decimal", False, "180"),
    ("service_fee", "服务费", "Decimal", False, "270"),
    ("penalty_fee", "应收罚息", "Decimal", False, "10"),
    ("account_number", "代扣账号", "String", False, "6222001234567890"),
    ("bank_name", "开户行", "String", False, "工商银行"),
    ("loan_app", "借款App", "String", False, "MegaPeso"),
    ("contract_no", "合同编号", "String", True, "CON20250101001"),
    ("disbursement_channel", "放款渠道", "String", True, "Partner Bank"),
    ("collection_entry_time", "入催时间", "Datetime", True, "2025-01-05 00:00:00"),
    ("overdue_days", "当前逾期天数", "Integer", True, "3"),
    ("next_collection_time", "下次催收时间", "Datetime", True, "2025-01-08 09:00:00"),
    ("collection_status", "催收状态", "Enum", True, "未联系"),
    ("collection_stage", "催收阶段", "Enum", True, "早期"),
    ("repayment_method", "还款方式", "Enum", True, "银行转账"),
    ("last_payment_date", "最近还款日期", "Datetime", True, "2025-01-02 14:33:22"),
]
for field_key, field_name, field_type, is_extended, example in loan_details_fields:
    standard_fields.append({
        "id": field_id,
        "field_key": field_key if field_key != "loan_id" else "loan_details_loan_id",
        "field_name": field_name,
        "field_type": field_type,
        "field_group_id": 5,
        "is_required": field_key in ["loan_id"],
        "is_extended": is_extended,
        "example_value": example,
        "sort_order": field_id,
    })
    field_id += 1

# 分期详情
installment_fields = [
    ("installment_no", "期数", "Integer", False, "1"),
    ("installment_status", "状态", "Enum", False, "待还款"),
    ("due_date", "应还款时间", "Date", False, "2025-01-15"),
    ("installment_overdue_days", "逾期天数", "Integer", False, "0"),
    ("due_amount", "应还金额", "Decimal", False, "985"),
    ("payment_date", "实际还款时间", "Datetime", False, "2025-01-15 12:00:00"),
    ("paid_amount", "实际已还金额", "Decimal", False, "985"),
    ("early_repayment_flag", "提前还款标志", "Boolean", False, "FALSE"),
    ("outstanding", "应还未还", "Decimal", False, "0"),
    ("principal", "应收本金", "Decimal", False, "678"),
    ("interest", "应收利息", "Decimal", False, "30"),
    ("penalty", "应收罚息", "Decimal", False, "0"),
    ("installment_service_fee", "服务费", "Decimal", False, "270"),
    ("waived_amount", "减免金额", "Decimal", True, "0"),
    ("installment_channel", "分期还款渠道", "String", True, "GCash"),
]
for field_key, field_name, field_type, is_extended, example in installment_fields:
    standard_fields.append({
        "id": field_id,
        "field_key": field_key,
        "field_name": field_name,
        "field_type": field_type,
        "field_group_id": 6,
        "is_required": False,
        "is_extended": is_extended,
        "example_value": example,
        "sort_order": field_id,
    })
    field_id += 1

# 借款记录
loan_record_fields = [
    ("loan_record_loan_id", "贷款编号", "String", False, "LOAN100023"),
    ("loan_record_user_id", "用户编号", "String", False, "USER5983"),
    ("loan_record_user_name", "用户姓名", "String", False, "张三"),
    ("mobile_number", "手机号码", "String", False, "+86 13800138000"),
    ("app_name", "App名称", "String", False, "MegaPeso"),
    ("product_name", "产品名称", "String", False, "Cash Loan"),
    ("merchant_name", "贷超商户", "String", True, "EasyLoan Partner"),
    ("system_name", "所属系统", "String", True, "CollectionSystemV2"),
    ("app_download_url", "App下载链接", "String", True, "https://play.google.com/..."),
    ("collection_type", "首复催类型", "Enum", False, "首催"),
    ("reborrow_flag", "是否复借", "Boolean", True, "FALSE"),
    ("auto_reloan", "自动复借", "Boolean", True, "FALSE"),
    ("first_term_days", "首期期限", "Integer", True, "14"),
    ("loan_record_due_date", "应还款日期", "Date", False, "2025-01-15"),
    ("loan_record_overdue_days", "逾期天数", "Integer", False, "0"),
    ("loan_record_outstanding_amount", "应还未还金额", "Decimal", False, "850"),
    ("binder", "绑定人", "String", False, "Agent_001"),
    ("loan_record_case_status", "案件状态", "Enum", False, "进行中"),
    ("settlement_method", "结清方式", "Enum", False, "自动扣款"),
    ("settlement_time", "结清时间", "Datetime", False, ""),
    ("latest_result", "最新结果", "Enum", False, "承诺还款"),
    ("last_collection_time", "最近催收时间", "Datetime", False, "2025-01-05 18:00:00"),
    ("operator", "处理人", "String", False, "Agent_Ana"),
    ("loan_record_remark", "备注", "String", True, "电话无人接听"),
    ("loan_record_principal_due", "应还本金", "Decimal", True, "1000"),
    ("loan_record_interest_due", "应收利息", "Decimal", True, "150"),
]
for field_key, field_name, field_type, is_extended, example in loan_record_fields:
    standard_fields.append({
        "id": field_id,
        "field_key": field_key,
        "field_name": field_name,
        "field_type": field_type,
        "field_group_id": 7,
        "is_required": False,
        "is_extended": is_extended,
        "example_value": example,
        "sort_order": field_id,
    })
    field_id += 1

# 还款记录
repayment_fields = [
    ("repayment_time", "还款时间", "Datetime", False, "2025-01-15 12:23:36"),
    ("repayment_type", "还款类型", "Enum", False, "部分还款"),
    ("repayment_channel", "还款渠道", "String", False, "GCash"),
    ("repayment_amount", "还款金额", "Decimal", False, "500"),
    ("repayment_result", "还款结果", "Enum", False, "成功"),
    ("bill_id", "对应账单ID", "String", True, "BILL20250315001"),
    ("repayment_installment_no", "对应期数", "Integer", False, "1"),
    ("transaction_id", "交易流水号", "String", True, "TXN123456789"),
    ("transaction_time", "交易时间", "Datetime", True, "2025-01-15 12:23:39"),
    ("transaction_fee", "手续费", "Decimal", True, "5"),
    ("received_amount", "实收金额", "Decimal", True, "495"),
    ("repayment_waived_amount", "减免金额", "Decimal", True, "10"),
    ("repayment_remark", "还款备注", "String", True, "手动修正"),
]
for field_key, field_name, field_type, is_extended, example in repayment_fields:
    standard_fields.append({
        "id": field_id,
        "field_key": field_key,
        "field_name": field_name,
        "field_type": field_type,
        "field_group_id": 8,
        "is_required": False,
        "is_extended": is_extended,
        "example_value": example,
        "sort_order": field_id,
    })
    field_id += 1

# 催记
collection_fields = [
    ("communication_time", "沟通时间", "Datetime", False, "2025-01-05 14:22:33"),
    ("contact_person", "沟通人", "String", False, "本人"),
    ("contact_relation", "沟通关系", "Enum", False, "本人"),
    ("contacted_number", "触达号码", "String", False, "+86 13800138000"),
    ("collector_name", "催员姓名", "String", False, "Agent_Ana"),
    ("communication_channel", "沟通方式", "Enum", False, "电话"),
    ("call_duration", "通话时长", "Integer", True, "86"),
    ("call_record_url", "录音链接", "String", True, "https://.../record.mp3"),
    ("collection_remark", "备注", "String", False, "借款人承诺月底还款"),
    ("communication_status", "沟通状态", "Enum", False, "可联"),
    ("communication_result", "沟通结果", "Enum", False, "承诺还款"),
    ("next_contact_time", "下一次联系时间", "Datetime", False, "2025-01-12 10:00:00"),
    ("ptp_time", "PTP承诺还款时间", "Datetime", False, "2025-01-31 00:00:00"),
]
for field_key, field_name, field_type, is_extended, example in collection_fields:
    standard_fields.append({
        "id": field_id,
        "field_key": field_key,
        "field_name": field_name,
        "field_type": field_type,
        "field_group_id": 9,
        "is_required": False,
        "is_extended": is_extended,
        "example_value": example,
        "sort_order": field_id,
    })
    field_id += 1

# 添加标准属性
for field in standard_fields:
    field.update({
        "field_name_en": field["field_key"].replace("_", " ").title(),
        "description": f"{field['field_name']}",
        "validation_rules": None,
        "enum_options": None,
        "is_active": True,
        "is_deleted": False,
        "deleted_at": None,
        "created_at": "2025-01-01T00:00:00",
        "updated_at": "2025-01-01T00:00:00",
    })

# 添加标准属性到分组
for group in field_groups:
    group.update({
        "is_active": True,
        "created_at": "2025-01-01T00:00:00",
        "updated_at": "2025-01-01T00:00:00",
    })

# 输出JSON
print(json.dumps({
    "field_groups": field_groups,
    "standard_fields": standard_fields,
    "total_groups": len(field_groups),
    "total_fields": len(standard_fields),
}, ensure_ascii=False, indent=2))

