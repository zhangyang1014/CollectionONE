package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 小组群Mock控制器
 * 提供小组群相关API的Mock数据
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/team-groups")
public class MockTeamGroupController {

    /**
     * 获取小组群列表
     * GET /api/v1/team-groups
     */
    @GetMapping
    public ResponseData<List<Map<String, Object>>> getTeamGroups(
            @RequestParam(required = false) Long tenant_id,
            @RequestParam(required = false) Long agency_id,
            @RequestParam(required = false) Boolean is_active,
            @RequestParam(required = false, defaultValue = "0") Integer skip,
            @RequestParam(required = false, defaultValue = "100") Integer limit
    ) {
        log.info("获取小组群列表（Mock），tenant_id={}, agency_id={}, is_active={}, skip={}, limit={}", 
                tenant_id, agency_id, is_active, skip, limit);
        
        List<Map<String, Object>> teamGroups = new ArrayList<>();
        
        // 生成Mock数据
        // 假设有2个机构，每个机构有2个小组群
        for (long agencyId = 1; agencyId <= 2; agencyId++) {
            // 如果指定了agency_id，只生成该机构的
            if (agency_id != null && !agency_id.equals(agencyId)) {
                continue;
            }
            
            for (long groupId = 1; groupId <= 2; groupId++) {
                long id = (agencyId - 1) * 2 + groupId;
                
                Map<String, Object> teamGroup = new HashMap<>();
                teamGroup.put("id", id);
                teamGroup.put("tenant_id", tenant_id != null ? tenant_id : 1L);
                teamGroup.put("agency_id", agencyId);
                teamGroup.put("group_code", "GROUP_" + agencyId + "_" + groupId);
                teamGroup.put("group_name", "小组群" + agencyId + "-" + groupId);
                teamGroup.put("group_name_en", "Team Group " + agencyId + "-" + groupId);
                teamGroup.put("description", "这是小组群" + agencyId + "-" + groupId + "的描述");
                teamGroup.put("sort_order", (int) id);
                teamGroup.put("is_active", is_active != null ? is_active : true);
                teamGroup.put("agency_name", "机构" + agencyId);
                teamGroup.put("spv_account_name", "SPV管理员" + id);
                teamGroup.put("spv_login_id", "spv_" + agencyId + "_" + groupId);
                teamGroup.put("team_count", 3); // 每个小组群有3个小组
                teamGroup.put("collector_count", 15); // 每个小组群有15个催员
                teamGroup.put("created_at", LocalDateTime.now().minusDays(30).toString());
                teamGroup.put("updated_at", LocalDateTime.now().toString());
                
                teamGroups.add(teamGroup);
            }
        }
        
        // 应用分页
        int start = Math.min(skip, teamGroups.size());
        int end = Math.min(skip + limit, teamGroups.size());
        List<Map<String, Object>> pagedGroups = teamGroups.subList(start, end);
        
        return ResponseData.success(pagedGroups);
    }
    
    /**
     * 获取小组群详情
     * GET /api/v1/team-groups/{teamGroupId}
     */
    @GetMapping("/{teamGroupId}")
    public ResponseData<Map<String, Object>> getTeamGroup(@PathVariable Long teamGroupId) {
        log.info("获取小组群详情（Mock），teamGroupId={}", teamGroupId);
        
        Map<String, Object> teamGroup = new HashMap<>();
        teamGroup.put("id", teamGroupId);
        teamGroup.put("tenant_id", 1L);
        teamGroup.put("agency_id", (teamGroupId - 1) / 2 + 1);
        teamGroup.put("group_code", "GROUP_" + teamGroupId);
        teamGroup.put("group_name", "小组群" + teamGroupId);
        teamGroup.put("group_name_en", "Team Group " + teamGroupId);
        teamGroup.put("description", "这是小组群" + teamGroupId + "的描述");
        teamGroup.put("sort_order", teamGroupId.intValue());
        teamGroup.put("is_active", true);
        teamGroup.put("agency_name", "机构" + ((teamGroupId - 1) / 2 + 1));
        teamGroup.put("spv_account_name", "SPV管理员" + teamGroupId);
        teamGroup.put("spv_login_id", "spv_" + teamGroupId);
        teamGroup.put("spv_email", "spv" + teamGroupId + "@example.com");
        teamGroup.put("spv_mobile", "138001380" + String.format("%02d", teamGroupId));
        teamGroup.put("team_count", 3);
        teamGroup.put("collector_count", 15);
        teamGroup.put("created_at", LocalDateTime.now().minusDays(30).toString());
        teamGroup.put("updated_at", LocalDateTime.now().toString());
        
        return ResponseData.success(teamGroup);
    }
    
