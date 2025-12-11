/**
 * 标准字段数据 - 基于CSV文件内容
 * 
 * 版本变更记录：
 * | 版本 | 日期 | 变更内容 | 变更人 |
 * |------|------|----------|--------|
 * | 1.0.0 | 2025-12-11 | 初始创建，整合所有CSV字段数据 | 大象 |
 */

export interface StandardField {
  field_name: string          // 字段名称
  field_key: string           // 英文名/字段标识
  field_type: string          // 类型
  example?: string            // 示例
  description: string         // 说明
  is_extension: boolean       // 是否为拓展字段
  enum_values?: string[]      // 枚举值（如果是Enum类型）
  group_key: string           // 分组标识
  group_name: string          // 分组名称
}

export interface FieldGroup {
  group_key: string
  group_name: string
  group_name_en: string
  parent_key?: string         // 父分组标识
  fields: StandardField[]
}

/**
 * 客户基础信息 - 基础身份信息
 */
const identityInfoFields: StandardField[] = [
  {
    field_name: '用户编号',
    field_key: 'user_id',
    field_type: 'String',
    example: '5983',
    description: '系统内部唯一用户标识',
    is_extension: false,
    group_key: 'identity_info',
    group_name: '基础身份信息'
  },
  {
    field_name: '用户姓名',
    field_key: 'user_name',
    field_type: 'String',
    example: 'Juan Dela Cruz',
    description: '借款人姓名',
    is_extension: false,
    group_key: 'identity_info',
    group_name: '基础身份信息'
  },
  {
    field_name: '性别',
    field_key: 'gender',
    field_type: 'Enum',
    example: 'Male / Female',
    description: '用户性别',
    is_extension: false,
    enum_values: ['Male', 'Female'],
    group_key: 'identity_info',
    group_name: '基础身份信息'
  },
  {
    field_name: '出生日期',
    field_key: 'birth_date',
    field_type: 'Date',
    example: '1980/5/5',
    description: '用户生日',
    is_extension: false,
    group_key: 'identity_info',
    group_name: '基础身份信息'
  },
  {
    field_name: '国籍',
    field_key: 'nationality',
    field_type: 'String',
    example: 'Philippines',
    description: '国籍或居住国家',
    is_extension: true,
    group_key: 'identity_info',
    group_name: '基础身份信息'
  },
  {
    field_name: '婚姻状况',
    field_key: 'marital_status',
    field_type: 'Enum',
    example: 'Single / Married',
    description: '婚姻状态',
    is_extension: false,
    enum_values: ['Single', 'Married'],
    group_key: 'identity_info',
    group_name: '基础身份信息'
  },
  {
    field_name: '证件类型',
    field_key: 'id_type',
    field_type: 'Enum',
    example: 'National ID / Passport / Driver\'s License',
    description: '身份证件类型',
    is_extension: false,
    enum_values: ['National ID', 'Passport', 'Driver\'s License'],
    group_key: 'identity_info',
    group_name: '基础身份信息'
  },
  {
    field_name: '证件号码',
    field_key: 'id_number',
    field_type: 'String',
    example: 'N2319594759',
    description: '证件号码',
    is_extension: false,
    group_key: 'identity_info',
    group_name: '基础身份信息'
  },
  {
    field_name: '居住地址',
    field_key: 'address',
    field_type: 'String',
    example: '123 Main St, Quezon City',
    description: '包含街道、城市、省份、邮编',
    is_extension: false,
    group_key: 'identity_info',
    group_name: '基础身份信息'
  },
  {
    field_name: '居住年限',
    field_key: 'years_at_address',
    field_type: 'Integer',
    example: '5',
    description: '当前居住地年数',
    is_extension: false,
    group_key: 'identity_info',
    group_name: '基础身份信息'
  },
  {
    field_name: '居住类型',
    field_key: 'housing_type',
    field_type: 'Enum',
    example: 'Own / Rent / With Family',
    description: '居住情况',
    is_extension: false,
    enum_values: ['Own', 'Rent', 'With Family'],
    group_key: 'identity_info',
    group_name: '基础身份信息'
  }
]

