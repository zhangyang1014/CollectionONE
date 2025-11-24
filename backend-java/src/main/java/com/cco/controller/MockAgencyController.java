package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Mock机构管理Controller（临时，用于前端开发）
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/agencies")
public class MockAgencyController {

    /**
     * 获取机构列表
     * GET /api/v1/agencies
     */
    @GetMapping("")
    public ResponseData<List<Map<String, Object>>> getAgencies(
            @RequestParam(required = false) Long tenant_id,
            @RequestParam(required = false) Boolean is_active,
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit
    ) {
        log.info("获取机构列表（Mock），tenant_id={}, is_active={}, skip={}, limit={}", 
                tenant_id, is_active, skip, limit);
        
        List<Map<String, Object>> agencies = new ArrayList<>();
        agencies.add(createAgency(1L, 1L, "机构A", "agency_a", 1, true));
        agencies.add(createAgency(2L, 1L, "机构B", "agency_b", 2, true));
        agencies.add(createAgency(3L, 2L, "机构C", "agency_c", 1, true));
        
        return ResponseData.success(agencies);
    }

    /**
     * 获取机构详情
     * GET /api/v1/agencies/{id}
     */
    @GetMapping("/{id}")
    public ResponseData<Map<String, Object>> getAgency(@PathVariable Long id) {
        log.info("获取机构详情（Mock），id={}", id);
        
        Map<String, Object> agency = createAgency(id, 1L, "机构A", "agency_a", 1, true);
        
        return ResponseData.success(agency);
    }

    /**
     * 创建机构
     * POST /api/v1/agencies
     */
    @PostMapping("")
    public ResponseData<Map<String, Object>> createAgency(@RequestBody Map<String, Object> data) {
        log.info("创建机构（Mock），data={}", data);
        
        Long id = System.currentTimeMillis();
        Map<String, Object> agency = createAgency(
            id,
            Long.valueOf(data.getOrDefault("tenant_id", 1L).toString()),
            data.getOrDefault("agency_name", "新机构").toString(),
            data.getOrDefault("agency_code", "new_agency").toString(),
            Integer.valueOf(data.getOrDefault("sort_order", 0).toString()),
            Boolean.valueOf(data.getOrDefault("is_active", true).toString())
        );
        
        return ResponseData.success(agency);
    }

    /**
     * 更新机构
     * PUT /api/v1/agencies/{id}
     */
    @PutMapping("/{id}")
    public ResponseData<Map<String, Object>> updateAgency(
            @PathVariable Long id,
            @RequestBody Map<String, Object> data
    ) {
        log.info("更新机构（Mock），id={}, data={}", id, data);
        
        Map<String, Object> agency = createAgency(id, 1L, "机构A", "agency_a", 1, true);
        
        // 更新字段
        if (data.containsKey("agency_name")) {
            agency.put("agency_name", data.get("agency_name"));
        }
        if (data.containsKey("is_active")) {
            agency.put("is_active", data.get("is_active"));
        }
        
        return ResponseData.success(agency);
    }

    /**
     * 删除机构
     * DELETE /api/v1/agencies/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseData<Void> deleteAgency(@PathVariable Long id) {
        log.info("删除机构（Mock），id={}", id);
        
        return ResponseData.success();
    }

    /**
     * 获取机构作息时间
     * GET /api/v1/agencies/{id}/working-hours
     */
    @GetMapping("/{id}/working-hours")
    public ResponseData<List<Map<String, Object>>> getAgencyWorkingHours(@PathVariable Long id) {
        log.info("获取机构作息时间（Mock），agencyId={}", id);
        
        List<Map<String, Object>> workingHours = new ArrayList<>();
        
        // 生成7天的作息时间数据（周一到周日，day_of_week: 0-6）
        for (int day = 0; day < 7; day++) {
            Map<String, Object> wh = new HashMap<>();
            wh.put("id", day + 1);
            wh.put("agency_id", id);
            wh.put("day_of_week", day);
            
            // 默认工作时间：周一到周五 9:00-18:00，周六日为空
            List<Map<String, Object>> timeSlots = new ArrayList<>();
            if (day < 5) { // 周一到周五
                Map<String, Object> slot = new HashMap<>();
                slot.put("start", "09:00");
                slot.put("end", "18:00");
                timeSlots.add(slot);
            }
            wh.put("time_slots", timeSlots);
            wh.put("created_at", "2025-11-22T10:00:00");
            wh.put("updated_at", "2025-11-22T10:00:00");
            
            workingHours.add(wh);
        }
        
        return ResponseData.success(workingHours);
    }

