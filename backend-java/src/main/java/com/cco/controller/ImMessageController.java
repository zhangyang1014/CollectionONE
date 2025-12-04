package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * IM端消息Controller - Mock实现
 * 处理IM端消息相关的接口
 * 
 * @author CCO Team
 * @since 2025-01-22
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/im/messages")
public class ImMessageController {

    /**
     * 获取新消息
     * GET /api/v1/im/messages/new?contactId=1&lastMessageId=17&limit=20
     */
    @GetMapping("/new")
    public ResponseData<Map<String, Object>> getNewMessages(
            @RequestParam(required = false) Integer contactId,
            @RequestParam(required = false) Integer lastMessageId,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        log.info("获取新消息请求，contactId={}, lastMessageId={}, limit={}", 
                contactId, lastMessageId, limit);
        
        try {
            // Mock数据：返回空消息列表
            Map<String, Object> result = new HashMap<>();
            result.put("messages", new ArrayList<>());
            result.put("unreadCount", 0);
            
            log.info("返回新消息，消息数量=0");
            return ResponseData.success(result);
        } catch (Exception e) {
            log.error("获取新消息失败", e);
            return ResponseData.error("获取新消息失败: " + e.getMessage());
        }
    }

    /**
     * 获取未读消息数
     * GET /api/v1/im/messages/unread-count?caseId=32
     */
    @GetMapping("/unread-count")
    public ResponseData<Map<String, Object>> getUnreadCount(
            @RequestParam(required = false) Integer caseId) {
        
        log.info("获取未读消息数请求，caseId={}", caseId);
        
        try {
            // Mock数据：返回空的未读消息数
            Map<String, Object> result = new HashMap<>();
            result.put("byContact", new HashMap<>());
            result.put("total", 0);
            
            log.info("返回未读消息数，总数=0");
            return ResponseData.success(result);
        } catch (Exception e) {
            log.error("获取未读消息数失败", e);
            return ResponseData.error("获取未读消息数失败: " + e.getMessage());
        }
    }

    /**
     * 标记消息已读
     * POST /api/v1/im/messages/mark-read
     */
    @PostMapping("/mark-read")
    public ResponseData<Void> markMessagesAsRead(
            @RequestParam(required = false) Integer contactId) {
        
        log.info("标记消息已读请求，contactId={}", contactId);
        
        try {
            // Mock实现：直接返回成功
            log.info("标记消息已读成功");
            return ResponseData.success();
        } catch (Exception e) {
            log.error("标记消息已读失败", e);
            return ResponseData.error("标记消息已读失败: " + e.getMessage());
        }
    }

    /**
     * 获取消息状态
     * GET /api/v1/im/messages/{messageId}/status
     */
    @GetMapping("/{messageId}/status")
    public ResponseData<Map<String, Object>> getMessageStatus(
            @PathVariable String messageId) {
        
        log.info("获取消息状态请求，messageId={}", messageId);
        
        try {
            // Mock数据：返回已读状态
            Map<String, Object> result = new HashMap<>();
            result.put("status", "read");
            result.put("messageId", messageId);
            
            log.info("返回消息状态，status=read");
            return ResponseData.success(result);
        } catch (Exception e) {
            log.error("获取消息状态失败", e);
            return ResponseData.error("获取消息状态失败: " + e.getMessage());
        }
    }

    /**
     * 发送消息
     * POST /api/v1/im/messages/send
     */
    @PostMapping("/send")
    public ResponseData<Map<String, Object>> sendMessage(
            @RequestBody Map<String, Object> request) {
        
        String senderId = (String) request.get("senderId");
        String messageType = (String) request.get("messageType");
        String content = (String) request.get("content");
        Integer contactId = (Integer) request.get("contactId");
        
        log.info("发送消息请求，senderId={}, messageType={}, contactId={}", 
                senderId, messageType, contactId);
        
        try {
            // 生成消息ID
            String messageId = "msg_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
            
            // 根据senderId查询催员名称（Mock实现）
            // 实际应该从数据库查询催员信息
            String senderName = getCollectorName(senderId);
            
            // 构建响应数据
            Map<String, Object> result = new HashMap<>();
            result.put("messageId", messageId);
            result.put("status", "sent");
            result.put("sentAt", java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z");
            result.put("senderId", senderId);
            result.put("senderName", senderName); // ✅ 返回催员名称
            
            // WhatsApp特有字段
            if (request.containsKey("waAccountType")) {
                result.put("waAccountType", request.get("waAccountType"));
                result.put("waAccountId", request.get("waAccountId"));
            }
            
            log.info("消息发送成功，messageId={}, senderName={}", messageId, senderName);
            return ResponseData.success(result);
        } catch (Exception e) {
            log.error("发送消息失败", e);
            return ResponseData.error("发送消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据催员ID获取催员名称（Mock实现）
     * 实际应该从数据库查询
     */
    private String getCollectorName(String collectorId) {
        // Mock数据：根据ID返回名称
        // TODO: 实际应该从Collector表查询
        Map<String, String> collectorNames = new HashMap<>();
        collectorNames.put("1", "催员小王");
        collectorNames.put("2", "催员小李");
        collectorNames.put("collector01", "催员01");
        collectorNames.put("collector02", "催员02");
        collectorNames.put("btq001", "催员btq001");
        collectorNames.put("btq002", "催员btq002");
        
        // 返回催员名称，如果找不到则返回默认值
        return collectorNames.getOrDefault(collectorId, "催员" + collectorId);
    }
}