/**
 * 客户基础信息 - 教育信息
 */
const educationFields: StandardField[] = [
  {
    field_name: '教育程度',
    field_key: 'education_level',
    field_type: 'Enum',
    example: 'University / High School',
    description: '最高学历',
    is_extension: false,
    enum_values: ['University', 'High School'],
    group_key: 'education_info',
    group_name: '教育信息'
  },
  {
    field_name: '学校名称',
    field_key: 'school_name',
    field_type: 'String',
    example: 'University of the Philippines',
    description: '毕业学校或当前学校',
    is_extension: false,
    group_key: 'education_info',
    group_name: '教育信息'
  },
  {
    field_name: '专业',
    field_key: 'major',
    field_type: 'String',
    example: 'Business Administration',
    description: '学习或毕业专业',
    is_extension: false,
    group_key: 'education_info',
    group_name: '教育信息'
  }
]

/**
 * 客户基础信息 - 职业信息
 */
const employmentFields: StandardField[] = [
  {
    field_name: '工作状态',
    field_key: 'employment_status',
    field_type: 'Enum',
    example: 'Employed / Self-employed / Student / Unemployed',
    description: '当前职业状态',
    is_extension: false,
    enum_values: ['Employed', 'Self-employed', 'Student', 'Unemployed'],
    group_key: 'employment_info',
    group_name: '职业信息'
  },
  {
    field_name: '公司名称',
    field_key: 'company_name',
    field_type: 'String',
    example: 'ABC Transport',
    description: '所在公司',
    is_extension: false,
    group_key: 'employment_info',
    group_name: '职业信息'
  },
  {
    field_name: '职位',
    field_key: 'job_title',
    field_type: 'String',
    example: 'Driver',
    description: '当前职位或职称',
    is_extension: false,
    group_key: 'employment_info',
    group_name: '职业信息'
  },
  {
    field_name: '行业类别',
    field_key: 'industry',
    field_type: 'String',
    example: 'Transportation',
    description: '所属行业',
    is_extension: false,
    group_key: 'employment_info',
    group_name: '职业信息'
  },
  {
    field_name: '工作年限',
    field_key: 'years_of_employment',
    field_type: 'Integer',
    example: '3',
    description: '当前岗位工作时长',
    is_extension: false,
    group_key: 'employment_info',
    group_name: '职业信息'
  },
  {
    field_name: '工作地址',
    field_key: 'work_address',
    field_type: 'String',
    example: '15 Pasay Ave, Manila',
    description: '工作地点',
    is_extension: false,
    group_key: 'employment_info',
    group_name: '职业信息'
  },
  {
    field_name: '收入类型',
    field_key: 'income_type',
    field_type: 'Enum',
    example: 'Monthly / Weekly / Daily',
    description: '工资发放周期',
    is_extension: false,
    enum_values: ['Monthly', 'Weekly', 'Daily'],
    group_key: 'employment_info',
    group_name: '职业信息'
  },
  {
    field_name: '发薪日',
    field_key: 'payday',
    field_type: 'String',
    example: '15th / End of Month',
    description: '发薪时间',
    is_extension: false,
    group_key: 'employment_info',
    group_name: '职业信息'
  },
  {
    field_name: '收入',
    field_key: 'income_range',
    field_type: 'String',
    example: '32000 PHP – 64000 PHP',
    description: '每月或每周收入区间',
    is_extension: false,
    group_key: 'employment_info',
    group_name: '职业信息'
  },
  {
    field_name: '收入来源',
    field_key: 'income_source',
    field_type: 'Enum',
    example: 'Primary / Secondary',
    description: '主或副收入来源',
    is_extension: false,
    enum_values: ['Primary', 'Secondary'],
    group_key: 'employment_info',
    group_name: '职业信息'
  },
  {
    field_name: '收入证明文件',
    field_key: 'income_proof_files',
    field_type: 'FileList',
    example: 'payslip.pdf / bank_statement.jpg',
    description: '上传的收入凭证文件',
    is_extension: true,
    group_key: 'employment_info',
    group_name: '职业信息'
  }
]

