package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 队列Mock控制器
 * 提供队列相关API的Mock数据
 */
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/queues")
public class MockQueueController {

    /**
     * 获取队列列表
     */
    @GetMapping
    public ResponseData<Map<String, Object>> getQueues(
            @RequestParam(value = "tenant_id", required = false) Long tenantId,
            @RequestParam(value = "is_active", required = false) Boolean isActive,
            @RequestParam(required = false, defaultValue = "0") Integer skip,
            @RequestParam(required = false, defaultValue = "100") Integer limit
    ) {
        System.out.println("===============================================");
        System.out.println("[队列API] 接收参数:");
        System.out.println("  tenantId = " + tenantId);
        System.out.println("  isActive = " + isActive);
        System.out.println("  skip = " + skip);
        System.out.println("  limit = " + limit);
        System.out.println("===============================================");
        
        List<Map<String, Object>> queues = generateMockQueues(tenantId);
        
        // 根据is_active过滤
        if (isActive != null) {
            queues = queues.stream()
                    .filter(q -> isActive.equals(q.get("is_active")))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // 应用分页
        int start = Math.min(skip, queues.size());
        int end = Math.min(skip + limit, queues.size());
        List<Map<String, Object>> pagedQueues = queues.subList(start, end);
        
        Map<String, Object> result = new HashMap<>();
        result.put("items", pagedQueues);
        result.put("total", queues.size());
        
        return ResponseData.success(result);
    }
    
    /**
     * 生成Mock队列数据
     */
    private List<Map<String, Object>> generateMockQueues(Long tenantId) {
        List<Map<String, Object>> queues = new ArrayList<>();
        Long defaultTenantId = tenantId != null ? tenantId : 1L;
        
        queues.add(createQueue(1L, defaultTenantId, "默认队列", "default", 1, true));
        queues.add(createQueue(2L, defaultTenantId, "优先队列", "priority", 2, true));
        queues.add(createQueue(3L, defaultTenantId, "普通队列", "normal", 3, true));
        
        return queues;
    }
    
    /**
     * 创建队列对象
     */
    private Map<String, Object> createQueue(
            Long id,
            Long tenantId,
            String queueName,
            String queueKey,
            Integer sortOrder,
            Boolean isActive
    ) {
        Map<String, Object> queue = new HashMap<>();
        queue.put("id", id);
        queue.put("tenant_id", tenantId);
        queue.put("queue_name", queueName);
        queue.put("queue_key", queueKey);
        queue.put("sort_order", sortOrder);
        queue.put("is_active", isActive);
        queue.put("description", queueName + "的描述");
        queue.put("created_at", LocalDateTime.now().minusDays(30).toString());
        queue.put("updated_at", LocalDateTime.now().toString());
        return queue;
    }
    
    /**
     * 获取单个队列
     */
    @GetMapping("/{id}")
    public ResponseData<Map<String, Object>> getQueue(@PathVariable Long id) {
        List<Map<String, Object>> allQueues = generateMockQueues(null);
        Map<String, Object> queue = allQueues.stream()
                .filter(q -> id.equals(q.get("id")))
                .findFirst()
                .orElse(null);
        
        if (queue == null) {
            return ResponseData.error(404, "队列不存在");
        }
        
        return ResponseData.success(queue);
    }
    
    /**
     * 创建队列
     */
    @PostMapping
    public ResponseData<Map<String, Object>> createQueue(@RequestBody Map<String, Object> request) {
        Map<String, Object> queue = new HashMap<>();
        queue.put("id", System.currentTimeMillis());
        queue.put("tenant_id", request.get("tenant_id"));
        queue.put("queue_name", request.get("queue_name"));
        queue.put("queue_key", request.get("queue_key"));
        queue.put("sort_order", request.getOrDefault("sort_order", 0));
        queue.put("is_active", request.getOrDefault("is_active", true));
        queue.put("description", request.get("description"));
        queue.put("created_at", LocalDateTime.now().toString());
        queue.put("updated_at", LocalDateTime.now().toString());
        
        return ResponseData.success(queue);
    }
    
    /**
     * 更新队列
     */
    @PutMapping("/{id}")
    public ResponseData<Map<String, Object>> updateQueue(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        List<Map<String, Object>> allQueues = generateMockQueues(null);
        Map<String, Object> queue = allQueues.stream()
                .filter(q -> id.equals(q.get("id")))
                .findFirst()
                .orElse(new HashMap<>());
        
        if (request.containsKey("queue_name")) {
            queue.put("queue_name", request.get("queue_name"));
        }
        if (request.containsKey("queue_key")) {
            queue.put("queue_key", request.get("queue_key"));
        }
        if (request.containsKey("sort_order")) {
            queue.put("sort_order", request.get("sort_order"));
        }
        if (request.containsKey("is_active")) {
            queue.put("is_active", request.get("is_active"));
        }
        if (request.containsKey("description")) {
            queue.put("description", request.get("description"));
        }
        
        queue.put("updated_at", LocalDateTime.now().toString());
        
        return ResponseData.success(queue);
    }
    
    /**
     * 删除队列
     */
    @DeleteMapping("/{id}")
    public ResponseData<Map<String, Object>> deleteQueue(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "队列删除成功");
        result.put("id", id);
        
        return ResponseData.success(result);
    }
    
    /**
     * 获取队列字段配置
     */
    @GetMapping("/{queueId}/field-configs")
    public ResponseData<Map<String, Object>> getQueueFieldConfigs(@PathVariable Long queueId) {
        Map<String, Object> result = new HashMap<>();
        result.put("queue_id", queueId);
        result.put("queue_name", "队列" + queueId);
        result.put("fields", new ArrayList<>());
        
        return ResponseData.success(result);
    }
    
    /**
     * 批量更新队列字段配置
     */
    @PutMapping("/{queueId}/field-configs")
    public ResponseData<Map<String, Object>> updateQueueFieldConfigs(
            @PathVariable Long queueId,
            @RequestBody Map<String, Object> request
    ) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "队列字段配置更新成功");
        result.put("queue_id", queueId);
        
        return ResponseData.success(result);
    }
    
    /**
     * 从其他队列复制字段配置
     */
    @PostMapping("/{targetQueueId}/copy-field-configs")
    public ResponseData<Map<String, Object>> copyQueueFieldConfigs(
            @PathVariable Long targetQueueId,
            @RequestBody Map<String, Object> request
    ) {
        Map<String, Object> result = new HashMap<>();
        result.put("copied_count", 10);
        result.put("skipped_count", 0);
        
        return ResponseData.success(result);
    }
}




