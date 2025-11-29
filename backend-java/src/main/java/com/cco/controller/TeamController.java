package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 催收小组Controller - Mock实现
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/teams")
public class TeamController {

    /**
     * 获取催收小组列表
     */
    @GetMapping
    public ResponseData<List<Map<String, Object>>> getTeams(
            @RequestParam(required = false) Long agency_id,
            @RequestParam(required = false) Boolean is_active) {
        log.info("========== 获取催收小组列表，agency_id={}, is_active={} ==========", agency_id, is_active);
        
        List<Map<String, Object>> teams = new ArrayList<>();
        
        // Mock数据
        Map<String, Object> team1 = new HashMap<>();
        team1.put("id", 1L);
        team1.put("agency_id", agency_id != null ? agency_id : 1L);
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
        team1.put("agency_name", "测试机构1");
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
        team2.put("agency_id", agency_id != null ? agency_id : 1L);
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
        team2.put("agency_name", "测试机构1");
        team2.put("team_group_name", "测试小组群1");
        team2.put("queue_name", "S0队列");
        team2.put("team_leader_name", "组长2");
        team2.put("collector_count", 8);
        team2.put("case_count", 80);
        team2.put("created_at", "2025-01-02T00:00:00");
        team2.put("updated_at", "2025-11-25T00:00:00");
        teams.add(team2);
        
        // 过滤逻辑
        if (agency_id != null) {
            teams.removeIf(t -> !agency_id.equals(t.get("agency_id")));
        }
        
        if (is_active != null) {
            teams.removeIf(t -> !is_active.equals(t.get("is_active")));
        }
        
        log.info("========== 返回催收小组列表，数量={} ==========", teams.size());
        return ResponseData.success(teams);
    }

    /**
     * 获取催收小组详情
     */
    @GetMapping("/{id}")
    public ResponseData<Map<String, Object>> getTeam(@PathVariable Long id) {
        log.info("========== 获取催收小组详情，id={} ==========", id);
        
        Map<String, Object> team = new HashMap<>();
        team.put("id", id);
        team.put("agency_id", 1L);
        team.put("team_group_id", 1L);
        team.put("queue_id", 1L);
        team.put("team_code", "TEAM" + String.format("%03d", id));
        team.put("team_name", "测试小组" + id);
        team.put("team_name_en", "Test Team " + id);
        team.put("team_leader_id", id);
        team.put("team_type", "normal");
        team.put("description", "小组描述" + id);
        team.put("max_case_count", 200);
        team.put("sort_order", id.intValue());
        team.put("is_active", true);
        team.put("collector_count", 0);
        team.put("case_count", 0);
        team.put("created_at", "2025-01-01T00:00:00");
        team.put("updated_at", "2025-11-25T00:00:00");
        
        return ResponseData.success(team);
    }

    /**
     * 创建催收小组
     */
    @PostMapping
    public ResponseData<Map<String, Object>> createTeam(@RequestBody Map<String, Object> request) {
        log.info("========== 创建催收小组，request={} ==========", request);
        
        Map<String, Object> team = new HashMap<>();
        team.put("id", System.currentTimeMillis());
        team.put("agency_id", request.get("agency_id") != null ? request.get("agency_id") : request.get("agencyId"));
        team.put("team_group_id", request.get("team_group_id") != null ? request.get("team_group_id") : request.get("teamGroupId"));
        team.put("queue_id", request.get("queue_id") != null ? request.get("queue_id") : request.get("queueId"));
        team.put("team_code", request.get("team_code") != null ? request.get("team_code") : request.get("teamCode"));
        team.put("team_name", request.get("team_name") != null ? request.get("team_name") : request.get("teamName"));
        team.put("team_name_en", request.get("team_name_en") != null ? request.get("team_name_en") : request.get("teamNameEn"));
        team.put("team_leader_id", request.get("team_leader_id") != null ? request.get("team_leader_id") : request.get("teamLeaderId"));
        team.put("team_type", request.get("team_type") != null ? request.get("team_type") : request.get("teamType"));
        team.put("description", request.get("description"));
        team.put("max_case_count", request.getOrDefault("max_case_count", request.getOrDefault("maxCaseCount", 200)));
        team.put("sort_order", request.getOrDefault("sort_order", request.getOrDefault("sortOrder", 0)));
        team.put("is_active", request.getOrDefault("is_active", request.getOrDefault("isActive", true)));
        team.put("collector_count", 0);
        team.put("case_count", 0);
        team.put("created_at", new Date().toString());
        team.put("updated_at", new Date().toString());
        
        return ResponseData.success(team);
    }