/**
 * 客户基础信息 - 用户行为与信用
 */
const behaviorCreditFields: StandardField[] = [
  {
    field_name: '最近打开时间',
    field_key: 'last_app_open_time',
    field_type: 'Datetime',
    example: '2025/10/23 12:02:21',
    description: '用户最近一次打开借款App的时间',
    is_extension: false,
    group_key: 'behavior_credit',
    group_name: '用户行为与信用'
  },
  {
    field_name: '最近访问还款页时间',
    field_key: 'last_repayment_page_visit_time',
    field_type: 'Datetime',
    example: '2025/10/26 02:01:51',
    description: '用户最近访问还款页面的时间',
    is_extension: false,
    group_key: 'behavior_credit',
    group_name: '用户行为与信用'
  },
  {
    field_name: '历史借款总笔数',
    field_key: 'total_loan_count',
    field_type: 'Integer',
    example: '5',
    description: '统计借款人累计放款次数',
    is_extension: false,
    group_key: 'behavior_credit',
    group_name: '用户行为与信用'
  },
  {
    field_name: '已结清笔数',
    field_key: 'cleared_loan_count',
    field_type: 'Integer',
    example: '3',
    description: '已全额还清的贷款订单数量',
    is_extension: false,
    group_key: 'behavior_credit',
    group_name: '用户行为与信用'
  },
  {
    field_name: '历史逾期笔数',
    field_key: 'overdue_loan_count',
    field_type: 'Integer',
    example: '2',
    description: '曾经发生逾期的订单数量',
    is_extension: false,
    group_key: 'behavior_credit',
    group_name: '用户行为与信用'
  },
  {
    field_name: '历史最大逾期天数',
    field_key: 'max_overdue_days',
    field_type: 'Integer',
    example: '15',
    description: '用户历史上最长一次逾期天数',
    is_extension: false,
    group_key: 'behavior_credit',
    group_name: '用户行为与信用'
  },
  {
    field_name: '平均借款金额',
    field_key: 'avg_loan_amount',
    field_type: 'Decimal',
    example: '1500',
    description: '用户历次贷款的平均放款金额',
    is_extension: false,
    group_key: 'behavior_credit',
    group_name: '用户行为与信用'
  },
  {
    field_name: '001信用评分',
    field_key: 'credit_score_001',
    field_type: 'Enum',
    example: 'A',
    description: '系统1计算的信用等级',
    is_extension: false,
    enum_values: ['A', 'B', 'C', 'D'],
    group_key: 'behavior_credit',
    group_name: '用户行为与信用'
  },
  {
    field_name: '002信用评分',
    field_key: 'credit_score_002',
    field_type: 'Enum',
    example: 'B',
    description: '系统2计算的信用等级',
    is_extension: false,
    enum_values: ['A', 'B', 'C', 'D'],
    group_key: 'behavior_credit',
    group_name: '用户行为与信用'
  },
  {
    field_name: '003信用评分',
    field_key: 'credit_score_003',
    field_type: 'Enum',
    example: 'C',
    description: '系统3计算的信用等级',
    is_extension: false,
    enum_values: ['A', 'B', 'C', 'D'],
    group_key: 'behavior_credit',
    group_name: '用户行为与信用'
  }
]

/**
 * 客户基础信息 - 联系方式
 */
const contactFields: StandardField[] = [
  {
    field_name: '手机号码',
    field_key: 'mobile_number',
    field_type: 'String',
    example: '+63 9123456789',
    description: '用户注册手机号',
    is_extension: false,
    group_key: 'contact_info',
    group_name: '联系方式'
  },
  {
    field_name: '公司联系电话',
    field_key: 'company_phone',
    field_type: 'String',
    example: '+63 2 9123456',
    description: '公司电话',
    is_extension: false,
    group_key: 'contact_info',
    group_name: '联系方式'
  }
]

/**
 * 贷款详情
 */
