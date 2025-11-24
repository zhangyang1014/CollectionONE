package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 催员Mock控制器
 * 提供催员相关API的Mock数据
 */
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/collectors")
public class MockCollectorController {

    /**
     * 获取催员列表
     */
    @GetMapping
    public ResponseData<List<Map<String, Object>>> getCollectors(
            @RequestParam(value = "team_id", required = false) Long teamId,
            @RequestParam(value = "agency_id", required = false) Long agencyId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "is_active", required = false) Boolean isActive,
            @RequestParam(required = false, defaultValue = "0") Integer skip,
            @RequestParam(required = false, defaultValue = "100") Integer limit
    ) {
        System.out.println("===============================================");
        System.out.println("[催员API] 接收参数:");
        System.out.println("  teamId = " + teamId);
        System.out.println("  agencyId = " + agencyId);
        System.out.println("  status = " + status);
        System.out.println("  isActive = " + isActive);
        System.out.println("===============================================");
        
        List<Map<String, Object>> collectors = generateMockCollectors(teamId, agencyId);
        
        // 根据status过滤
        if (status != null) {
            collectors = collectors.stream()
                    .filter(c -> status.equals(c.get("status")))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // 根据is_active过滤
        if (isActive != null) {
            collectors = collectors.stream()
                    .filter(c -> isActive.equals(c.get("is_active")))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // 应用分页
        int start = Math.min(skip, collectors.size());
        int end = Math.min(skip + limit, collectors.size());
        List<Map<String, Object>> pagedCollectors = collectors.subList(start, end);
        
        return ResponseData.success(pagedCollectors);
    }
    
    /**
     * 生成Mock催员数据
     */
    private List<Map<String, Object>> generateMockCollectors(Long teamId, Long agencyId) {
        List<Map<String, Object>> collectors = new ArrayList<>();
        
        // 如果指定了teamId，返回该小组的催员
        if (teamId != null) {
            for (int i = 1; i <= 5; i++) {
                collectors.add(createCollector((long) i, teamId, agencyId != null ? agencyId : 1L, "催员" + i, "collector_" + i, "active", true));
            }
        } else {
            // 返回所有催员
            for (int i = 1; i <= 10; i++) {
                collectors.add(createCollector((long) i, (long) ((i % 2) + 1), 1L, "催员" + i, "collector_" + i, "active", true));
            }
        }
        
        return collectors;
    }
    
    /**
     * 创建催员对象
     */
    private Map<String, Object> createCollector(
            Long id,
            Long teamId,
            Long agencyId,
            String collectorName,
            String collectorCode,
            String status,
            Boolean isActive
    ) {
        Map<String, Object> collector = new HashMap<>();
        collector.put("id", id);
        collector.put("team_id", teamId);
        collector.put("agency_id", agencyId);
        collector.put("collector_name", collectorName);
        collector.put("collector_code", collectorCode);
        collector.put("status", status);
        collector.put("is_active", isActive);
        collector.put("mobile", "1380013800" + (id % 10));
        collector.put("email", "collector" + id + "@example.com");
        collector.put("created_at", LocalDateTime.now().minusDays(30).toString());
        collector.put("updated_at", LocalDateTime.now().toString());
        return collector;
    }
    
    /**
     * 获取单个催员
     */
    @GetMapping("/{id}")
    public ResponseData<Map<String, Object>> getCollector(@PathVariable Long id) {
        List<Map<String, Object>> allCollectors = generateMockCollectors(null, null);
        Map<String, Object> collector = allCollectors.stream()
                .filter(c -> id.equals(c.get("id")))
                .findFirst()
                .orElse(null);
        
        if (collector == null) {
            return ResponseData.error(404, "催员不存在");
        }
        
        return ResponseData.success(collector);
    }
    
    /**
     * 获取催员详情（包含统计）
     */
    @GetMapping("/{id}/detail")
    public ResponseData<Map<String, Object>> getCollectorDetail(@PathVariable Long id) {
        Map<String, Object> collector = createCollector(id, 1L, 1L, "催员" + id, "collector_" + id, "active", true);
        
        // 添加统计信息
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_cases", 50);
        stats.put("active_cases", 30);
        stats.put("completed_cases", 20);
        stats.put("total_collected", 500000.00);
        stats.put("collection_rate", 0.75);
        
        collector.put("statistics", stats);
        
        return ResponseData.success(collector);
    }
    
    /**
     * 创建催员
     */
    @PostMapping
    public ResponseData<Map<String, Object>> createCollector(@RequestBody Map<String, Object> request) {
        Map<String, Object> collector = new HashMap<>();
        collector.put("id", System.currentTimeMillis());
        collector.put("team_id", request.get("team_id"));
        collector.put("agency_id", request.get("agency_id"));
        collector.put("collector_name", request.get("collector_name"));
        collector.put("collector_code", request.get("collector_code"));
        collector.put("status", request.getOrDefault("status", "active"));
        collector.put("is_active", request.getOrDefault("is_active", true));
        collector.put("mobile", request.get("mobile"));
        collector.put("email", request.get("email"));
        collector.put("created_at", LocalDateTime.now().toString());
        collector.put("updated_at", LocalDateTime.now().toString());
        
        return ResponseData.success(collector);
    }
    
    /**
     * 更新催员
     */
    @PutMapping("/{id}")
    public ResponseData<Map<String, Object>> updateCollector(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        List<Map<String, Object>> allCollectors = generateMockCollectors(null, null);
        Map<String, Object> collector = allCollectors.stream()
                .filter(c -> id.equals(c.get("id")))
                .findFirst()
                .orElse(new HashMap<>());
        
        if (request.containsKey("collector_name")) {
            collector.put("collector_name", request.get("collector_name"));
        }
        if (request.containsKey("team_id")) {
            collector.put("team_id", request.get("team_id"));
        }
        if (request.containsKey("status")) {
            collector.put("status", request.get("status"));
        }
        if (request.containsKey("is_active")) {
            collector.put("is_active", request.get("is_active"));
        }
        if (request.containsKey("mobile")) {
            collector.put("mobile", request.get("mobile"));
        }
        if (request.containsKey("email")) {
            collector.put("email", request.get("email"));
        }
        
        collector.put("updated_at", LocalDateTime.now().toString());
        
        return ResponseData.success(collector);
    }
    
    /**
     * 删除催员
     */
    @DeleteMapping("/{id}")
    public ResponseData<Map<String, Object>> deleteCollector(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "催员删除成功");
        result.put("id", id);
        
        return ResponseData.success(result);
    }
    
    /**
     * 获取催员登录人脸记录
     */
    @GetMapping("/{collectorId}/login-face-records")
    public ResponseData<List<Map<String, Object>>> getCollectorLoginFaceRecords(@PathVariable Long collectorId) {
        List<Map<String, Object>> records = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", (long) i);
            record.put("collector_id", collectorId);
            record.put("login_time", LocalDateTime.now().minusDays(i).toString());
            record.put("face_image", "data:image/jpeg;base64,/9j/4AAQSkZJRg...");
            record.put("face_id", "face_" + collectorId + "_" + i);
            record.put("created_at", LocalDateTime.now().minusDays(i).toString());
            records.add(record);
        }
        
        return ResponseData.success(records);
    }
}