    /**
     * 更新催收小组
     */
    @PutMapping("/{id}")
    public ResponseData<Map<String, Object>> updateTeam(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("========== 更新催收小组，id={}, request={} ==========", id, request);
        
        Map<String, Object> team = new HashMap<>();
        team.put("id", id);
        team.put("agency_id", request.get("agency_id") != null ? request.get("agency_id") : request.get("agencyId"));
        team.put("team_group_id", request.get("team_group_id") != null ? request.get("team_group_id") : request.get("teamGroupId"));
        team.put("queue_id", request.get("queue_id") != null ? request.get("queue_id") : request.get("queueId"));
        team.put("team_code", request.get("team_code") != null ? request.get("team_code") : request.getOrDefault("teamCode", "TEAM" + String.format("%03d", id)));
        team.put("team_name", request.get("team_name") != null ? request.get("team_name") : request.getOrDefault("teamName", "测试小组" + id));
        team.put("team_name_en", request.get("team_name_en") != null ? request.get("team_name_en") : request.getOrDefault("teamNameEn", "Test Team " + id));
        team.put("team_leader_id", request.get("team_leader_id") != null ? request.get("team_leader_id") : request.get("teamLeaderId"));
        team.put("team_type", request.get("team_type") != null ? request.get("team_type") : request.get("teamType"));
        team.put("description", request.get("description"));
        team.put("max_case_count", request.getOrDefault("max_case_count", request.getOrDefault("maxCaseCount", 200)));
        team.put("sort_order", request.getOrDefault("sort_order", request.getOrDefault("sortOrder", id.intValue())));
        team.put("is_active", request.getOrDefault("is_active", request.getOrDefault("isActive", true)));
        team.put("updated_at", new Date().toString());
        
        return ResponseData.success(team);
    }

    /**
     * 删除催收小组
     */
    @DeleteMapping("/{id}")
    public ResponseData<String> deleteTeam(@PathVariable Long id) {
        log.info("========== 删除催收小组，id={} ==========", id);
        return ResponseData.success("删除成功");
    }

    /**
     * 获取小组管理员账户列表
     */
    @GetMapping("/{teamId}/admin-accounts")
    public ResponseData<List<Map<String, Object>>> getTeamAdminAccounts(@PathVariable Long teamId) {
        log.info("========== 获取小组管理员账户列表，teamId={} ==========", teamId);
        
        List<Map<String, Object>> accounts = new ArrayList<>();
        
        // Mock数据
        Map<String, Object> account1 = new HashMap<>();
        account1.put("id", 1L);
        account1.put("account_code", "ADMIN" + String.format("%03d", teamId) + "001");
        account1.put("account_name", "小组管理员" + teamId + "-1");
        account1.put("login_id", "admin" + teamId + "_1");
        account1.put("password", "******"); // 不返回真实密码
        account1.put("agency_id", 1L);
        account1.put("team_id", teamId);
        account1.put("role", "TEAM_LEADER");
        account1.put("mobile", "1380000" + String.format("%04d", teamId * 10 + 1));
        account1.put("email", "admin" + teamId + "_1@example.com");
        account1.put("remark", "小组" + teamId + "的管理员1");
        account1.put("is_active", true);
        account1.put("created_at", "2025-01-01T00:00:00");
        account1.put("updated_at", "2025-11-25T00:00:00");
        accounts.add(account1);
        
        Map<String, Object> account2 = new HashMap<>();
        account2.put("id", 2L);
        account2.put("account_code", "ADMIN" + String.format("%03d", teamId) + "002");
        account2.put("account_name", "小组管理员" + teamId + "-2");
        account2.put("login_id", "admin" + teamId + "_2");
        account2.put("password", "******");
        account2.put("agency_id", 1L);
        account2.put("team_id", teamId);
        account2.put("role", "TEAM_LEADER");
        account2.put("mobile", "1380000" + String.format("%04d", teamId * 10 + 2));
        account2.put("email", "admin" + teamId + "_2@example.com");
        account2.put("remark", "小组" + teamId + "的管理员2");
        account2.put("is_active", true);
        account2.put("created_at", "2025-01-02T00:00:00");
        account2.put("updated_at", "2025-11-25T00:00:00");
        accounts.add(account2);
        
        log.info("========== 返回小组管理员账户列表，数量={} ==========", accounts.size());
        return ResponseData.success(accounts);
    }