const loanDetailsFields: StandardField[] = [
  {
    field_name: '贷款编号',
    field_key: 'loan_id',
    field_type: 'String',
    example: '123',
    description: '系统生成的唯一标识',
    is_extension: false,
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '案件状态',
    field_key: 'case_status',
    field_type: 'Enum',
    example: '未结清 / 逾期 / 结清',
    description: '当前借款订单状态',
    is_extension: false,
    enum_values: ['未结清', '逾期', '结清'],
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '产品类别',
    field_key: 'product_type',
    field_type: 'String',
    example: '借款订单',
    description: '区分借款类型（借款 / 展期等）',
    is_extension: false,
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '放款时间',
    field_key: 'disbursement_time',
    field_type: 'Datetime',
    example: '2025/2/26 01:00:00',
    description: '实际放款到账时间',
    is_extension: false,
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '应还金额',
    field_key: 'total_due_amount',
    field_type: 'Decimal',
    example: '1460',
    description: '合同约定应还总额',
    is_extension: false,
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '已还金额',
    field_key: 'total_paid_amount',
    field_type: 'Decimal',
    example: '975',
    description: '用户已偿还金额',
    is_extension: false,
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '应还未还金额',
    field_key: 'outstanding_amount',
    field_type: 'Decimal',
    example: '485',
    description: '当前未偿还金额',
    is_extension: false,
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '应收本金',
    field_key: 'principal_due',
    field_type: 'Decimal',
    example: '1000',
    description: '合同本金部分',
    is_extension: false,
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '应收利息',
    field_key: 'interest_due',
    field_type: 'Decimal',
    example: '180',
    description: '合同利息部分',
    is_extension: false,
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '服务费',
    field_key: 'service_fee',
    field_type: 'Decimal',
    example: '270',
    description: '手续费或管理费',
    is_extension: false,
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '应收罚息',
    field_key: 'penalty_fee',
    field_type: 'Decimal',
    example: '10',
    description: '逾期罚息',
    is_extension: false,
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '代扣账号',
    field_key: 'account_number',
    field_type: 'String',
    example: '98546121',
    description: '用户绑定还款账号',
    is_extension: false,
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '开户行',
    field_key: 'bank_name',
    field_type: 'String',
    example: 'GCash',
    description: '代扣银行或电子钱包名称',
    is_extension: false,
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '借款App',
    field_key: 'loan_app',
    field_type: 'String',
    example: 'MegaPeso',
    description: '借款来源平台',
    is_extension: false,
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '合同编号',
    field_key: 'contract_no',
    field_type: 'String',
    example: 'MP20250226001',
    description: '与外部合同匹配的编号',
    is_extension: true,
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '放款渠道',
    field_key: 'disbursement_channel',
    field_type: 'String',
    example: 'Partner Bank',
    description: '资金来源渠道',
    is_extension: true,
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '入催时间',
    field_key: 'collection_entry_time',
    field_type: 'Datetime',
    example: '2025/3/1 00:00:00',
    description: '进入催收系统的时间',
    is_extension: true,
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '当前逾期天数',
    field_key: 'overdue_days',
    field_type: 'Integer',
    example: '3',
    description: '系统自动计算',
    is_extension: true,
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '下次催收时间',
    field_key: 'next_collection_time',
    field_type: 'Datetime',
    example: '2025/3/5 09:00:00',
    description: '系统生成或人工设定',
    is_extension: true,
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '催收状态',
    field_key: 'collection_status',
    field_type: 'Enum',
    example: '未联系 / 承诺还款 / 已结清',
    description: '当前催收进度',
    is_extension: true,
    enum_values: ['未联系', '承诺还款', '已结清'],
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '催收阶段',
    field_key: 'collection_stage',
    field_type: 'Enum',
    example: '早期 / 中期 / 后期',
    description: '按逾期天数划分',
    is_extension: true,
    enum_values: ['早期', '中期', '后期'],
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '还款方式',
    field_key: 'repayment_method',
    field_type: 'Enum',
    example: '银行转账 / 钱包 / 门店',
    description: '当前订单的还款途径',
    is_extension: true,
    enum_values: ['银行转账', '钱包', '门店'],
    group_key: 'loan_details',
    group_name: '贷款详情'
  },
  {
    field_name: '最近还款日期',
    field_key: 'last_payment_date',
    field_type: 'Datetime',
    example: '2025/2/28 14:33:22',
    description: '上一次部分或全部还款时间',
    is_extension: true,
    group_key: 'loan_details',
    group_name: '贷款详情'
  }
]

