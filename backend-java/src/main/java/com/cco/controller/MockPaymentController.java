package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 支付/还款Mock控制器
 * 提供还款码、支付渠道等API的Mock数据
 */
@Slf4j
@RestController
@RequestMapping("/api/im")
public class MockPaymentController {

    /**
     * 获取还款码列表
     * GET /api/v1/im/payment-codes?case_id=91&page=1&page_size=100
     */
    @GetMapping("/payment-codes")
    public ResponseData<Map<String, Object>> getPaymentCodes(
            @RequestParam(value = "case_id", required = false) Long caseId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "page_size", required = false, defaultValue = "20") Integer pageSize
    ) {
        log.info("获取还款码列表 - caseId: {}, page: {}, pageSize: {}", caseId, page, pageSize);
        
        List<Map<String, Object>> paymentCodes = new ArrayList<>();
        
        // 为每个案件生成3-5个还款码（不同渠道）
        int count = 3 + new Random().nextInt(3);
        String[] channels = {"OXXO", "SPEI", "Bank Transfer", "7-Eleven", "PayPal"};
        String[] statuses = {"active", "active", "expired", "used"};
        
        for (int i = 0; i < count; i++) {
            Map<String, Object> code = new HashMap<>();
            code.put("id", (long) (i + 1));
            code.put("case_id", caseId);
            code.put("payment_code", "PAY" + String.format("%012d", new Random().nextInt(999999999)));
            code.put("channel", channels[i % channels.length]);
            code.put("amount", 5000 + new Random().nextInt(10000));
            code.put("currency", "MXN");
            code.put("status", statuses[i % statuses.length]);
            code.put("expires_at", LocalDateTime.now().plusDays(7).format(DateTimeFormatter.ISO_DATE_TIME));
            code.put("created_at", LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ISO_DATE_TIME));
            code.put("qr_code_url", "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + code.get("payment_code"));
            code.put("instructions", "请在便利店出示此付款码完成付款");
            
            paymentCodes.add(code);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("items", paymentCodes);
        result.put("total", (long) paymentCodes.size());
        result.put("page", page);
        result.put("page_size", pageSize);
        result.put("total_pages", 1);
        
        return ResponseData.success(result);
    }

    /**
     * 获取可用支付渠道
     * GET /api/v1/im/payment-channels?party_id=1
     */
    @GetMapping("/payment-channels")
    public ResponseData<List<Map<String, Object>>> getAvailableChannels(
            @RequestParam(value = "party_id", required = false) Long partyId
    ) {
        log.info("获取可用支付渠道 - partyId: {}", partyId);
        
        List<Map<String, Object>> channels = new ArrayList<>();
        
        // OXXO渠道
        Map<String, Object> oxxo = new HashMap<>();
        oxxo.put("id", 1L);
        oxxo.put("channel_code", "OXXO");
        oxxo.put("channel_name", "OXXO便利店");
        oxxo.put("channel_type", "cash");
        oxxo.put("min_amount", 100);
        oxxo.put("max_amount", 50000);
        oxxo.put("currency", "MXN");
        oxxo.put("is_active", true);
        oxxo.put("processing_time", "即时");
        oxxo.put("fee_rate", 0.0);
        oxxo.put("description", "在OXXO便利店使用现金支付");
        channels.add(oxxo);
        
        // SPEI转账
        Map<String, Object> spei = new HashMap<>();
        spei.put("id", 2L);
        spei.put("channel_code", "SPEI");
        spei.put("channel_name", "SPEI银行转账");
        spei.put("channel_type", "bank_transfer");
        spei.put("min_amount", 100);
        spei.put("max_amount", 1000000);
        spei.put("currency", "MXN");
        spei.put("is_active", true);
        spei.put("processing_time", "1-2小时");
        spei.put("fee_rate", 0.0);
        spei.put("description", "通过SPEI系统进行银行转账");
        channels.add(spei);
        
        // 银行转账
        Map<String, Object> bank = new HashMap<>();
        bank.put("id", 3L);
        bank.put("channel_code", "BANK_TRANSFER");
        bank.put("channel_name", "银行转账");
        bank.put("channel_type", "bank_transfer");
        bank.put("min_amount", 100);
        bank.put("max_amount", 1000000);
        bank.put("currency", "MXN");
        bank.put("is_active", true);
        bank.put("processing_time", "1-3个工作日");
        bank.put("fee_rate", 0.0);
        bank.put("description", "传统银行转账方式");
        channels.add(bank);
        
        // 7-Eleven
        Map<String, Object> sevenEleven = new HashMap<>();
        sevenEleven.put("id", 4L);
        sevenEleven.put("channel_code", "7ELEVEN");
        sevenEleven.put("channel_name", "7-Eleven便利店");
        sevenEleven.put("channel_type", "cash");
        sevenEleven.put("min_amount", 100);
        sevenEleven.put("max_amount", 30000);
        sevenEleven.put("currency", "MXN");
        sevenEleven.put("is_active", true);
        sevenEleven.put("processing_time", "即时");
        sevenEleven.put("fee_rate", 0.0);
        sevenEleven.put("description", "在7-Eleven便利店使用现金支付");
        channels.add(sevenEleven);
        
        return ResponseData.success(channels);
    }

    /**
     * 生成还款码
     * POST /api/v1/im/payment-codes
     */
    @PostMapping("/payment-codes")
    public ResponseData<Map<String, Object>> createPaymentCode(@RequestBody Map<String, Object> request) {
        log.info("生成还款码 - 请求参数: {}", request);
        
        Long caseId = request.get("case_id") != null ? ((Number) request.get("case_id")).longValue() : null;
        String channel = (String) request.get("channel");
        Double amount = request.get("amount") != null ? ((Number) request.get("amount")).doubleValue() : null;
        
        Map<String, Object> paymentCode = new HashMap<>();
        paymentCode.put("id", System.currentTimeMillis());
        paymentCode.put("case_id", caseId);
        paymentCode.put("payment_code", "PAY" + String.format("%012d", new Random().nextInt(999999999)));
        paymentCode.put("channel", channel);
        paymentCode.put("amount", amount);
        paymentCode.put("currency", "MXN");
        paymentCode.put("status", "active");
        paymentCode.put("expires_at", LocalDateTime.now().plusDays(7).format(DateTimeFormatter.ISO_DATE_TIME));
        paymentCode.put("created_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        paymentCode.put("qr_code_url", "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + paymentCode.get("payment_code"));
        paymentCode.put("instructions", "请在便利店出示此付款码完成付款");
        
        return ResponseData.success(paymentCode);
    }

    /**
     * 发送还款码（通过WhatsApp/SMS等）
     * POST /api/v1/im/payment-codes/{id}/send
     */
    @PostMapping("/payment-codes/{id}/send")
    public ResponseData<Map<String, Object>> sendPaymentCode(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        log.info("发送还款码 - id: {}, 请求参数: {}", id, request);
        
        String channel = (String) request.get("channel"); // whatsapp, sms
        String recipient = (String) request.get("recipient");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "还款码已通过" + channel + "发送到" + recipient);
        result.put("sent_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        return ResponseData.success(result);
    }

    /**
     * 获取还款码详情
     * GET /api/v1/im/payment-codes/{id}
     */
    @GetMapping("/payment-codes/{id}")
    public ResponseData<Map<String, Object>> getPaymentCodeDetail(@PathVariable Long id) {
        log.info("获取还款码详情 - id: {}", id);
        
        Map<String, Object> paymentCode = new HashMap<>();
        paymentCode.put("id", id);
        paymentCode.put("case_id", 91L);
        paymentCode.put("payment_code", "PAY" + String.format("%012d", id));
        paymentCode.put("channel", "OXXO");
        paymentCode.put("amount", 10000);
        paymentCode.put("currency", "MXN");
        paymentCode.put("status", "active");
        paymentCode.put("expires_at", LocalDateTime.now().plusDays(7).format(DateTimeFormatter.ISO_DATE_TIME));
        paymentCode.put("created_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        paymentCode.put("qr_code_url", "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + paymentCode.get("payment_code"));
        paymentCode.put("instructions", "请在便利店出示此付款码完成付款");
        
        return ResponseData.success(paymentCode);
    }

    /**
     * 使还款码失效
     * POST /api/v1/im/payment-codes/{id}/expire
     */
    @PostMapping("/payment-codes/{id}/expire")
    public ResponseData<Map<String, Object>> expirePaymentCode(@PathVariable Long id) {
        log.info("使还款码失效 - id: {}", id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "还款码已失效");
        result.put("expired_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        return ResponseData.success(result);
    }
}

