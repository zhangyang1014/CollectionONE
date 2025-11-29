package com.cco.controller;

import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.Random;

/**
 * 还款渠道Controller - IM端专用
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping("/api/im")
public class PaymentChannelController {

    /**
     * 获取可用还款渠道（IM端）
     */
    @GetMapping("/payment-channels")
    public ResponseData<List<Map<String, Object>>> getAvailableChannels(
            @RequestParam Long party_id) {
        log.info("========== 获取可用还款渠道，party_id={} ==========", party_id);
        
        List<Map<String, Object>> channels = new ArrayList<>();
        
        // Mock数据 - 常见的还款渠道
        String[] channelNames = {"GCash", "Maya", "Bank Transfer", "7-Eleven", "Palawan Express"};
        String[] channelCodes = {"gcash", "maya", "bank_transfer", "7eleven", "palawan"};
        
        for (int i = 0; i < channelNames.length; i++) {
            Map<String, Object> channel = new HashMap<>();
            channel.put("id", (long) (i + 1));
            channel.put("party_id", party_id);
            channel.put("channel_name", channelNames[i]);
            channel.put("channel_code", channelCodes[i]);
            channel.put("channel_type", i < 2 ? "digital_wallet" : i < 3 ? "bank" : "cash");
            channel.put("is_enabled", true);
            channel.put("sort_order", i + 1);
            channel.put("icon_url", "https://example.com/icons/" + channelCodes[i] + ".png");
            channel.put("description", channelNames[i] + "还款渠道");
            channel.put("created_at", "2025-01-01T00:00:00");
            channel.put("updated_at", "2025-11-25T00:00:00");
            
            channels.add(channel);
        }
        
        log.info("========== 返回可用还款渠道，数量={} ==========", channels.size());
        return ResponseData.success(channels);
    }

    /**
     * 获取还款码列表（IM端）
     */
    @GetMapping("/payment-codes")
    public ResponseData<List<Map<String, Object>>> getPaymentCodes(
            @RequestParam(required = false) Long case_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "100") Integer page_size) {
        log.info("========== 获取还款码列表，case_id={}, status={}, page={}, page_size={} ==========", 
                case_id, status, page, page_size);
        
        List<Map<String, Object>> paymentCodes = new ArrayList<>();
        
        // Mock数据 - 还款码列表
        String[] statuses = {"PENDING", "PAID", "EXPIRED"};
        String[] channelNames = {"GCash", "Maya", "Bank Transfer"};
        String[] channelCodes = {"gcash", "maya", "bank_transfer"};
        String[] paymentTypes = {"full", "partial", "installment"};
        
        Random random = new Random();
        int count = case_id != null ? 3 + random.nextInt(5) : 0; // 如果有case_id，生成3-7条
        
        for (int i = 0; i < count; i++) {
            Map<String, Object> code = new HashMap<>();
            String codeStatus = status != null && !status.isEmpty() ? status : statuses[random.nextInt(statuses.length)];
            int channelIndex = random.nextInt(channelNames.length);
            
            code.put("id", (long) (i + 1));
            code.put("code_no", "PAY" + String.format("%010d", case_id != null ? case_id * 1000 + i : i + 1));
            code.put("case_id", case_id != null ? case_id : 1L);
            code.put("loan_id", case_id != null ? case_id : 1L);
            code.put("channel_id", (long) (channelIndex + 1));
            code.put("channel_name", channelNames[channelIndex]);
            code.put("channel_code", channelCodes[channelIndex]);
            code.put("channel_icon", "https://example.com/icons/" + channelCodes[channelIndex] + ".png");
            code.put("payment_type", paymentTypes[random.nextInt(paymentTypes.length)]);
            code.put("installment_number", random.nextBoolean() ? random.nextInt(12) + 1 : null);
            code.put("amount", 1000.0 + random.nextDouble() * 50000.0);
            code.put("currency", "PHP");
            code.put("status", codeStatus);
            code.put("expires_at", java.time.LocalDateTime.now().plusHours(24 - i).format(
                    java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            code.put("paid_at", "PENDING".equals(codeStatus) ? null : 
                    java.time.LocalDateTime.now().minusHours(i).format(
                            java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            code.put("created_at", java.time.LocalDateTime.now().minusHours(i + 1).format(
                    java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            code.put("updated_at", java.time.LocalDateTime.now().minusHours(i).format(
                    java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            // 如果是PENDING状态，添加剩余秒数
            if ("PENDING".equals(codeStatus)) {
                code.put("remaining_seconds", 86400 - i * 3600); // 24小时倒计时
            }
            
            paymentCodes.add(code);
        }
        
        log.info("========== 返回还款码列表，数量={} ==========", paymentCodes.size());
        return ResponseData.success(paymentCodes);
    }

    /**
     * 获取还款码详情（IM端）
     */
    @GetMapping("/payment-codes/{codeNo}")
    public ResponseData<Map<String, Object>> getPaymentCodeDetail(@PathVariable String codeNo) {
        log.info("========== 获取还款码详情，codeNo={} ==========", codeNo);
        
        // Mock数据 - 还款码详情
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", 1L);
        detail.put("code_no", codeNo);
        detail.put("case_id", 1L);
        detail.put("loan_id", 1L);
        detail.put("channel_id", 1L);
        detail.put("channel_name", "GCash");
        detail.put("channel_code", "gcash");
        detail.put("channel_icon", "https://example.com/icons/gcash.png");
        detail.put("payment_type", "full");
        detail.put("installment_number", null);
        detail.put("amount", 50000.0);
        detail.put("currency", "PHP");
        detail.put("status", "PENDING");
        detail.put("expires_at", java.time.LocalDateTime.now().plusHours(24).format(
                java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        detail.put("paid_at", null);
        detail.put("payment_url", "https://payment.example.com/pay/" + codeNo);
        detail.put("qr_code_url", "https://payment.example.com/qr/" + codeNo);
        detail.put("created_at", java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        detail.put("updated_at", java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        detail.put("remaining_seconds", 86400);
        
        return ResponseData.success(detail);
    }

    /**
     * 获取案件期数信息（IM端）
     */
    @GetMapping("/cases/{caseId}/installments")
    public ResponseData<Map<String, Object>> getCaseInstallments(@PathVariable Long caseId) {
        log.info("========== 获取案件期数信息，caseId={} ==========", caseId);
        
        // Mock数据 - 期数信息
        Map<String, Object> result = new HashMap<>();
        result.put("total_installments", 12);
        result.put("current_overdue", 3);
        
        List<Map<String, Object>> installments = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 1; i <= 12; i++) {
            Map<String, Object> installment = new HashMap<>();
            installment.put("number", i);
            String status = i <= 3 ? "OVERDUE" : (i <= 6 ? "PENDING" : "PAID");
            installment.put("status", status);
            installment.put("due_date", java.time.LocalDateTime.now().minusMonths(12 - i).format(
                    java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            if ("OVERDUE".equals(status)) {
                installment.put("overdue_days", (12 - i) * 30 + random.nextInt(30));
            }
            installment.put("principal", 10000.0 + random.nextDouble() * 5000.0);
            installment.put("interest", 500.0 + random.nextDouble() * 200.0);
            installment.put("penalty", "OVERDUE".equals(status) ? 100.0 + random.nextDouble() * 50.0 : 0.0);
            installment.put("fee", 50.0);
            double total = ((Number) installment.get("principal")).doubleValue() +
                          ((Number) installment.get("interest")).doubleValue() +
                          ((Number) installment.get("penalty")).doubleValue() +
                          ((Number) installment.get("fee")).doubleValue();
            installment.put("total", total);
            
            installments.add(installment);
        }
        
        result.put("installments", installments);
        
        log.info("========== 返回期数信息，总期数={} ==========", installments.size());
        return ResponseData.success(result);
    }

    /**
     * 请求还款码（IM端）
     */
    @PostMapping("/payment-codes/request")
    public ResponseData<Map<String, Object>> requestPaymentCode(@RequestBody Map<String, Object> request) {
        log.info("========== 请求还款码，request={} ==========", request);
        
        // Mock数据 - 生成还款码
        Map<String, Object> result = new HashMap<>();
        Long caseId = request.get("case_id") != null ? 
                Long.valueOf(request.get("case_id").toString()) : 1L;
        Long loanId = request.get("loan_id") != null ? 
                Long.valueOf(request.get("loan_id").toString()) : 1L;
        Long channelId = request.get("channel_id") != null ? 
                Long.valueOf(request.get("channel_id").toString()) : 1L;
        Double amount = request.get("amount") != null ? 
                Double.valueOf(request.get("amount").toString()) : 1000.0;
        
        result.put("id", System.currentTimeMillis());
        result.put("code_no", "PAY" + String.format("%010d", caseId * 1000 + System.currentTimeMillis() % 1000));
        result.put("case_id", caseId);
        result.put("loan_id", loanId);
        result.put("channel_id", channelId);
        result.put("channel_name", "GCash");
        result.put("channel_code", "gcash");
        result.put("payment_type", "VA");
        result.put("installment_number", request.get("installment_number"));
        result.put("amount", amount);
        result.put("currency", "PHP");
        result.put("status", "PENDING");
        result.put("payment_code", "1234567890");
        result.put("payment_url", "https://payment.example.com/pay/" + result.get("code_no"));
        result.put("qr_code_url", "https://payment.example.com/qr/" + result.get("code_no"));
        result.put("expires_at", java.time.LocalDateTime.now().plusHours(24).format(
                java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        result.put("created_at", java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        result.put("updated_at", java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        result.put("remaining_seconds", 86400);
        
        log.info("========== 返回还款码，code_no={} ==========", result.get("code_no"));
        return ResponseData.success(result);
    }
}

