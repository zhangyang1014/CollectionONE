package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.entity.AgencyWorkingHours;
import com.cco.service.AgencyWorkingHoursService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.*;

/**
 * 催收机构Controller - Mock实现
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/agencies")
public class AgencyController {
    
    @Autowired
    private AgencyWorkingHoursService agencyWorkingHoursService;

    /**
     * 获取催收机构列表
     */
    @GetMapping
    public ResponseData<List<Map<String, Object>>> getAgencies(
            @RequestParam(required = false) Long tenant_id,
            @RequestParam(required = false) Boolean is_active) {
        log.info("========== 获取催收机构列表，tenant_id={}, is_active={} ==========", tenant_id, is_active);
        
        List<Map<String, Object>> agencies = new ArrayList<>();
        
        // Mock数据
        Map<String, Object> agency1 = new HashMap<>();
        agency1.put("id", 1L);
        agency1.put("tenant_id", tenant_id != null ? tenant_id : 1L);
        agency1.put("agency_code", "AGENCY001");
        agency1.put("agency_name", "测试机构1");
        agency1.put("agency_name_en", "Test Agency 1");
        agency1.put("contact_person", "张三");
        agency1.put("contact_phone", "13800138001");
        agency1.put("contact_email", "agency1@test.com");
        agency1.put("address", "北京市朝阳区");
        agency1.put("description", "测试机构1的描述");
        agency1.put("agency_type", "real");
        agency1.put("timezone", 8);
        agency1.put("sort_order", 1);
        agency1.put("is_active", true);
        agency1.put("team_count", 2);
        agency1.put("collector_count", 10);
        agency1.put("case_count", 100);
        agency1.put("created_at", "2025-01-01T00:00:00");
        agency1.put("updated_at", "2025-11-25T00:00:00");
        agencies.add(agency1);
        
        Map<String, Object> agency2 = new HashMap<>();
        agency2.put("id", 2L);
        agency2.put("tenant_id", tenant_id != null ? tenant_id : 1L);
        agency2.put("agency_code", "AGENCY002");
        agency2.put("agency_name", "测试机构2");
        agency2.put("agency_name_en", "Test Agency 2");
        agency2.put("contact_person", "李四");
        agency2.put("contact_phone", "13800138002");
        agency2.put("contact_email", "agency2@test.com");
        agency2.put("address", "上海市浦东新区");
        agency2.put("description", "测试机构2的描述");
        agency2.put("agency_type", "real");
        agency2.put("timezone", 8);
        agency2.put("sort_order", 2);
        agency2.put("is_active", true);
        agency2.put("team_count", 3);
        agency2.put("collector_count", 15);
        agency2.put("case_count", 150);
        agency2.put("created_at", "2025-01-02T00:00:00");
        agency2.put("updated_at", "2025-11-25T00:00:00");
        agencies.add(agency2);
        
        // 过滤逻辑
        if (tenant_id != null) {
            agencies.removeIf(a -> !tenant_id.equals(a.get("tenant_id")));
        }
        
        if (is_active != null) {
            agencies.removeIf(a -> !is_active.equals(a.get("is_active")));
        }
        
        log.info("========== 返回催收机构列表，数量={} ==========", agencies.size());
        return ResponseData.success(agencies);
    }

    /**
     * 获取催收机构详情
     */
    @GetMapping("/{id}")
    public ResponseData<Map<String, Object>> getAgency(@PathVariable Long id) {
        log.info("========== 获取催收机构详情，id={} ==========", id);
        
        Map<String, Object> agency = new HashMap<>();
        agency.put("id", id);
        agency.put("tenant_id", 1L);
        agency.put("agency_code", "AGENCY" + String.format("%03d", id));
        agency.put("agency_name", "测试机构" + id);
        agency.put("agency_name_en", "Test Agency " + id);
        agency.put("contact_person", "联系人" + id);
        agency.put("contact_phone", "1380013800" + id);
        agency.put("contact_email", "agency" + id + "@test.com");
        agency.put("address", "地址" + id);
        agency.put("description", "机构描述" + id);
        agency.put("sort_order", id.intValue());
        agency.put("is_active", true);
        agency.put("created_at", "2025-01-01T00:00:00");
        agency.put("updated_at", "2025-11-25T00:00:00");
        
        return ResponseData.success(agency);
    }

    /**
     * 创建催收机构
     */
    @PostMapping
    public ResponseData<Map<String, Object>> createAgency(@RequestBody Map<String, Object> request) {
        log.info("========== 创建催收机构，request={} ==========", request);
        
        Map<String, Object> agency = new HashMap<>();
        agency.put("id", System.currentTimeMillis());
        agency.put("tenant_id", request.get("tenant_id") != null ? request.get("tenant_id") : request.get("tenantId"));
        agency.put("agency_code", request.get("agency_code") != null ? request.get("agency_code") : request.get("agencyCode"));
        agency.put("agency_name", request.get("agency_name") != null ? request.get("agency_name") : request.get("agencyName"));
        agency.put("agency_name_en", request.get("agency_name_en") != null ? request.get("agency_name_en") : request.get("agencyNameEn"));
        agency.put("contact_person", request.get("contact_person") != null ? request.get("contact_person") : request.get("contactPerson"));
        agency.put("contact_phone", request.get("contact_phone") != null ? request.get("contact_phone") : request.get("contactPhone"));
        agency.put("contact_email", request.get("contact_email") != null ? request.get("contact_email") : request.get("contactEmail"));
        agency.put("address", request.get("address"));
        agency.put("description", request.get("description"));
        agency.put("sort_order", request.getOrDefault("sort_order", request.getOrDefault("sortOrder", 0)));
        agency.put("is_active", request.getOrDefault("is_active", request.getOrDefault("isActive", true)));
        agency.put("created_at", new Date().toString());
        agency.put("updated_at", new Date().toString());
        
        return ResponseData.success(agency);
    }

    /**
     * 更新催收机构
     */
    @PutMapping("/{id}")
    public ResponseData<Map<String, Object>> updateAgency(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("========== 更新催收机构，id={}, request={} ==========", id, request);
        
        Map<String, Object> agency = new HashMap<>();
        agency.put("id", id);
        agency.put("tenant_id", request.get("tenant_id") != null ? request.get("tenant_id") : request.get("tenantId"));
        agency.put("agency_code", request.get("agency_code") != null ? request.get("agency_code") : request.getOrDefault("agencyCode", "AGENCY" + String.format("%03d", id)));
        agency.put("agency_name", request.get("agency_name") != null ? request.get("agency_name") : request.getOrDefault("agencyName", "测试机构" + id));
        agency.put("agency_name_en", request.get("agency_name_en") != null ? request.get("agency_name_en") : request.getOrDefault("agencyNameEn", "Test Agency " + id));
        agency.put("contact_person", request.get("contact_person") != null ? request.get("contact_person") : request.get("contactPerson"));
        agency.put("contact_phone", request.get("contact_phone") != null ? request.get("contact_phone") : request.get("contactPhone"));
        agency.put("contact_email", request.get("contact_email") != null ? request.get("contact_email") : request.get("contactEmail"));
        agency.put("address", request.get("address"));
        agency.put("description", request.get("description"));
        agency.put("sort_order", request.getOrDefault("sort_order", request.getOrDefault("sortOrder", id.intValue())));
        agency.put("is_active", request.getOrDefault("is_active", request.getOrDefault("isActive", true)));
        agency.put("updated_at", new Date().toString());
        
        return ResponseData.success(agency);
    }

    /**
     * 删除催收机构
     */
    @DeleteMapping("/{id}")
    public ResponseData<String> deleteAgency(@PathVariable Long id) {
        log.info("========== 删除催收机构，id={} ==========", id);
        return ResponseData.success("删除成功");
    }

    /**
     * 获取指定机构的小组列表
     */
    @GetMapping("/{agencyId}/teams")
    public ResponseData<List<Map<String, Object>>> getAgencyTeams(@PathVariable Long agencyId) {
        log.info("========== 获取机构小组列表，agencyId={} ==========", agencyId);
        
        List<Map<String, Object>> teams = new ArrayList<>();
        
        // Mock数据
        Map<String, Object> team1 = new HashMap<>();
        team1.put("id", 1L);
        team1.put("agency_id", agencyId);
        team1.put("team_group_id", 1L);
        team1.put("queue_id", 1L);
        team1.put("team_code", "TEAM001");
        team1.put("team_name", "测试小组1");
        team1.put("team_name_en", "Test Team 1");
        team1.put("team_leader_id", 1L);
        team1.put("team_type", "normal");
        team1.put("description", "测试小组1的描述");
        team1.put("max_case_count", 200);
        team1.put("sort_order", 1);
        team1.put("is_active", true);
        team1.put("agency_name", "测试机构" + agencyId);
        team1.put("team_group_name", "测试小组群1");
        team1.put("queue_name", "C队列");
        team1.put("team_leader_name", "组长1");
        team1.put("collector_count", 5);
        team1.put("case_count", 50);
        team1.put("created_at", "2025-01-01T00:00:00");
        team1.put("updated_at", "2025-11-25T00:00:00");
        teams.add(team1);
        
        Map<String, Object> team2 = new HashMap<>();
        team2.put("id", 2L);
        team2.put("agency_id", agencyId);
        team2.put("team_group_id", 1L);
        team2.put("queue_id", 2L);
        team2.put("team_code", "TEAM002");
        team2.put("team_name", "测试小组2");
        team2.put("team_name_en", "Test Team 2");
        team2.put("team_leader_id", 2L);
        team2.put("team_type", "urgent");
        team2.put("description", "测试小组2的描述");
        team2.put("max_case_count", 300);
        team2.put("sort_order", 2);
        team2.put("is_active", true);
        team2.put("agency_name", "测试机构" + agencyId);
        team2.put("team_group_name", "测试小组群1");
        team2.put("queue_name", "S0队列");
        team2.put("team_leader_name", "组长2");
        team2.put("collector_count", 8);
        team2.put("case_count", 80);
        team2.put("created_at", "2025-01-02T00:00:00");
        team2.put("updated_at", "2025-11-25T00:00:00");
        teams.add(team2);
        
        log.info("========== 返回机构小组列表，数量={} ==========", teams.size());
        return ResponseData.success(teams);
    }

    /**
     * 获取机构作息时间
     * 根据PRD要求：GET /api/v1/agencies/{agency_id}/working-hours
     */
    @GetMapping("/{agency_id}/working-hours")
    public ResponseData<List<AgencyWorkingHours>> getAgencyWorkingHours(@PathVariable Long agency_id) {
        log.info("========== 获取机构作息时间，agency_id={} ==========", agency_id);
        
        List<AgencyWorkingHours> workingHours = agencyWorkingHoursService.getByAgencyId(agency_id);
        
        return ResponseData.success(workingHours);
    }

    /**
     * 配置机构作息时间
     * 根据PRD要求：PUT /api/v1/agencies/{agency_id}/working-hours
     */
    @PutMapping("/{agency_id}/working-hours")
    public ResponseData<List<AgencyWorkingHours>> updateAgencyWorkingHours(
            @PathVariable Long agency_id,
            @RequestBody Map<String, Object> request) {
        log.info("========== 配置机构作息时间，agency_id={}, request={} ==========", agency_id, request);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> workingHoursList = (List<Map<String, Object>>) request.get("working_hours");
        
        if (workingHoursList == null || workingHoursList.isEmpty()) {
            return ResponseData.error(400, "作息时间列表不能为空");
        }
        
        // 转换为实体列表
        List<AgencyWorkingHours> workingHours = new ArrayList<>();
        for (Map<String, Object> whMap : workingHoursList) {
            AgencyWorkingHours wh = new AgencyWorkingHours();
            wh.setAgencyId(agency_id);
            
            Object dayOfWeekObj = whMap.get("day_of_week");
            if (dayOfWeekObj instanceof Integer) {
                wh.setDayOfWeek((Integer) dayOfWeekObj);
            } else if (dayOfWeekObj instanceof String) {
                try {
                    wh.setDayOfWeek(Integer.parseInt((String) dayOfWeekObj));
                } catch (NumberFormatException e) {
                    return ResponseData.error(400, "day_of_week格式错误：" + dayOfWeekObj);
                }
            }
            
            // 解析时间
            String startTimeStr = (String) whMap.get("start_time");
            String endTimeStr = (String) whMap.get("end_time");
            if (startTimeStr != null && !startTimeStr.isEmpty()) {
                wh.setStartTime(LocalTime.parse(startTimeStr));
            }
            if (endTimeStr != null && !endTimeStr.isEmpty()) {
                wh.setEndTime(LocalTime.parse(endTimeStr));
            }
            
            wh.setIsActive((Boolean) whMap.getOrDefault("is_active", true));
            workingHours.add(wh);
        }
        
        // 批量更新
        List<AgencyWorkingHours> result = agencyWorkingHoursService.batchUpdate(agency_id, workingHours);
        
        log.info("========== 配置机构作息时间成功，数量={} ==========", result.size());
        return ResponseData.success(result);
    }

    /**
     * 获取机构统计信息
     * 根据PRD要求：GET /api/v1/agencies/{agency_id}/statistics
     */
    @GetMapping("/{agency_id}/statistics")
    public ResponseData<Map<String, Object>> getAgencyStatistics(@PathVariable Long agency_id) {
        log.info("========== 获取机构统计信息，agency_id={} ==========", agency_id);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("agency_id", agency_id);
        statistics.put("team_count", 5); // 小组数量
        statistics.put("collector_count", 25); // 催员数量
        
        return ResponseData.success(statistics);
    }
}