    /**
     * 创建小组群
     * POST /api/v1/team-groups
     */
    @PostMapping
    public ResponseData<Map<String, Object>> createTeamGroup(@RequestBody Map<String, Object> request) {
        log.info("创建小组群（Mock），request={}", request);
        
        Map<String, Object> teamGroup = new HashMap<>();
        long id = System.currentTimeMillis() % 10000; // 使用时间戳生成ID
        
        teamGroup.put("id", id);
        teamGroup.put("tenant_id", request.getOrDefault("tenant_id", 1L));
        teamGroup.put("agency_id", request.getOrDefault("agency_id", 1L));
        teamGroup.put("group_code", request.getOrDefault("group_code", "GROUP_" + id));
        teamGroup.put("group_name", request.getOrDefault("group_name", "新小组群"));
        teamGroup.put("group_name_en", request.getOrDefault("group_name_en", "New Team Group"));
        teamGroup.put("description", request.getOrDefault("description", ""));
        teamGroup.put("sort_order", request.getOrDefault("sort_order", 0));
        teamGroup.put("is_active", request.getOrDefault("is_active", true));
        teamGroup.put("agency_name", "机构" + request.getOrDefault("agency_id", 1L));
        teamGroup.put("spv_account_name", request.getOrDefault("spv_account_name", "SPV管理员"));
        teamGroup.put("spv_login_id", request.getOrDefault("spv_login_id", "spv_" + id));
        teamGroup.put("team_count", 0);
        teamGroup.put("collector_count", 0);
        teamGroup.put("created_at", LocalDateTime.now().toString());
        teamGroup.put("updated_at", LocalDateTime.now().toString());
        
        return ResponseData.success(teamGroup);
    }
    
    /**
     * 更新小组群
     * PUT /api/v1/team-groups/{teamGroupId}
     */
    @PutMapping("/{teamGroupId}")
    public ResponseData<Map<String, Object>> updateTeamGroup(
            @PathVariable Long teamGroupId,
            @RequestBody Map<String, Object> request
    ) {
        log.info("更新小组群（Mock），teamGroupId={}, request={}", teamGroupId, request);
        
        Map<String, Object> teamGroup = new HashMap<>();
        teamGroup.put("id", teamGroupId);
        teamGroup.put("tenant_id", request.getOrDefault("tenant_id", 1L));
        teamGroup.put("agency_id", request.getOrDefault("agency_id", 1L));
        teamGroup.put("group_code", request.getOrDefault("group_code", "GROUP_" + teamGroupId));
        teamGroup.put("group_name", request.getOrDefault("group_name", "小组群" + teamGroupId));
        teamGroup.put("group_name_en", request.getOrDefault("group_name_en", "Team Group " + teamGroupId));
        teamGroup.put("description", request.getOrDefault("description", ""));
        teamGroup.put("sort_order", request.getOrDefault("sort_order", teamGroupId.intValue()));
        teamGroup.put("is_active", request.getOrDefault("is_active", true));
        teamGroup.put("agency_name", "机构" + request.getOrDefault("agency_id", 1L));
        teamGroup.put("spv_account_name", request.getOrDefault("spv_account_name", "SPV管理员"));
        teamGroup.put("spv_login_id", request.getOrDefault("spv_login_id", "spv_" + teamGroupId));
        teamGroup.put("team_count", 3);
        teamGroup.put("collector_count", 15);
        teamGroup.put("created_at", LocalDateTime.now().minusDays(30).toString());
        teamGroup.put("updated_at", LocalDateTime.now().toString());
        
        return ResponseData.success(teamGroup);
    }
    
    /**
     * 删除小组群
     * DELETE /api/v1/team-groups/{teamGroupId}
     */
    @DeleteMapping("/{teamGroupId}")
    public ResponseData<Map<String, Object>> deleteTeamGroup(@PathVariable Long teamGroupId) {
        log.info("删除小组群（Mock），teamGroupId={}", teamGroupId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "小组群删除成功");
        result.put("id", teamGroupId);
        
        return ResponseData.success(result);
    }
}




