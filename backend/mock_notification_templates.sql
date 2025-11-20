-- Mock 10条通知模板数据
-- 用于测试和演示通知模板功能

USE cco_system;

-- 清空现有数据（可选，如果需要重新开始）
-- TRUNCATE TABLE notification_templates;

-- 插入10条Mock数据
INSERT INTO notification_templates (
    template_id, 
    template_name, 
    template_type, 
    description,
    content_template, 
    jump_url_template,
    target_type, 
    target_agencies,
    target_teams,
    target_collectors,
    is_forced_read,
    repeat_interval_minutes,
    max_remind_count,
    notify_time_start,
    notify_time_end,
    priority, 
    display_duration_seconds,
    is_enabled,
    available_variables,
    total_sent,
    total_read
) VALUES 
-- 1. 案件标签变化模板
(
    'case_tag_change',
    '案件标签变化通知',
    'case_tag_change',
    '当案件标签发生变化时，实时通知相关催员',
    '📌 案件 {case_number} 的标签已从「{old_tag}」更改为「{new_tag}」，操作人：{operator}',
    '/cases/{case_id}',
    'agency',
    NULL,
    NULL,
    NULL,
    FALSE,
    30,
    3,
    '09:00',
    '21:00',
    'medium',
    5,
    TRUE,
    '{"case_id":"案件ID","case_number":"案件编号","tag_name":"标签名称","old_tag":"旧标签","new_tag":"新标签","operator":"操作人"}',
    1250,
    980
),

-- 2. 案件还款模板
(
    'case_payment',
    '案件还款到账通知',
    'case_payment',
    '当案件收到还款时，立即通知负责催员',
    '💰 好消息！案件 {case_number} 收到还款 ￥{amount}，还款时间：{payment_time}，渠道：{payment_channel}',
    '/cases/{case_id}',
    'collector',
    NULL,
    NULL,
    NULL,
    TRUE,
    NULL,
    NULL,
    NULL,
    NULL,
    'high',
    8,
    TRUE,
    '{"case_id":"案件ID","case_number":"案件编号","amount":"还款金额","payment_time":"还款时间","payment_channel":"还款渠道","debtor_name":"债务人姓名"}',
    3420,
    3380
),

-- 3. 用户访问APP模板
(
    'user_app_visit',
    '用户APP访问提醒',
    'user_app_visit',
    '当债务人访问APP时，提醒催员及时跟进',
    '👤 用户 {user_name} ({user_phone}) 刚刚访问了APP，案件：{case_number}，设备：{device_type}',
    '/cases/{case_id}',
    'collector',
    NULL,
    NULL,
    NULL,
    FALSE,
    60,
    5,
    '08:00',
    '22:00',
    'medium',
    6,
    TRUE,
    '{"case_id":"案件ID","case_number":"案件编号","user_name":"用户姓名","user_phone":"用户手机号","visit_time":"访问时间","device_type":"设备类型"}',
    2180,
    1950
),

-- 4. 用户访问还款页模板
(
    'user_payment_page_visit',
    '用户还款页访问通知',
    'user_payment_page_visit',
    '当用户访问还款页面时，提示催员用户有还款意向',
    '💳 重要！用户 {user_name} 正在查看还款页面，待还金额：￥{outstanding_amount}，请及时联系！',
    '/cases/{case_id}',
    'collector',
    NULL,
    NULL,
    NULL,
    TRUE,
    NULL,
    NULL,
    NULL,
    NULL,
    'high',
    10,
    TRUE,
    '{"case_id":"案件ID","case_number":"案件编号","user_name":"用户姓名","user_phone":"用户手机号","visit_time":"访问时间","outstanding_amount":"待还金额"}',
    1560,
    1520
),

-- 5. 案件分配模板
(
    'case_assigned',
    '新案件分配通知',
    'case_assigned',
    '当新案件分配给催员时发送通知',
    '📋 您有新的案件分配！案件编号：{case_number}，金额：￥{case_amount}，分配时间：{assign_time}',
    '/cases/{case_id}',
    'collector',
    NULL,
    NULL,
    NULL,
    TRUE,
    NULL,
    NULL,
    NULL,
    NULL,
    'high',
    7,
    TRUE,
    '{"case_id":"案件ID","case_number":"案件编号","collector_name":"催员姓名","assign_time":"分配时间","case_amount":"案件金额"}',
    5680,
    5450
),

