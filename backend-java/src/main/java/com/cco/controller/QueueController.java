package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.exception.BusinessException;
import com.cco.common.response.ResponseData;
import com.cco.model.entity.CaseQueue;
import com.cco.service.QueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 案件队列Controller - 数据库实现
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/queues")
public class QueueController {
    
    @Autowired
    private QueueService queueService;

    /**
     * 获取队列列表
     */
    @GetMapping
    public ResponseData<List<Map<String, Object>>> getQueues(
            @RequestParam(required = false) Long tenant_id,
            @RequestParam(required = false) Boolean is_active) {
        log.info("========== 获取队列列表，tenant_id={}, is_active={} ==========", tenant_id, is_active);
        
        List<CaseQueue> queues;
        
        if (tenant_id != null) {
            // 根据甲方ID查询
            queues = queueService.listByTenantId(tenant_id, is_active);
        } else {
            // 查询所有队列
            queues = queueService.list();
            if (is_active != null) {
                queues = queues.stream()
                        .filter(q -> is_active.equals(q.getIsActive()))
                        .collect(Collectors.toList());
            }
        }
        
        // 转换为Map格式（保持前端兼容）
        List<Map<String, Object>> result = queues.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
        
        log.info("========== 返回队列列表，数量={} ==========", result.size());
        return ResponseData.success(result);
    }

    /**
     * 获取队列详情
     */
    @GetMapping("/{id}")
    public ResponseData<Map<String, Object>> getQueue(@PathVariable Long id) {
        log.info("========== 获取队列详情，id={} ==========", id);
        
        CaseQueue queue = queueService.getById(id);
        if (queue == null) {
            throw new BusinessException("队列不存在");
        }
        
        return ResponseData.success(convertToMap(queue));
    }

    /**
     * 创建队列
     */
    @PostMapping
    public ResponseData<Map<String, Object>> createQueue(@RequestBody Map<String, Object> request) {
        log.info("========== 创建队列，request={} ==========", request);
        
        // 获取参数
        Long tenantId = getLongValue(request, "tenant_id", "tenantId");
        String queueCode = getStringValue(request, "queue_code", "queueCode");
        String queueName = getStringValue(request, "queue_name", "queueName");
        String queueNameEn = getStringValue(request, "queue_name_en", "queueNameEn");
        String queueDescription = getStringValue(request, "queue_description", "queueDescription");
        Integer overdueDaysStart = getIntegerValue(request, "overdue_days_start", "overdueDaysStart");
        Integer overdueDaysEnd = getIntegerValue(request, "overdue_days_end", "overdueDaysEnd");
        Integer sortOrder = getIntegerValue(request, "sort_order", "sortOrder", 0);
        Boolean isActive = getBooleanValue(request, "is_active", "isActive", true);
        
        // 参数验证
        if (tenantId == null) {
            throw new BusinessException("甲方ID不能为空");
        }
        if (queueCode == null || queueCode.trim().isEmpty()) {
            throw new BusinessException("队列编码不能为空");
        }
        if (queueName == null || queueName.trim().isEmpty()) {
            throw new BusinessException("队列名称不能为空");
        }
        
        // 验证队列编码唯一性
        if (queueService.existsByQueueCode(tenantId, queueCode, null)) {
            throw new BusinessException("队列编码已存在：" + queueCode);
        }
        
        // 验证逾期天数范围不重合
        if (queueService.hasOverlappingRange(tenantId, overdueDaysStart, overdueDaysEnd, null)) {
            throw new BusinessException("逾期天数范围与其他队列重合");
        }
        
        // 创建队列对象
        CaseQueue queue = new CaseQueue();
        queue.setTenantId(tenantId);
        queue.setQueueCode(queueCode);
        queue.setQueueName(queueName);
        queue.setQueueNameEn(queueNameEn);
        queue.setQueueDescription(queueDescription);
        queue.setOverdueDaysStart(overdueDaysStart);
        queue.setOverdueDaysEnd(overdueDaysEnd);
        queue.setSortOrder(sortOrder);
        queue.setIsActive(isActive);
        
        // 保存到数据库
        queueService.save(queue);
        
        log.info("========== 队列创建成功，id={} ==========", queue.getId());
        return ResponseData.success(convertToMap(queue));
    }