/**
 * 分期详情
 */
const installmentFields: StandardField[] = [
  {
    field_name: '期数',
    field_key: 'installment_no',
    field_type: 'Integer',
    example: '1',
    description: '分期编号',
    is_extension: false,
    group_key: 'installment_details',
    group_name: '分期详情'
  },
  {
    field_name: '状态',
    field_key: 'installment_status',
    field_type: 'Enum',
    example: '待还款 / 已还清',
    description: '当前期状态',
    is_extension: false,
    enum_values: ['待还款', '已还清'],
    group_key: 'installment_details',
    group_name: '分期详情'
  },
  {
    field_name: '应还款时间',
    field_key: 'due_date',
    field_type: 'Date',
    example: '2025/3/13',
    description: '应还日期',
    is_extension: false,
    group_key: 'installment_details',
    group_name: '分期详情'
  },
  {
    field_name: '逾期天数',
    field_key: 'overdue_days',
    field_type: 'Integer',
    example: '1',
    description: '超过应还日期未还的天数',
    is_extension: false,
    group_key: 'installment_details',
    group_name: '分期详情'
  },
  {
    field_name: '应还金额',
    field_key: 'due_amount',
    field_type: 'Decimal',
    example: '985',
    description: '当前期应还总额',
    is_extension: false,
    group_key: 'installment_details',
    group_name: '分期详情'
  },
  {
    field_name: '实际还款时间',
    field_key: 'payment_date',
    field_type: 'Datetime',
    example: '2025/3/14 12:23:36',
    description: '实际还款完成时间',
    is_extension: false,
    group_key: 'installment_details',
    group_name: '分期详情'
  },
  {
    field_name: '实际已还金额',
    field_key: 'paid_amount',
    field_type: 'Decimal',
    example: '975',
    description: '当前期已还金额',
    is_extension: false,
    group_key: 'installment_details',
    group_name: '分期详情'
  },
  {
    field_name: '提前还款标志',
    field_key: 'early_repayment_flag',
    field_type: 'Boolean',
    example: 'FALSE',
    description: '是否提前还清该期',
    is_extension: false,
    group_key: 'installment_details',
    group_name: '分期详情'
  },
  {
    field_name: '应还未还',
    field_key: 'outstanding',
    field_type: 'Decimal',
    example: '10',
    description: '当前期未还金额',
    is_extension: false,
    group_key: 'installment_details',
    group_name: '分期详情'
  },
  {
    field_name: '应收本金',
    field_key: 'principal',
    field_type: 'Decimal',
    example: '678',
    description: '当前期本金',
    is_extension: false,
    group_key: 'installment_details',
    group_name: '分期详情'
  },
  {
    field_name: '应收利息',
    field_key: 'interest',
    field_type: 'Decimal',
    example: '30',
    description: '当前期利息',
    is_extension: false,
    group_key: 'installment_details',
    group_name: '分期详情'
  },
  {
    field_name: '应收罚息',
    field_key: 'penalty',
    field_type: 'Decimal',
    example: '10',
    description: '当前期罚息',
    is_extension: false,
    group_key: 'installment_details',
    group_name: '分期详情'
  },
  {
    field_name: '服务费',
    field_key: 'service_fee',
    field_type: 'Decimal',
    example: '270',
    description: '当前期服务费',
    is_extension: false,
    group_key: 'installment_details',
    group_name: '分期详情'
  },
  {
    field_name: '获取还款码',
    field_key: 'repayment_code',
    field_type: 'Button',
    example: '还款码',
    description: '获取二维码或还款链接',
    is_extension: false,
    group_key: 'installment_details',
    group_name: '分期详情'
  },
  {
    field_name: '减免金额',
    field_key: 'waived_amount',
    field_type: 'Decimal',
    example: '50',
    description: '当前期的费用减免',
    is_extension: true,
    group_key: 'installment_details',
    group_name: '分期详情'
  },
  {
    field_name: '分期还款渠道',
    field_key: 'installment_channel',
    field_type: 'String',
    example: 'GCash / Maya',
    description: '实际支付渠道',
    is_extension: true,
    group_key: 'installment_details',
    group_name: '分期详情'
  }
]