    /**
     * 批量更新机构作息时间
     * PUT /api/v1/agencies/{id}/working-hours
     */
    @PutMapping("/{id}/working-hours")
    public ResponseData<List<Map<String, Object>>> updateAgencyWorkingHours(
            @PathVariable Long id,
            @RequestBody Map<String, Object> data
    ) {
        log.info("批量更新机构作息时间（Mock），agencyId={}, data={}", id, data);
        
        // 返回更新后的作息时间
        return getAgencyWorkingHours(id);
    }

    /**
     * 更新单天作息时间
     * PUT /api/v1/agencies/{id}/working-hours/{dayOfWeek}
     */
    @PutMapping("/{id}/working-hours/{dayOfWeek}")
    public ResponseData<Map<String, Object>> updateSingleDayWorkingHours(
            @PathVariable Long id,
            @PathVariable Integer dayOfWeek,
            @RequestBody Map<String, Object> data
    ) {
        log.info("更新单天作息时间（Mock），agencyId={}, dayOfWeek={}, data={}", id, dayOfWeek, data);
        
        Map<String, Object> wh = new HashMap<>();
        wh.put("id", dayOfWeek + 1);
        wh.put("agency_id", id);
        wh.put("day_of_week", dayOfWeek);
        wh.put("time_slots", data.getOrDefault("time_slots", new ArrayList<>()));
        wh.put("created_at", "2025-11-22T10:00:00");
        wh.put("updated_at", "2025-11-22T10:00:00");
        
        return ResponseData.success(wh);
    }

    /**
     * 获取机构下的小组列表
     * GET /api/v1/agencies/{id}/teams
     */
    @GetMapping("/{id}/teams")
    public ResponseData<List<Map<String, Object>>> getAgencyTeams(@PathVariable Long id) {
        log.info("获取机构下的小组列表（Mock），agencyId={}", id);
        
        List<Map<String, Object>> teams = new ArrayList<>();
        teams.add(createTeam(1L, id, "小组1", "team_1", 1, true));
        teams.add(createTeam(2L, id, "小组2", "team_2", 2, true));
        
        return ResponseData.success(teams);
    }

    // Helper methods
    private Map<String, Object> createAgency(Long id, Long tenantId, String name, 
                                            String code, Integer sortOrder, Boolean isActive) {
        Map<String, Object> agency = new HashMap<>();
        agency.put("id", id);
        agency.put("tenant_id", tenantId);
        agency.put("agency_name", name);
        agency.put("agency_code", code);
        agency.put("agency_name_en", name + " (EN)");
        agency.put("contact_person", "联系人");
        agency.put("contact_phone", "13800138000");
        agency.put("contact_email", "contact@example.com");
        agency.put("address", "机构地址");
        agency.put("description", "机构描述");
        agency.put("timezone", 8); // UTC+8
        agency.put("agency_type", "real");
        agency.put("sort_order", sortOrder);
        agency.put("is_active", isActive);
        agency.put("team_count", 5);
        agency.put("collector_count", 20);
        agency.put("case_count", 100);
        agency.put("created_at", "2025-11-22T10:00:00");
        agency.put("updated_at", "2025-11-22T10:00:00");
        return agency;
    }

    private Map<String, Object> createTeam(Long id, Long agencyId, String name, 
                                          String code, Integer sortOrder, Boolean isActive) {
        Map<String, Object> team = new HashMap<>();
        team.put("id", id);
        team.put("agency_id", agencyId);
        team.put("team_name", name);
        team.put("team_code", code);
        team.put("sort_order", sortOrder);
        team.put("is_active", isActive);
        team.put("collector_count", 10);
        team.put("case_count", 50);
        team.put("created_at", "2025-11-22T10:00:00");
        return team;
    }
}