    /**
     * 更新队列
     */
    @PutMapping("/{id}")
    public ResponseData<Map<String, Object>> updateQueue(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("========== 更新队列，id={}, request={} ==========", id, request);
        
        // 查询现有队列
        CaseQueue queue = queueService.getById(id);
        if (queue == null) {
            throw new BusinessException("队列不存在");
        }
        
        // 获取参数（只更新提供的字段）
        String queueCode = getStringValue(request, "queue_code", "queueCode");
        String queueName = getStringValue(request, "queue_name", "queueName");
        String queueNameEn = getStringValue(request, "queue_name_en", "queueNameEn");
        String queueDescription = getStringValue(request, "queue_description", "queueDescription");
        Integer overdueDaysStart = getIntegerValue(request, "overdue_days_start", "overdueDaysStart");
        Integer overdueDaysEnd = getIntegerValue(request, "overdue_days_end", "overdueDaysEnd");
        Integer sortOrder = getIntegerValue(request, "sort_order", "sortOrder");
        Boolean isActive = getBooleanValue(request, "is_active", "isActive", null);
        
        // 更新字段
        if (queueCode != null) {
            // 验证队列编码唯一性
            if (queueService.existsByQueueCode(queue.getTenantId(), queueCode, id)) {
                throw new BusinessException("队列编码已存在：" + queueCode);
            }
            queue.setQueueCode(queueCode);
        }
        if (queueName != null) {
            queue.setQueueName(queueName);
        }
        if (queueNameEn != null) {
            queue.setQueueNameEn(queueNameEn);
        }
        if (queueDescription != null) {
            queue.setQueueDescription(queueDescription);
        }
        if (overdueDaysStart != null || overdueDaysEnd != null) {
            // 如果更新了范围，需要验证
            Integer newStart = overdueDaysStart != null ? overdueDaysStart : queue.getOverdueDaysStart();
            Integer newEnd = overdueDaysEnd != null ? overdueDaysEnd : queue.getOverdueDaysEnd();
            
            if (queueService.hasOverlappingRange(queue.getTenantId(), newStart, newEnd, id)) {
                throw new BusinessException("逾期天数范围与其他队列重合");
            }
            
            if (overdueDaysStart != null) {
                queue.setOverdueDaysStart(overdueDaysStart);
            }
            if (overdueDaysEnd != null) {
                queue.setOverdueDaysEnd(overdueDaysEnd);
            }
        }
        if (sortOrder != null) {
            queue.setSortOrder(sortOrder);
        }
        if (isActive != null) {
            queue.setIsActive(isActive);
        }
        
        // 更新到数据库
        queueService.updateById(queue);
        
        log.info("========== 队列更新成功，id={} ==========", id);
        return ResponseData.success(convertToMap(queue));
    }

    /**
     * 删除队列
     */
    @DeleteMapping("/{id}")
    public ResponseData<String> deleteQueue(@PathVariable Long id) {
        log.info("========== 删除队列，id={} ==========", id);
        
        CaseQueue queue = queueService.getById(id);
        if (queue == null) {
            throw new BusinessException("队列不存在");
        }
        
        // 从数据库删除
        queueService.removeById(id);
        
        log.info("========== 队列删除成功，id={} ==========", id);
        return ResponseData.success("删除成功");
    }
    
    /**
     * 将CaseQueue实体转换为Map（保持前端兼容）
     */
    private Map<String, Object> convertToMap(CaseQueue queue) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", queue.getId());
        map.put("tenant_id", queue.getTenantId());
        map.put("queue_code", queue.getQueueCode());
        map.put("queue_name", queue.getQueueName());
        map.put("queue_name_en", queue.getQueueNameEn());
        map.put("queue_description", queue.getQueueDescription());
        map.put("overdue_days_start", queue.getOverdueDaysStart());
        map.put("overdue_days_end", queue.getOverdueDaysEnd());
        map.put("sort_order", queue.getSortOrder());
        map.put("is_active", queue.getIsActive());
        map.put("case_count", 0);  // TODO: 统计案件数量
        if (queue.getCreatedAt() != null) {
            map.put("created_at", queue.getCreatedAt().toString());
        }
        if (queue.getUpdatedAt() != null) {
            map.put("updated_at", queue.getUpdatedAt().toString());
        }
        return map;
    }
    
    /**
     * 从Map中获取Long值（支持两种字段名格式）
     */
    private Long getLongValue(Map<String, Object> map, String key1, String key2) {
        Object value = map.get(key1);
        if (value == null) {
            value = map.get(key2);
        }
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }
    
    /**
     * 从Map中获取String值（支持两种字段名格式）
     */
    private String getStringValue(Map<String, Object> map, String key1, String key2) {
        Object value = map.get(key1);
        if (value == null) {
            value = map.get(key2);
        }
        return value != null ? value.toString() : null;
    }
    
    /**
     * 从Map中获取Integer值（支持两种字段名格式）
     */
    private Integer getIntegerValue(Map<String, Object> map, String key1, String key2) {
        return getIntegerValue(map, key1, key2, null);
    }
    
    /**
     * 从Map中获取Integer值（支持两种字段名格式，带默认值）
     */
    private Integer getIntegerValue(Map<String, Object> map, String key1, String key2, Integer defaultValue) {
        Object value = map.get(key1);
        if (value == null) {
            value = map.get(key2);
        }
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
    
    /**
     * 从Map中获取Boolean值（支持两种字段名格式，带默认值）
     */
    private Boolean getBooleanValue(Map<String, Object> map, String key1, String key2, Boolean defaultValue) {
        Object value = map.get(key1);
        if (value == null) {
            value = map.get(key2);
        }
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }
}


