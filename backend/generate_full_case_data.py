#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成完整的案件详情数据，包含所有字段分组
"""

import json
import random
from datetime import datetime, timedelta

def generate_customer_basic_info():
    """生成客户基本信息"""
    return {
        # 基础身份信息
        "user_id": f"MX{random.randint(10000, 99999)}",
        "user_name": random.choice(["Luis Cruz", "María González", "José Ramírez", "Ana Martínez"]),
        "id_number": f"CURP{random.randint(1000000000, 9999999999)}",
        "id_type": "CURP",
        "birth_date": f"199{random.randint(0, 9)}-{random.randint(1, 12):02d}-{random.randint(1, 28):02d}",
        "gender": random.choice(["男", "女"]),
        "age": random.randint(22, 45),
        
        # 联系信息
        "mobile_number": f"+52 55 {random.randint(1000, 9999)} {random.randint(1000, 9999)}",
        "email": f"user{random.randint(100, 999)}@example.com",
        "whatsapp_number": f"+52 55 {random.randint(1000, 9999)} {random.randint(1000, 9999)}",
        
        # 地址信息
        "state": random.choice(["Ciudad de México", "Jalisco", "Nuevo León"]),
        "city": random.choice(["México", "Guadalajara", "Monterrey"]),
        "address": f"Calle {random.randint(1, 100)} #{random.randint(1, 500)}",
        "postal_code": f"{random.randint(10000, 99999)}",
        
        # 教育
        "education_level": random.choice(["高中", "大专", "本科", "研究生"]),
        
        # 职业信息
        "employment_type": random.choice(["全职", "兼职", "自雇", "无业"]),
        "company_name": f"Company {random.randint(1, 100)}",
        "monthly_income": random.randint(8000, 30000),
        "work_years": random.randint(1, 10),
        
        # 用户行为与信用
        "credit_score": random.randint(550, 800),
        "total_loans": random.randint(1, 5),
        "overdue_times": random.randint(0, 3),
        "last_login_time": (datetime.now() - timedelta(days=random.randint(0, 30))).strftime("%Y-%m-%d %H:%M:%S"),
        
        # 紧急联系人
        "emergency_contact_name": random.choice(["Pedro García", "Laura López", "Carlos Sánchez"]),
        "emergency_contact_relation": random.choice(["配偶", "父亲", "母亲", "朋友"]),
        "emergency_contact_phone": f"+52 55 {random.randint(1000, 9999)} {random.randint(1000, 9999)}",
    }

def generate_document_images():
    """生成影像资料"""
    return {
        "id_front_image": f"https://example.com/images/id_front_{random.randint(1000, 9999)}.jpg",
        "id_back_image": f"https://example.com/images/id_back_{random.randint(1000, 9999)}.jpg",
        "live_photo": f"https://example.com/images/live_{random.randint(1000, 9999)}.jpg",
        "document_status": random.choice(["待审核", "已审核", "需复核"]),
        "document_verification": {
            "is_fake_id": False,
            "is_fake_live_photo": False,
            "is_mismatch": False,
            "has_other_issue": False,
            "verified_at": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            "verified_by": "系统自动审核",
            "remark": ""
        }
    }

def generate_loan_details(is_multi_term=False):
    """生成贷款详情"""
    loan_amount = random.randint(3000, 15000)
    interest_rate = random.randint(25, 40)
    
    data = {
        "loan_id": f"BTQ-{random.randint(100000, 999999)}",
        "loan_type": "多期" if is_multi_term else "单期",
        "loan_source": random.choice(["App直接申请", "贷超导流", "线下推广"]),
        "product_name": random.choice(["Cash Express", "Quick Loan", "Easy Money"]),
        "app_name": random.choice(["PesoMex", "DineroRápido", "PrestaYa"]),
        
        # 借款合同信息
        "contract_number": f"CON{random.randint(100000000, 999999999)}",
        "contract_sign_date": (datetime.now() - timedelta(days=random.randint(30, 180))).strftime("%Y-%m-%d"),
        "contract_amount": loan_amount,
        "contract_term": "7天" if not is_multi_term else f"{random.randint(3, 12)}个月",
        "interest_rate": f"{interest_rate}%",
        "service_fee": random.randint(100, 500),
        "contract_file_url": f"https://example.com/contracts/contract_{random.randint(1000, 9999)}.pdf",
        
        # 放款信息
        "disbursement_amount": loan_amount,
        "disbursement_date": (datetime.now() - timedelta(days=random.randint(25, 175))).strftime("%Y-%m-%d"),
        "disbursement_status": "已放款",
        "transaction_id": f"TXN{random.randint(10000000000, 99999999999)}",
        "recipient_name": random.choice(["Luis Cruz", "María González"]),
        "bank_name": random.choice(["BBVA Bancomer", "Santander", "Banorte"]),
        "bank_account": f"****{random.randint(1000, 9999)}",
        
        # 应还信息
        "total_due_amount": int(loan_amount * (1 + interest_rate / 100)),
        "outstanding_amount": random.randint(0, int(loan_amount * (1 + interest_rate / 100))),
        "due_date": (datetime.now() - timedelta(days=random.randint(-10, 50))).strftime("%Y-%m-%d"),
        "overdue_days": random.randint(-5, 50),
        "overdue_penalty": random.randint(0, 2000),
    }
    
    return data

def generate_installment_details():
    """生成分期详情（多期贷款）"""
    total_installments = random.randint(3, 12)
    current_installment = random.randint(1, total_installments)
    installment_amount = random.randint(800, 2000)
    
    installments = []
    for i in range(1, total_installments + 1):
        due_date = datetime.now() - timedelta(days=(total_installments - i) * 30)
        status = "已还清" if i < current_installment else ("逾期" if i == current_installment and random.random() > 0.5 else "待还款")
        
        installments.append({
            "installment_number": i,
            "due_date": due_date.strftime("%Y-%m-%d"),
            "due_amount": installment_amount,
            "paid_amount": installment_amount if status == "已还清" else 0,
            "outstanding_amount": 0 if status == "已还清" else installment_amount,
            "status": status,
            "payment_date": due_date.strftime("%Y-%m-%d") if status == "已还清" else None,
            "overdue_days": max(0, (datetime.now() - due_date).days) if status != "已还清" else 0,
        })
    
    return {
        "total_installments": total_installments,
        "current_installment": current_installment,
        "installment_amount": installment_amount,
        "installments": installments,
        "payment_qr_code": f"https://example.com/qr/payment_{random.randint(1000, 9999)}.png"
    }

def generate_loan_history():
    """生成历史借款记录"""
    history_count = random.randint(0, 3)
    history = []
    
    for i in range(history_count):
        loan_amount = random.randint(2000, 10000)
        history.append({
            "loan_id": f"BTQ-{random.randint(100000, 999999)}",
            "loan_date": (datetime.now() - timedelta(days=random.randint(180, 730))).strftime("%Y-%m-%d"),
            "loan_amount": loan_amount,
            "repay_date": (datetime.now() - timedelta(days=random.randint(150, 700))).strftime("%Y-%m-%d"),
            "repay_amount": int(loan_amount * 1.3),
            "status": random.choice(["已结清", "逾期已还", "正常还款"]),
            "overdue_days": random.randint(0, 30) if random.random() > 0.5 else 0,
        })
    
    return history

def generate_payment_records():
    """生成还款记录"""
    record_count = random.randint(0, 5)
    records = []
    
    for i in range(record_count):
        amount = random.randint(500, 5000)
        records.append({
            "payment_id": f"PAY{random.randint(10000000, 99999999)}",
            "payment_date": (datetime.now() - timedelta(days=random.randint(1, 60))).strftime("%Y-%m-%d %H:%M:%S"),
            "payment_amount": amount,
            "payment_method": random.choice(["SPEI", "OXXO", "银行转账", "借记卡"]),
            "transaction_id": f"TXN{random.randint(10000000000, 99999999999)}",
            "payment_status": random.choice(["成功", "处理中", "失败"]),
            "payment_channel": random.choice(["App", "网页", "ATM"]),
            "remark": random.choice(["正常还款", "部分还款", "逾期还款", ""]),
        })
    
    return records

def generate_full_case(case_id, tenant_id, is_multi_term=False):
    """生成完整案件数据"""
    customer_info = generate_customer_basic_info()
    loan_details = generate_loan_details(is_multi_term)
    
    case_data = {
        "id": case_id,
        "case_id": f"CASE{case_id:06d}",
        "tenant_id": tenant_id,
        "created_at": (datetime.now() - timedelta(days=random.randint(30, 180))).strftime("%Y-%m-%d %H:%M:%S"),
        "updated_at": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        
        # 案件状态
        "case_status": random.choice(["待催收", "催收中", "已结清", "承诺还款"]),
        "collector_id": f"COL{random.randint(100, 999)}",
        "collection_priority": random.choice(["高", "中", "低"]),
        
        # 客户基本信息
        "customer_basic_info": customer_info,
        
        # 影像资料
        "document_images": generate_document_images(),
        
        # 贷款详情
        "loan_details": loan_details,
        
        # 历史借款记录
        "loan_history": generate_loan_history(),
        
        # 还款记录
        "payment_records": generate_payment_records(),
    }
    
    # 如果是多期贷款，添加分期详情
    if is_multi_term:
        case_data["installment_details"] = generate_installment_details()
    
    return case_data

if __name__ == "__main__":
    # 生成BTQ（单期）和BTSK（多期）的完整案件数据
    btq_case = generate_full_case(1, 1, is_multi_term=False)
    btsk_case = generate_full_case(2, 2, is_multi_term=True)
    
    output = {
        "btq_sample": btq_case,
        "btsk_sample": btsk_case
    }
    
    with open('full_case_data_sample.json', 'w', encoding='utf-8') as f:
        json.dump(output, f, ensure_ascii=False, indent=2)
    
    print("✅ 完整案件数据样本已生成！")
    print(f"  文件: full_case_data_sample.json")