    /**
     * 获取小组催员列表
     */
    @GetMapping("/{teamId}/collectors")
    public ResponseData<List<Map<String, Object>>> getTeamCollectors(@PathVariable Long teamId) {
        log.info("========== 获取小组催员列表，teamId={} ==========", teamId);
        
        List<Map<String, Object>> collectors = new ArrayList<>();
        
        // Mock数据
        Map<String, Object> collector1 = new HashMap<>();
        collector1.put("id", 1L);
        collector1.put("tenant_id", 1L);
        collector1.put("agency_id", 1L);
        collector1.put("team_id", teamId);
        collector1.put("user_id", 100L + teamId * 10 + 1);
        collector1.put("collector_code", "COLLECTOR" + String.format("%03d", teamId) + "001");
        collector1.put("collector_name", "催员" + teamId + "-1");
        collector1.put("mobile_number", "1390000" + String.format("%04d", teamId * 10 + 1));
        collector1.put("email", "collector" + teamId + "_1@example.com");
        collector1.put("employee_no", "EMP" + String.format("%03d", teamId) + "001");
        collector1.put("collector_level", "A");
        collector1.put("max_case_count", 100);
        collector1.put("current_case_count", 45);
        collector1.put("specialties", Arrays.asList("电话催收", "短信催收"));
        collector1.put("performance_score", 85.5);
        collector1.put("status", "active");
        collector1.put("hire_date", "2025-01-01");
        collector1.put("is_active", true);
        collector1.put("created_at", "2025-01-01T00:00:00");
        collector1.put("updated_at", "2025-11-25T00:00:00");
        collectors.add(collector1);
        
        Map<String, Object> collector2 = new HashMap<>();
        collector2.put("id", 2L);
        collector2.put("tenant_id", 1L);
        collector2.put("agency_id", 1L);
        collector2.put("team_id", teamId);
        collector2.put("user_id", 100L + teamId * 10 + 2);
        collector2.put("collector_code", "COLLECTOR" + String.format("%03d", teamId) + "002");
        collector2.put("collector_name", "催员" + teamId + "-2");
        collector2.put("mobile_number", "1390000" + String.format("%04d", teamId * 10 + 2));
        collector2.put("email", "collector" + teamId + "_2@example.com");
        collector2.put("employee_no", "EMP" + String.format("%03d", teamId) + "002");
        collector2.put("collector_level", "B");
        collector2.put("max_case_count", 80);
        collector2.put("current_case_count", 35);
        collector2.put("specialties", Arrays.asList("电话催收"));
        collector2.put("performance_score", 75.0);
        collector2.put("status", "active");
        collector2.put("hire_date", "2025-01-02");
        collector2.put("is_active", true);
        collector2.put("created_at", "2025-01-02T00:00:00");
        collector2.put("updated_at", "2025-11-25T00:00:00");
        collectors.add(collector2);
        
        Map<String, Object> collector3 = new HashMap<>();
        collector3.put("id", 3L);
        collector3.put("tenant_id", 1L);
        collector3.put("agency_id", 1L);
        collector3.put("team_id", teamId);
        collector3.put("user_id", 100L + teamId * 10 + 3);
        collector3.put("collector_code", "COLLECTOR" + String.format("%03d", teamId) + "003");
        collector3.put("collector_name", "催员" + teamId + "-3");
        collector3.put("mobile_number", "1390000" + String.format("%04d", teamId * 10 + 3));
        collector3.put("email", "collector" + teamId + "_3@example.com");
        collector3.put("employee_no", "EMP" + String.format("%03d", teamId) + "003");
        collector3.put("collector_level", "C");
        collector3.put("max_case_count", 60);
        collector3.put("current_case_count", 25);
        collector3.put("specialties", Arrays.asList("短信催收", "邮件催收"));
        collector3.put("performance_score", 65.5);
        collector3.put("status", "active");
        collector3.put("hire_date", "2025-01-03");
        collector3.put("is_active", true);
        collector3.put("created_at", "2025-01-03T00:00:00");
        collector3.put("updated_at", "2025-11-25T00:00:00");
        collectors.add(collector3);
        
        log.info("========== 返回小组催员列表，数量={} ==========", collectors.size());
        return ResponseData.success(collectors);
    }
}

