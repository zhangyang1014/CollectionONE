#!/usr/bin/env python3
"""
生成虚拟案件数据
"""
import json
import random
from datetime import datetime, timedelta

# 墨西哥常见姓名
mexico_first_names = [
    "Miguel", "José", "Juan", "Carlos", "Luis", "Antonio", "Francisco", "Alejandro",
    "María", "Guadalupe", "Rosa", "Ana", "Juana", "Carmen", "Elena", "Martha",
    "Pedro", "Jorge", "Rafael", "Fernando", "Ricardo", "Roberto", "Manuel", "Diego"
]

mexico_last_names = [
    "García", "Rodríguez", "Martínez", "Hernández", "López", "González", "Pérez", "Sánchez",
    "Ramírez", "Torres", "Flores", "Rivera", "Gómez", "Díaz", "Cruz", "Morales",
    "Reyes", "Jiménez", "Álvarez", "Ruiz", "Castillo", "Mendoza", "Vargas", "Romero"
]

# 印度常见姓名
india_first_names = [
    "Raj", "Amit", "Rohit", "Rahul", "Vikram", "Sanjay", "Arjun", "Karan",
    "Priya", "Anjali", "Neha", "Pooja", "Kavya", "Riya", "Shreya", "Divya",
    "Suresh", "Kumar", "Anil", "Deepak", "Manoj", "Ravi", "Naveen", "Ajay"
]

india_last_names = [
    "Kumar", "Singh", "Sharma", "Patel", "Gupta", "Reddy", "Rao", "Nair",
    "Agarwal", "Joshi", "Desai", "Menon", "Verma", "Mishra", "Iyer", "Khan",
    "Mehta", "Pandey", "Bhatia", "Kapoor", "Malhotra", "Chopra", "Bansal", "Saxena"
]

def generate_btq_cases(start_id=1, count=35):
    """生成BTQ（墨西哥）单期贷款案件"""
    cases = []
    base_date = datetime(2024, 6, 1)
    
    for i in range(count):
        case_id = start_id + i
        first_name = random.choice(mexico_first_names)
        last_name = random.choice(mexico_last_names)
        user_name = f"{first_name} {last_name}"
        user_id = f"MX{10000 + case_id}"
        loan_id = f"BTQ-{100000 + case_id}"
        mobile = f"+52 55 {random.randint(1000, 9999)} {random.randint(1000, 9999)}"
        
        # 随机生成贷款金额（500-10000 MXN）
        loan_amount = random.randint(50, 1000) * 10
        
        # 随机逾期天数（-7到60天）
        overdue_days = random.randint(-7, 60)
        
        # 根据逾期情况确定状态和金额
        if overdue_days < -1:
            case_status = "进行中"
            outstanding_amount = loan_amount
            total_due_amount = loan_amount
            settlement_method = ""
            settlement_time = ""
        elif overdue_days <= 0:
            # 当日或即将到期
            case_status = "进行中"
            outstanding_amount = loan_amount
            total_due_amount = loan_amount
            settlement_method = ""
            settlement_time = ""
        elif overdue_days <= 30:
            # 轻度逾期
            case_status = "逾期"
            # 未还金额 + 罚息
            penalty = loan_amount * 0.05 * overdue_days
            outstanding_amount = round(loan_amount + penalty, 2)
            total_due_amount = round(loan_amount + penalty, 2)
            settlement_method = ""
            settlement_time = ""
        else:
            # 严重逾期
            case_status = "逾期"
            penalty = loan_amount * 0.05 * overdue_days
            outstanding_amount = round(loan_amount + penalty, 2)
            total_due_amount = round(loan_amount + penalty, 2)
            settlement_method = ""
            settlement_time = ""
        
        # 20%的案件已结清
        if random.random() < 0.2:
            case_status = "已结清"
            outstanding_amount = 0
            settlement_method = random.choice(["主动还款", "自动扣款", "催收还款"])
            days_ago = random.randint(1, 30)
            settlement_time = (datetime.now() - timedelta(days=days_ago)).strftime("%Y-%m-%dT%H:%M:%S")
        
        # 产品和App
        product = random.choice(["Préstamo Rápido", "Cash Express", "Dinero Ya"])
        app = random.choice(["PesoMex", "DineroFácil", "CashMexico"])
        
        # 创建时间
        days_old = random.randint(10, 180)
        created_at = (base_date + timedelta(days=days_old)).strftime("%Y-%m-%dT%H:%M:%S")
        updated_at = (datetime.now() - timedelta(days=random.randint(0, 7))).strftime("%Y-%m-%dT%H:%M:%S")
        
        # 自定义字段（BTQ特有 - 单期贷款）
        custom_fields = {
            "loan_term": "单期",
            "disbursement_amount": loan_amount,
            "interest_rate": f"{random.randint(15, 35)}%",
            "collection_priority": random.choice(["高", "中", "低"]),
            "payment_method": random.choice(["SPEI", "OXXO", "银行转账"]),
            "risk_level": random.choice(["A", "B", "C", "D"])
        }
        
        case = {
            "id": case_id,
            "case_id": f"CASE{str(case_id).zfill(6)}",
            "tenant_id": 1,
            "loan_id": loan_id,
            "user_id": user_id,
            "user_name": user_name,
            "user_first_name": first_name,
            "mobile_number": mobile,
            "overdue_days": overdue_days,
            "outstanding_amount": outstanding_amount,
            "total_due_amount": total_due_amount,
            "contact_channels": random.randint(0, 5),
            "case_status": case_status,
            "product_name": product,
            "app_name": app,
            "settlement_method": settlement_method,
            "settlement_time": settlement_time,
            "standard_fields": {},
            "custom_fields": custom_fields,
            "created_at": created_at,
            "updated_at": updated_at
        }
        cases.append(case)
    
    return cases