/**
 * 还款记录
 */
const repaymentRecordsFields: StandardField[] = [
  {
    field_name: '还款时间',
    field_key: 'repayment_time',
    field_type: 'Datetime',
    example: '2025/3/14 12:23:36',
    description: '实际完成还款的时间',
    is_extension: false,
    group_key: 'repayment_records',
    group_name: '还款记录'
  },
  {
    field_name: '还款类型',
    field_key: 'repayment_type',
    field_type: 'Enum',
    example: '部分还款 / 全额还款 / 提前还款',
    description: '表示还款行为的类型',
    is_extension: false,
    enum_values: ['部分还款', '全额还款', '提前还款'],
    group_key: 'repayment_records',
    group_name: '还款记录'
  },
  {
    field_name: '还款渠道',
    field_key: 'repayment_channel',
    field_type: 'String',
    example: 'GCash / Maya / Bank Transfer',
    description: '用户使用的支付方式或通道',
    is_extension: false,
    group_key: 'repayment_records',
    group_name: '还款记录'
  },
  {
    field_name: '还款金额',
    field_key: 'repayment_amount',
    field_type: 'Decimal',
    example: '500',
    description: '本次实际支付金额',
    is_extension: false,
    group_key: 'repayment_records',
    group_name: '还款记录'
  },
  {
    field_name: '还款结果',
    field_key: 'repayment_result',
    field_type: 'Enum',
    example: '成功 / 失败 / 处理中',
    description: '支付网关返回结果',
    is_extension: false,
    enum_values: ['成功', '失败', '处理中'],
    group_key: 'repayment_records',
    group_name: '还款记录'
  },
  {
    field_name: '对应账单ID',
    field_key: 'bill_id',
    field_type: 'String',
    example: 'BILL20250314001',
    description: '系统内账单唯一标识',
    is_extension: true,
    group_key: 'repayment_records',
    group_name: '还款记录'
  },
  {
    field_name: '对应期数',
    field_key: 'installment_no',
    field_type: 'Integer',
    example: '1',
    description: '该笔还款对应的分期编号',
    is_extension: false,
    group_key: 'repayment_records',
    group_name: '还款记录'
  },
  {
    field_name: '交易流水号',
    field_key: 'transaction_id',
    field_type: 'String',
    example: 'TXN123456789',
    description: '第三方支付或银行流水号',
    is_extension: true,
    group_key: 'repayment_records',
    group_name: '还款记录'
  },
  {
    field_name: '交易时间',
    field_key: 'transaction_time',
    field_type: 'Datetime',
    example: '2025/3/14 12:23:39',
    description: '第三方返回的实际交易完成时间',
    is_extension: true,
    group_key: 'repayment_records',
    group_name: '还款记录'
  },
  {
    field_name: '手续费',
    field_key: 'transaction_fee',
    field_type: 'Decimal',
    example: '5',
    description: '第三方代收通道费用',
    is_extension: true,
    group_key: 'repayment_records',
    group_name: '还款记录'
  },
  {
    field_name: '实收金额',
    field_key: 'received_amount',
    field_type: 'Decimal',
    example: '495',
    description: '平台实际到账金额（扣除手续费后）',
    is_extension: true,
    group_key: 'repayment_records',
    group_name: '还款记录'
  },
  {
    field_name: '减免金额',
    field_key: 'waived_amount',
    field_type: 'Decimal',
    example: '10',
    description: '本次还款中被减免的金额',
    is_extension: true,
    group_key: 'repayment_records',
    group_name: '还款记录'
  },
  {
    field_name: '还款备注',
    field_key: 'repayment_remark',
    field_type: 'String',
    example: '用户要求延后 / 手动修正',
    description: '记录异常或说明性信息',
    is_extension: true,
    group_key: 'repayment_records',
    group_name: '还款记录'
  }
]