-- 6. PTP提醒模板
(
    'ptp_reminder',
    'PTP承诺到期提醒',
    'ptp_reminder',
    'PTP到期前提醒催员跟进',
    '⏰ 提醒：案件 {case_number} 的PTP将于 {ptp_date} 到期，承诺金额：￥{ptp_amount}，债务人：{debtor_name}',
    '/cases/{case_id}',
    'collector',
    NULL,
    NULL,
    NULL,
    FALSE,
    120,
    3,
    '09:00',
    '18:00',
    'high',
    6,
    TRUE,
    '{"case_id":"案件ID","case_number":"案件编号","ptp_date":"PTP日期","ptp_amount":"承诺金额","debtor_name":"债务人姓名"}',
    2340,
    2100
),

-- 7. 逾期升级通知
(
    'overdue_escalation',
    '案件逾期升级警告',
    'case_update',
    '当案件逾期天数达到阈值时发送警告',
    '⚠️ 警告！案件 {case_number} 已逾期 {overdue_days} 天，当前状态：{case_status}，请尽快处理！',
    '/cases/{case_id}',
    'team',
    NULL,
    NULL,
    NULL,
    TRUE,
    NULL,
    NULL,
    NULL,
    NULL,
    'high',
    8,
    TRUE,
    '{"case_id":"案件ID","case_number":"案件编号","overdue_days":"逾期天数","case_status":"案件状态","debtor_name":"债务人姓名"}',
    890,
    850
),

-- 8. 小组业绩达成通知
(
    'team_performance',
    '小组业绩达成通知',
    'performance',
    '当小组业绩达到目标时发送祝贺通知',
    '🎉 恭喜！{team_name} 本月回款已达 ￥{amount}，完成率 {completion_rate}%，继续加油！',
    '/performance/team/{team_id}',
    'team',
    NULL,
    NULL,
    NULL,
    FALSE,
    NULL,
    1,
    NULL,
    NULL,
    'medium',
    10,
    TRUE,
    '{"team_id":"小组ID","team_name":"小组名称","amount":"回款金额","completion_rate":"完成率","target_amount":"目标金额"}',
    156,
    145
),

-- 9. 催员日报提醒
(
    'daily_report_reminder',
    '催员日报提交提醒',
    'timeout',
    '每日下班前提醒催员提交工作日报',
    '📝 请记得提交今日工作日报！今日联系案件数：{contact_count}，承诺还款：￥{promised_amount}',
    '/reports/daily',
    'collector',
    NULL,
    NULL,
    NULL,
    FALSE,
    30,
    2,
    '17:00',
    '19:00',
    'low',
    5,
    TRUE,
    '{"contact_count":"联系案件数","promised_amount":"承诺金额","follow_up_count":"跟进次数"}',
    4520,
    3980
),

-- 10. 系统维护通知
(
    'system_maintenance',
    '系统维护公告',
    'case_update',
    '系统维护或升级时的通知',
    '🔧 系统维护通知：系统将于 {maintenance_time} 进行维护，预计持续 {duration} 分钟，请提前保存工作内容。',
    '/system/notice',
    'agency',
    NULL,
    NULL,
    NULL,
    TRUE,
    NULL,
    NULL,
    NULL,
    NULL,
    'high',
    15,
    FALSE,
    '{"maintenance_time":"维护时间","duration":"持续时长","affected_modules":"影响模块"}',
    45,
    43
);

-- 查看插入结果
SELECT 
    id,
    template_id,
    template_name,
    template_type,
    priority,
    is_enabled,
    total_sent,
    total_read
FROM notification_templates
ORDER BY id;

-- 统计信息
SELECT 
    COUNT(*) as total_templates,
    SUM(CASE WHEN is_enabled = TRUE THEN 1 ELSE 0 END) as enabled_count,
    SUM(total_sent) as total_sent_all,
    SUM(total_read) as total_read_all,
    ROUND(SUM(total_read) / SUM(total_sent) * 100, 2) as read_rate
FROM notification_templates;