def generate_btsk_cases(start_id=100, count=40):
    """生成BTSK（印度）多期贷款案件"""
    cases = []
    base_date = datetime(2024, 8, 1)
    
    for i in range(count):
        case_id = start_id + i
        first_name = random.choice(india_first_names)
        last_name = random.choice(india_last_names)
        user_name = f"{first_name} {last_name}"
        user_id = f"IN{20000 + case_id}"
        loan_id = f"BTSK-{200000 + case_id}"
        mobile = f"+91 {random.randint(70, 99)} {random.randint(1000, 9999)} {random.randint(1000, 9999)}"
        
        # 随机生成多期贷款
        total_installments = random.choice([3, 6, 9, 12])  # 总期数
        current_installment = random.randint(1, total_installments)  # 当前期数
        installment_amount = random.randint(200, 1000) * 10  # 每期金额（INR）
        
        # 总贷款金额
        total_loan = installment_amount * total_installments
        
        # 剩余应还金额
        remaining_installments = total_installments - current_installment + 1
        total_due_amount = installment_amount * remaining_installments
        
        # 随机逾期天数（-7到45天）
        overdue_days = random.randint(-7, 45)
        
        # 根据逾期情况确定状态和金额
        if overdue_days < -1:
            case_status = "进行中"
            outstanding_amount = installment_amount
            settlement_method = ""
            settlement_time = ""
        elif overdue_days <= 0:
            case_status = "进行中"
            outstanding_amount = installment_amount
            settlement_method = ""
            settlement_time = ""
        elif overdue_days <= 30:
            case_status = "逾期"
            # 当期未还金额 + 罚息
            penalty = installment_amount * 0.03 * overdue_days
            outstanding_amount = round(installment_amount + penalty, 2)
            settlement_method = ""
            settlement_time = ""
        else:
            case_status = "逾期"
            penalty = installment_amount * 0.03 * overdue_days
            outstanding_amount = round(installment_amount + penalty, 2)
            settlement_method = ""
            settlement_time = ""
        
        # 15%的案件已结清（还清所有期数）
        if current_installment >= total_installments and random.random() < 0.15:
            case_status = "已结清"
            outstanding_amount = 0
            total_due_amount = total_loan
            settlement_method = random.choice(["UPI支付", "银行自动扣款", "线下还款"])
            days_ago = random.randint(1, 30)
            settlement_time = (datetime.now() - timedelta(days=days_ago)).strftime("%Y-%m-%dT%H:%M:%S")
        
        # 产品和App
        product = random.choice(["Personal Loan Plus", "Easy EMI", "Quick Credit"])
        app = random.choice(["LoanKaro", "CreditMitra", "RupeeNow"])
        
        # 创建时间
        days_old = random.randint(30, 365)
        created_at = (base_date + timedelta(days=days_old)).strftime("%Y-%m-%dT%H:%M:%S")
        updated_at = (datetime.now() - timedelta(days=random.randint(0, 7))).strftime("%Y-%m-%dT%H:%M:%S")
        
        # 自定义字段（BTSK特有 - 多期贷款）
        custom_fields = {
            "loan_term": "多期",
            "total_installments": total_installments,
            "current_installment": current_installment,
            "installment_amount": installment_amount,
            "total_loan_amount": total_loan,
            "interest_rate": f"{random.randint(12, 24)}%",
            "collection_stage": random.choice(["S1", "S2", "S3", "Legal"]),
            "payment_method": random.choice(["UPI", "Net Banking", "Debit Card", "NACH"]),
            "credit_score": random.randint(300, 850),
            "employment_type": random.choice(["Salaried", "Self-employed", "Business"])
        }
        
        case = {
            "id": case_id,
            "case_id": f"CASE{str(case_id).zfill(6)}",
            "tenant_id": 2,
            "loan_id": loan_id,
            "user_id": user_id,
            "user_name": user_name,
            "user_first_name": first_name,
            "mobile_number": mobile,
            "overdue_days": overdue_days,
            "outstanding_amount": outstanding_amount,
            "total_due_amount": total_due_amount,
            "contact_channels": random.randint(0, 5),
            "case_status": case_status,
            "product_name": product,
            "app_name": app,
            "settlement_method": settlement_method,
            "settlement_time": settlement_time,
            "standard_fields": {},
            "custom_fields": custom_fields,
            "created_at": created_at,
            "updated_at": updated_at
        }
        cases.append(case)
    
    return cases


if __name__ == "__main__":
    # 生成案件 - 每个机构25个，总共50个
    btq_cases = generate_btq_cases(start_id=1, count=25)
    btsk_cases = generate_btsk_cases(start_id=100, count=25)
    
    all_cases = btq_cases + btsk_cases
    
    # 保存到文件
    output = {
        "btq_cases": btq_cases,
        "btsk_cases": btsk_cases,
        "all_cases": all_cases,
        "summary": {
            "btq_count": len(btq_cases),
            "btsk_count": len(btsk_cases),
            "total_count": len(all_cases)
        }
    }
    
    with open('generated_cases.json', 'w', encoding='utf-8') as f:
        json.dump(output, f, ensure_ascii=False, indent=2)
    
    print(f"✅ 生成完成！")
    print(f"  BTQ（墨西哥）单期案件: {len(btq_cases)}个")
    print(f"  BTSK（印度）多期案件: {len(btsk_cases)}个")
    print(f"  总计: {len(all_cases)}个案件")
    print(f"  已保存到: generated_cases.json")