/**
 * 历史借款记录
 */
const loanHistoryFields: StandardField[] = [
  {
    field_name: '贷款编号',
    field_key: 'loan_id',
    field_type: 'String',
    example: '100023',
    description: '系统内唯一的贷款订单编号',
    is_extension: false,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '用户编号',
    field_key: 'user_id',
    field_type: 'String',
    example: '5983',
    description: '用户唯一标识',
    is_extension: false,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '用户姓名',
    field_key: 'user_name',
    field_type: 'String',
    example: 'Juan Dela Cruz',
    description: '借款人姓名',
    is_extension: false,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '手机号码',
    field_key: 'mobile_number',
    field_type: 'String',
    example: '+63 9123456789',
    description: '用户注册手机号',
    is_extension: false,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: 'App名称',
    field_key: 'app_name',
    field_type: 'String',
    example: 'MegaPeso',
    description: '借款App来源',
    is_extension: false,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '产品名称',
    field_key: 'product_name',
    field_type: 'String',
    example: 'Cash Loan',
    description: '所属产品类型',
    is_extension: false,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '贷超商户',
    field_key: 'merchant_name',
    field_type: 'String',
    example: 'EasyLoan Partner',
    description: '放款商户或渠道方名称',
    is_extension: true,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '所属系统',
    field_key: 'system_name',
    field_type: 'String',
    example: 'CollectionSystemV2',
    description: '当前数据所属业务系统',
    is_extension: true,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: 'App下载链接',
    field_key: 'app_download_url',
    field_type: 'String',
    example: 'https://play.google.com/...',
    description: '便于催员发送下载提醒',
    is_extension: true,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '首复借类型',
    field_key: 'collection_type',
    field_type: 'Enum',
    example: '首催 / 复催',
    description: '标记案件是否为首次或再次催收',
    is_extension: false,
    enum_values: ['首催', '复催'],
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '是否复借',
    field_key: 'reborrow_flag',
    field_type: 'Boolean',
    example: 'TRUE',
    description: '用户是否为老客再次借款',
    is_extension: true,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '自动复借',
    field_key: 'auto_reloan',
    field_type: 'Boolean',
    example: 'FALSE',
    description: '是否系统自动生成复借单',
    is_extension: true,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '首期期限',
    field_key: 'first_term_days',
    field_type: 'Integer',
    example: '14',
    description: '首次借款天数或期限',
    is_extension: true,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '应还款日期',
    field_key: 'due_date',
    field_type: 'Date',
    example: '2025/3/10',
    description: '应该完成还款的日期',
    is_extension: false,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '逾期天数',
    field_key: 'overdue_days',
    field_type: 'Integer',
    example: '5',
    description: '超过应还日期的天数',
    is_extension: false,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '应还未还金额',
    field_key: 'outstanding_amount',
    field_type: 'Decimal',
    example: '850',
    description: '当前待还金额',
    is_extension: false,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '绑定人',
    field_key: 'binder',
    field_type: 'String',
    example: 'Agent_001',
    description: '当前负责该案件的催员或分配人',
    is_extension: false,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '案件状态',
    field_key: 'case_status',
    field_type: 'Enum',
    example: '进行中 / 已结清 / 逾期',
    description: '当前催收案件进度',
    is_extension: false,
    enum_values: ['进行中', '已结清', '逾期'],
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '结清方式',
    field_key: 'settlement_method',
    field_type: 'Enum',
    example: '自动扣款 / 手动转账 / 第三方收款',
    description: '最终还款方式',
    is_extension: false,
    enum_values: ['自动扣款', '手动转账', '第三方收款'],
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '结清时间',
    field_key: 'settlement_time',
    field_type: 'Datetime',
    example: '2025/3/15 09:33:00',
    description: '最后一次结清操作时间',
    is_extension: false,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '最新结果',
    field_key: 'latest_result',
    field_type: 'Enum',
    example: '承诺还款 / 无法联系 / 已结清',
    description: '催收最近一次结果',
    is_extension: false,
    enum_values: ['承诺还款', '无法联系', '已结清'],
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '最近催收时间',
    field_key: 'last_collection_time',
    field_type: 'Datetime',
    example: '2025/3/14 18:00:00',
    description: '最近一次催收操作时间',
    is_extension: false,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '处理人',
    field_key: 'operator',
    field_type: 'String',
    example: 'Agent_Ana',
    description: '执行催收操作的员工',
    is_extension: false,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '备注',
    field_key: 'remark',
    field_type: 'String',
    example: '电话无人接听',
    description: '催员填写的备注信息',
    is_extension: true,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '应还本金',
    field_key: 'principal_due',
    field_type: 'Decimal',
    example: '1000',
    description: '当前订单本金',
    is_extension: true,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  },
  {
    field_name: '应收利息',
    field_key: 'interest_due',
    field_type: 'Decimal',
    example: '150',
    description: '当前订单利息',
    is_extension: true,
    group_key: 'loan_history',
    group_name: '历史借款记录'
  }
]

