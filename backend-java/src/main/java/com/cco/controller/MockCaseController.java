package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 案件管理Mock控制器
 * 提供案件相关API的Mock数据
 */
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/cases")
public class MockCaseController {

    /**
     * 获取案件列表
     */
    @GetMapping
    public ResponseData<Map<String, Object>> getCases(
            @RequestParam(value = "tenant_id", required = false) Long tenantId,
            @RequestParam(value = "queue_id", required = false) Long queueId,
            @RequestParam(value = "collector_id", required = false) Long collectorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "page_size", required = false, defaultValue = "20") Integer pageSize
    ) {
        System.out.println("===============================================");
        System.out.println("[案件API] 接收参数:");
        System.out.println("  tenantId = " + tenantId);
        System.out.println("  collectorId = " + collectorId);
        System.out.println("  queueId = " + queueId);
        System.out.println("  status = " + status);
        System.out.println("  page = " + page);
        System.out.println("  pageSize = " + pageSize);
        System.out.println("===============================================");
        
        // 创建Mock案件数据
        List<Map<String, Object>> cases = new ArrayList<>();
        
        // 如果指定了collectorId，返回15个案件（匹配Python后端数据）；否则返回10个
        int maxCases = collectorId != null ? 15 : 10;
        
        // 生成案件时，用collectorId的后2位作为案件编号的一部分（如果有的话）
        String casePrefix = collectorId != null ? "BT" + String.format("%04d", collectorId) : "CASE";
        
        for (int i = 1; i <= maxCases; i++) {
            Map<String, Object> caseData = new HashMap<>();
            long caseId = collectorId != null ? 90 + i : i; // 如果指定了催员ID，案件ID从91开始
            
            caseData.put("id", caseId);
            caseData.put("case_code", casePrefix + "_CASE_" + String.format("%03d", i) + "_20251121");
            caseData.put("loan_id", casePrefix + "_CASE_" + String.format("%03d", i) + "_20251121");
            caseData.put("tenant_id", tenantId != null ? tenantId : 1L);
            caseData.put("queue_id", queueId != null ? queueId : 1L);
            caseData.put("queue_name", "队列" + i);
            caseData.put("user_id", "USER_BT_" + String.format("%05d", i)); // 用户ID
            caseData.put("user_name", "借款人" + String.format("%03d", i)); // 统一字段名：user_name
            caseData.put("mobile", "+52 55 " + String.format("%04d %04d", 1000 + i, 1000 + i * 10));
            caseData.put("mobile_number", "+52 55 " + String.format("%04d %04d", 1000 + i, 1000 + i * 10));
            
            // 随机金额和逾期天数
            java.util.Random random = new java.util.Random(i);
            double loanAmount = random.nextInt(40000) + 5000;
            double repaidAmount = loanAmount * (random.nextDouble() * 0.7);
            double outstandingAmount = loanAmount - repaidAmount;
            int overdueDays = random.nextInt(80) + 5;
            
            caseData.put("loan_amount", loanAmount);
            caseData.put("total_due_amount", loanAmount);
            caseData.put("repaid_amount", repaidAmount);
            caseData.put("outstanding_amount", outstandingAmount);
            caseData.put("overdue_days", overdueDays);
            
            // 案件状态
            String[] statuses = {"pending_repayment", "partial_repayment", "pending_repayment", "partial_repayment"};
            caseData.put("case_status", status != null ? status : statuses[i % 4]);
            
            // 如果指定了collectorId，所有案件都分配给该催员
            if (collectorId != null) {
                caseData.put("collector_id", collectorId);
                caseData.put("collector_name", "催员" + collectorId);
            } else {
                caseData.put("collector_id", i % 2 == 0 ? (long) (i % 5 + 1) : null);
                caseData.put("collector_name", i % 2 == 0 ? "催员" + (i % 5 + 1) : null);
            }
            
            // 时间信息
            caseData.put("assigned_at", LocalDateTime.now().minusDays(random.nextInt(15) + 5).toString());
            caseData.put("last_contact_at", null);
            caseData.put("next_follow_up_at", LocalDateTime.now().plusDays(random.nextInt(3)).toString());
            caseData.put("created_at", LocalDateTime.now().minusDays(overdueDays + 30).toString());
            caseData.put("updated_at", LocalDateTime.now().toString());
            caseData.put("due_date", LocalDateTime.now().minusDays(overdueDays).toString());
            caseData.put("settlement_date", null);
            
            // 客户基础信息
            caseData.put("customer_id_card", "320101199001" + String.format("%04d", i));
            caseData.put("customer_address", "江苏省南京市玄武区测试路" + i + "号");
            
            // 贷款详情
            caseData.put("product_name", "信用贷款" + (i % 3 + 1));
            caseData.put("loan_date", LocalDateTime.now().minusDays(overdueDays + 365).toString());
            
            // 添加空的自定义字段（兼容前端）
            caseData.put("standard_fields", new HashMap<>());
            caseData.put("custom_fields", new HashMap<>());
            
            cases.add(caseData);
        }
        
        // 构建分页响应
        Map<String, Object> result = new HashMap<>();
        result.put("items", cases);
        result.put("total", 100L);
        result.put("page", page);
        result.put("page_size", pageSize);
        result.put("total_pages", (100 + pageSize - 1) / pageSize);
        
        return ResponseData.success(result);
    }

    /**
     * 获取案件详情
     */
    @GetMapping("/{id}")
    public ResponseData<Map<String, Object>> getCaseDetail(@PathVariable Long id) {
        Map<String, Object> caseDetail = new HashMap<>();
        caseDetail.put("id", id);
        caseDetail.put("case_code", "CASE" + String.format("%06d", id));
        caseDetail.put("loan_id", "LOAN" + String.format("%06d", id));
        caseDetail.put("tenant_id", 1L);
        caseDetail.put("tenant_name", "百熵企业");
        caseDetail.put("queue_id", 1L);
        caseDetail.put("queue_name", "测试队列");
        
        // 客户基础信息
        caseDetail.put("user_name", "张三"); // 统一字段名
        caseDetail.put("mobile", "13800138001"); // 统一字段名
        caseDetail.put("customer_id_card", "320101199001010001");
        caseDetail.put("customer_address", "江苏省南京市玄武区测试路1号");
        caseDetail.put("customer_gender", "男");
        caseDetail.put("customer_age", 35);
        
        // 贷款详情
        caseDetail.put("product_name", "信用贷款1");
        caseDetail.put("loan_amount", 50000.00);
        caseDetail.put("outstanding_amount", 25000.00);
        caseDetail.put("overdue_days", 45);
        caseDetail.put("overdue_amount", 5000.00);
        caseDetail.put("penalty_amount", 500.00);
        caseDetail.put("loan_date", "2024-01-01");
        caseDetail.put("due_date", "2025-10-08");
        
        // 案件状态
        caseDetail.put("case_status", "催收中"); // 统一字段名
        caseDetail.put("collector_id", 1L);
        caseDetail.put("collector_name", "催员1");
        caseDetail.put("assigned_at", LocalDateTime.now().minusDays(10));
        caseDetail.put("created_at", LocalDateTime.now().minusDays(60));
        caseDetail.put("updated_at", LocalDateTime.now().minusDays(1));
        
        return ResponseData.success(caseDetail);
    }

    /**
     * 获取案件统计
     */
    @GetMapping("/statistics")
    public ResponseData<Map<String, Object>> getCaseStatistics(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long collectorId
    ) {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total_cases", 100);
        statistics.put("pending_cases", 20);
        statistics.put("in_collection_cases", 60);
        statistics.put("completed_cases", 20);
        statistics.put("total_outstanding", 5000000.00);
        statistics.put("total_collected", 2000000.00);
        statistics.put("collection_rate", 0.40);
        
        return ResponseData.success(statistics);
    }

    /**
     * 分配案件
     */
    @PostMapping("/{id}/assign")
    public ResponseData<Map<String, Object>> assignCase(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "案件分配成功");
        result.put("case_id", id);
        result.put("collector_id", request.get("collector_id"));
        
        return ResponseData.success(result);
    }

    /**
     * 更新案件状态
     */
    @PutMapping("/{id}/status")
    public ResponseData<Map<String, Object>> updateCaseStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "案件状态更新成功");
        result.put("case_id", id);
        result.put("new_status", request.get("status"));
        
        return ResponseData.success(result);
    }

    /**
     * 获取案件催记列表
     */
    @GetMapping("/{id}/collection-records")
    public ResponseData<List<Map<String, Object>>> getCollectionRecords(@PathVariable Long id) {
        List<Map<String, Object>> records = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", (long) i);
            record.put("case_id", id);
            record.put("collector_id", (long) (i % 3 + 1));
            record.put("collector_name", "催员" + (i % 3 + 1));
            record.put("contact_type", i % 2 == 0 ? "电话" : "短信");
            record.put("contact_result", i % 3 == 0 ? "承诺还款" : "无人接听");
            record.put("promise_amount", i % 3 == 0 ? 1000.00 : null);
            record.put("promise_date", i % 3 == 0 ? LocalDateTime.now().plusDays(3).toString() : null);
            record.put("notes", "测试催记内容" + i);
            record.put("created_at", LocalDateTime.now().minusDays(i));
            
            records.add(record);
        }
        
        return ResponseData.success(records);
    }

    /**
     * 添加催记
     */
    @PostMapping("/{id}/collection-records")
    public ResponseData<Map<String, Object>> addCollectionRecord(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "催记添加成功");
        result.put("case_id", id);
        result.put("record_id", System.currentTimeMillis());
        
        return ResponseData.success(result);
    }

    /**
     * 批量导入案件
     */
    @PostMapping("/batch-import")
    public ResponseData<Map<String, Object>> batchImportCases(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "案件批量导入成功");
        result.put("imported_count", 10);
        result.put("failed_count", 0);
        
        return ResponseData.success(result);
    }

    /**
     * 获取联系人电话状态
     * 基于催记判断电话状态：
     * - never_called: 未拨打（绿色电话）
     * - never_connected: 从未接通（空心电话）
     * - connected: 播过且接通（对号）
     * - invalid_number: 号码不存在（电话上打x）
     * 
     * @param caseId 案件ID
     * @param contactId 联系人ID（可选，如果提供则查询该联系人的状态）
     * @param phoneNumber 电话号码（可选，如果提供则查询该号码的状态）
     * @return 电话状态信息
     */
    @GetMapping("/{caseId}/contacts/phone-status")
    public ResponseData<Map<String, Object>> getContactPhoneStatus(
            @PathVariable Long caseId,
            @RequestParam(value = "contact_id", required = false) Long contactId,
            @RequestParam(value = "phone_number", required = false) String phoneNumber
    ) {
        System.out.println("===============================================");
        System.out.println("[电话状态API] 接收参数:");
        System.out.println("  caseId = " + caseId);
        System.out.println("  contactId = " + contactId);
        System.out.println("  phoneNumber = " + phoneNumber);
        System.out.println("===============================================");
        
        // 如果没有提供contactId和phoneNumber，返回错误
        boolean hasContactId = contactId != null;
        boolean hasPhoneNumber = phoneNumber != null && !phoneNumber.trim().isEmpty();
        
        if (!hasContactId && !hasPhoneNumber) {
            return ResponseData.error(400, "必须提供contactId或phoneNumber参数");
        }

        // 模拟查询催记数据
        // 在实际实现中，这里应该查询communication_records表
        // 查询条件：case_id = caseId AND contact_person_id = contactId AND channel = 'phone'
        
        // 根据contactId或phoneNumber生成不同的状态（用于测试）
        // 实际应该从数据库查询
        String status;
        String statusName;
        
        // 根据contactId的奇偶性模拟不同状态
        long idForStatus = contactId != null ? contactId : (phoneNumber != null ? phoneNumber.hashCode() : 0);
        int statusType = (int) (Math.abs(idForStatus) % 4);
        
        switch (statusType) {
            case 0:
                // 未拨打
                status = "never_called";
                statusName = "未拨打";
                break;
            case 1:
                // 从未接通
                status = "never_connected";
                statusName = "从未接通";
                break;
            case 2:
                // 播过且接通
                status = "connected";
                statusName = "已接通";
                break;
            case 3:
                // 号码不存在
                status = "invalid_number";
                statusName = "号码不存在";
                break;
            default:
                status = "never_called";
                statusName = "未拨打";
        }
        
        // 构建响应数据
        Map<String, Object> result = new HashMap<>();
        result.put("case_id", caseId);
        result.put("contact_id", contactId);
        result.put("phone_number", phoneNumber);
        result.put("status", status);
        result.put("status_name", statusName);
        
        // 如果有催记，返回最后一条催记信息
        if (!status.equals("never_called")) {
            Map<String, Object> lastRecord = new HashMap<>();
            lastRecord.put("id", Math.abs(idForStatus));
            lastRecord.put("contacted_at", LocalDateTime.now().minusDays(statusType).toString());
            lastRecord.put("is_connected", status.equals("connected"));
            lastRecord.put("contact_result", status.equals("invalid_number") ? "invalid_number" : 
                         (status.equals("connected") ? "connected" : "not_connected"));
            result.put("last_call_record", lastRecord);
        }
        
        return ResponseData.success(result);
    }
}


