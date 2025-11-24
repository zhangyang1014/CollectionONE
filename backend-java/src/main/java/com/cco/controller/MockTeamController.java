package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 催收小组Mock控制器
 * 提供催收小组相关API的Mock数据
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/teams")
public class MockTeamController {

    /**
     * 获取小组下的催员列表
     * GET /api/v1/teams/{teamId}/collectors
     */
    @GetMapping("/{teamId}/collectors")
    public ResponseData<List<Map<String, Object>>> getTeamCollectors(@PathVariable Long teamId) {
        log.info("获取小组下的催员列表（Mock），teamId={}", teamId);
        
        List<Map<String, Object>> collectors = new ArrayList<>();
        
        // 为每个小组生成5个催员
        for (int i = 1; i <= 5; i++) {
            collectors.add(createCollector(
                (long) (teamId * 10 + i),
                teamId,
                (long) ((teamId - 1) / 2 + 1), // 假设teamId 1,2属于agency 1，3,4属于agency 2
                "催员" + (teamId * 10 + i),
                "collector_" + (teamId * 10 + i),
                "active",
                true
            ));
        }
        
        return ResponseData.success(collectors);
    }
    
    /**
     * 获取小组下的管理员账号列表
     * GET /api/v1/teams/{teamId}/admin-accounts
     */
    @GetMapping("/{teamId}/admin-accounts")
    public ResponseData<List<Map<String, Object>>> getTeamAdminAccounts(@PathVariable Long teamId) {
        log.info("获取小组管理员账号列表（Mock），teamId={}", teamId);
        
        List<Map<String, Object>> accounts = new ArrayList<>();
        
        // 为每个小组生成2-3个管理员账号（不同角色）
        String[] roles = {"team_leader", "quality_inspector", "statistician"};
        String[] roleNames = {"小组长", "质检员", "统计员"};
        
        for (int i = 0; i < 3; i++) {
            Map<String, Object> account = new HashMap<>();
            account.put("id", teamId * 10 + i + 1);
            account.put("account_code", "TEAM" + teamId + "_ADMIN" + (i + 1));
            account.put("account_name", roleNames[i] + (i + 1));
            account.put("login_id", "team" + teamId + "_" + roles[i]);
            account.put("tenant_id", 1L);
            account.put("tenant_name", "百熵企业");
            account.put("agency_id", (long) ((teamId - 1) / 2 + 1));
            account.put("agency_name", "机构" + ((teamId - 1) / 2 + 1));
            account.put("team_id", teamId);
            account.put("team_name", "小组" + teamId);
            account.put("role", roles[i]);
            account.put("mobile", "138001380" + String.format("%02d", teamId * 10 + i + 1));
            account.put("email", "team" + teamId + "_" + roles[i] + "@example.com");
            account.put("remark", roleNames[i] + "账号");
            account.put("is_active", true);
            account.put("created_at", LocalDateTime.now().minusDays(30).toString());
            account.put("updated_at", LocalDateTime.now().toString());
            
            accounts.add(account);
        }
        
        return ResponseData.success(accounts);
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
}