/**
 * 分组定义
 */
export const fieldGroups: FieldGroup[] = [
  {
    group_key: 'customer_info',
    group_name: '客户基础信息',
    group_name_en: 'Customer Information',
    fields: []
  },
  {
    group_key: 'identity_info',
    group_name: '基础身份信息',
    group_name_en: 'Identity Information',
    parent_key: 'customer_info',
    fields: identityInfoFields
  },
  {
    group_key: 'education_info',
    group_name: '教育信息',
    group_name_en: 'Education',
    parent_key: 'customer_info',
    fields: educationFields
  },
  {
    group_key: 'employment_info',
    group_name: '职业信息',
    group_name_en: 'Employment',
    parent_key: 'customer_info',
    fields: employmentFields
  },
  {
    group_key: 'behavior_credit',
    group_name: '用户行为与信用',
    group_name_en: 'User Behavior & Credit',
    parent_key: 'customer_info',
    fields: behaviorCreditFields
  },
  {
    group_key: 'contact_info',
    group_name: '联系方式',
    group_name_en: 'Contact Information',
    parent_key: 'customer_info',
    fields: contactFields
  },
  {
    group_key: 'loan_details',
    group_name: '贷款详情',
    group_name_en: 'Loan Details',
    fields: loanDetailsFields
  },
  {
    group_key: 'installment_details',
    group_name: '分期详情',
    group_name_en: 'Installment Details',
    fields: installmentFields
  },
  {
    group_key: 'repayment_records',
    group_name: '还款记录',
    group_name_en: 'Repayment Records',
    fields: repaymentRecordsFields
  },
  {
    group_key: 'loan_history',
    group_name: '历史借款记录',
    group_name_en: 'Loan History',
    fields: loanHistoryFields
  }
]

/**
 * 获取所有标准字段（扁平化）
 */
export function getAllStandardFields(): StandardField[] {
  const allFields: StandardField[] = []
  fieldGroups.forEach(group => {
    allFields.push(...group.fields)
  })
  return allFields
}

/**
 * 根据分组key获取字段
 */
export function getFieldsByGroupKey(groupKey: string): StandardField[] {
  const group = fieldGroups.find(g => g.group_key === groupKey)
  return group?.fields || []
}

/**
 * 获取分组树结构（用于显示）
 */
export function getGroupTree() {
  // 一级分组
  const rootGroups = fieldGroups.filter(g => !g.parent_key)
  
  return rootGroups.map(root => {
    // 查找子分组
    const children = fieldGroups.filter(g => g.parent_key === root.group_key)
    
    return {
      ...root,
      field_count: root.fields.length,
      children: children.map(child => ({
        ...child,
        field_count: child.fields.length
      }))
    }
  })
}
