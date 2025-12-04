package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.entity.TeamAdminAccount;
import com.cco.service.TeamAdminAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 小组群Controller - Mock实现
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/team-groups")
public class TeamGroupController {
    
    @Autowired
    private TeamAdminAccountService teamAdminAccountService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取小组群列表
     */
    @GetMapping
    public ResponseData<List<Map<String, Object>>> getTeamGroups(
            @RequestParam(required = false) Long tenant_id,
            @RequestParam(required = false) Long agency_id,
            @RequestParam(required = false) Boolean is_active) {
        log.info("========== 获取小组群列表，tenant_id={}, agency_id={}, is_active={} ==========", 
                tenant_id, agency_id, is_active);
        
        List<Map<String, Object>> teamGroups = new ArrayList<>();
        
        // Mock数据
        Map<String, Object> group1 = new HashMap<>();
        group1.put("id", 1L);
        group1.put("tenant_id", tenant_id != null ? tenant_id : 1L);
        group1.put("agency_id", agency_id != null ? agency_id : 1L);
        group1.put("group_code", "GROUP001");
        group1.put("group_name", "测试小组群1");
        group1.put("group_name_en", "Test Team Group 1");
        group1.put("description", "测试小组群1的描述");
        group1.put("sort_order", 1);
        group1.put("is_active", true);
        group1.put("agency_name", "测试机构1");
        group1.put("spv_account_name", "SPV001");
        group1.put("spv_login_id", "spv001");
        group1.put("team_count", 2);
        group1.put("collector_count", 10);
        group1.put("created_at", "2025-01-01T00:00:00");
        group1.put("updated_at", "2025-11-25T00:00:00");
        teamGroups.add(group1);
        
        Map<String, Object> group2 = new HashMap<>();
        group2.put("id", 2L);
        group2.put("tenant_id", tenant_id != null ? tenant_id : 1L);
        group2.put("agency_id", agency_id != null ? agency_id : 1L);
        group2.put("group_code", "GROUP002");
        group2.put("group_name", "测试小组群2");
        group2.put("group_name_en", "Test Team Group 2");
        group2.put("description", "测试小组群2的描述");
        group2.put("sort_order", 2);
        group2.put("is_active", true);
        group2.put("agency_name", "测试机构1");
        group2.put("spv_account_name", "SPV002");
        group2.put("spv_login_id", "spv002");
        group2.put("team_count", 3);
        group2.put("collector_count", 15);
        group2.put("created_at", "2025-01-02T00:00:00");
        group2.put("updated_at", "2025-11-25T00:00:00");
        teamGroups.add(group2);
        
        // 过滤逻辑
        if (tenant_id != null) {
            teamGroups.removeIf(g -> !tenant_id.equals(g.get("tenant_id")));
        }
        
        if (agency_id != null) {
            teamGroups.removeIf(g -> !agency_id.equals(g.get("agency_id")));
        }
        
        if (is_active != null) {
            teamGroups.removeIf(g -> !is_active.equals(g.get("is_active")));
        }
        
        log.info("========== 返回小组群列表，数量={} ==========", teamGroups.size());
        return ResponseData.success(teamGroups);
    }

    /**
     * 获取小组群详情
     */
    @GetMapping("/{id}")
    public ResponseData<Map<String, Object>> getTeamGroup(@PathVariable Long id) {
        log.info("========== 获取小组群详情，id={} ==========", id);
        
        Map<String, Object> group = new HashMap<>();
        group.put("id", id);
        group.put("tenant_id", 1L);
        group.put("agency_id", 1L);
        group.put("group_code", "GROUP" + String.format("%03d", id));
        group.put("group_name", "测试小组群" + id);
        group.put("group_name_en", "Test Team Group " + id);
        group.put("description", "小组群描述" + id);
        group.put("sort_order", id.intValue());
        group.put("is_active", true);
        group.put("agency_name", "测试机构1");
        group.put("spv_account_name", "SPV" + String.format("%03d", id));
        group.put("spv_login_id", "spv" + id);
        group.put("team_count", 0);
        group.put("collector_count", 0);
        group.put("created_at", "2025-01-01T00:00:00");
        group.put("updated_at", "2025-11-25T00:00:00");
        
        return ResponseData.success(group);
    }

    /**
     * 创建小组群（同时创建小组群管理员账号）
     * 根据PRD要求，创建小组群时必须同时创建小组群管理员账号
     */
    @PostMapping
    @Transactional
    public ResponseData<Map<String, Object>> createTeamGroup(@RequestBody Map<String, Object> request) {
        log.info("========== 创建小组群，request={} ==========", request);
        
        // 创建小组群
        Map<String, Object> group = new HashMap<>();
        Long groupId = System.currentTimeMillis();
        group.put("id", groupId);
        Long tenantId = getLongValue(request.get("tenant_id") != null ? request.get("tenant_id") : request.get("tenantId"));
        Long agencyId = getLongValue(request.get("agency_id") != null ? request.get("agency_id") : request.get("agencyId"));
        group.put("tenant_id", tenantId);
        group.put("agency_id", agencyId);
        group.put("group_code", request.get("group_code") != null ? request.get("group_code") : request.get("groupCode"));
        group.put("group_name", request.get("group_name") != null ? request.get("group_name") : request.get("groupName"));
        group.put("group_name_en", request.get("group_name_en") != null ? request.get("group_name_en") : request.get("groupNameEn"));
        group.put("description", request.get("description"));
        group.put("sort_order", request.getOrDefault("sort_order", request.getOrDefault("sortOrder", 0)));
        group.put("is_active", request.getOrDefault("is_active", request.getOrDefault("isActive", true)));
        group.put("created_at", new Date().toString());
        group.put("updated_at", new Date().toString());
        
        // 处理管理员信息（admin_info）
        Map<String, Object> adminInfo = null;
        if (request.containsKey("admin_info")) {
            adminInfo = (Map<String, Object>) request.get("admin_info");
        } else if (request.containsKey("adminInfo")) {
            adminInfo = (Map<String, Object>) request.get("adminInfo");
        }
        
        TeamAdminAccount admin = null;
        // 如果提供了管理员信息，同时创建管理员账号
        if (adminInfo != null && !adminInfo.isEmpty()) {
            String loginId = (String) (adminInfo.get("username") != null ? adminInfo.get("username") : adminInfo.get("login_id"));
            if (loginId == null || loginId.isEmpty()) {
                return ResponseData.error(400, "管理员登录ID不能为空");
            }
            
            // 检查登录ID是否已存在
            if (teamAdminAccountService.existsByLoginId(loginId, null)) {
                return ResponseData.error(400, "管理员登录ID已存在：" + loginId);
            }
            
            String password = (String) adminInfo.get("password");
            if (password == null || password.isEmpty()) {
                return ResponseData.error(400, "管理员密码不能为空");
            }
            
            // 验证密码确认
            String confirmPassword = (String) adminInfo.get("confirm_password");
            if (confirmPassword == null || confirmPassword.isEmpty()) {
                return ResponseData.error(400, "确认密码不能为空");
            }
            if (!password.equals(confirmPassword)) {
                return ResponseData.error(400, "密码和确认密码不一致");
            }
            
            // 创建管理员实体
            admin = new TeamAdminAccount();
            admin.setTenantId(tenantId);
            admin.setAgencyId(agencyId);
            admin.setTeamGroupId(groupId);
            admin.setAccountCode("GROUP_ADMIN_" + groupId);
            admin.setAccountName((String) (adminInfo.get("name") != null ? adminInfo.get("name") : adminInfo.get("account_name")));
            admin.setLoginId(loginId);
            admin.setPasswordHash(passwordEncoder.encode(password)); // BCrypt加密
            admin.setRole("spv");  // 小组群长角色
            admin.setEmail((String) adminInfo.get("email"));
            admin.setIsActive(true);
            
            // 保存管理员
            teamAdminAccountService.save(admin);
            log.info("========== 小组群管理员创建成功，adminId={} ==========", admin.getId());
            
            // 将管理员ID添加到group
            group.put("admin_id", admin.getId());
        }
        
        // 构建响应
        if (admin != null) {
            Map<String, Object> adminMap = new HashMap<>();
            adminMap.put("id", admin.getId());
            adminMap.put("tenant_id", admin.getTenantId());
            adminMap.put("agency_id", admin.getAgencyId());
            adminMap.put("team_group_id", admin.getTeamGroupId());
            adminMap.put("account_code", admin.getAccountCode());
            adminMap.put("account_name", admin.getAccountName());
            adminMap.put("login_id", admin.getLoginId());
            adminMap.put("role", admin.getRole());
            adminMap.put("email", admin.getEmail());
            adminMap.put("is_active", admin.getIsActive());
            group.put("admin", adminMap);
        }
        
        return ResponseData.success(group);
    }
    
    /**
     * 辅助方法：将Object转换为Long
     */
    private Long getLongValue(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) return Long.parseLong((String) value);
        return null;
    }

    /**
     * 更新小组群
     */
    @PutMapping("/{id}")
    public ResponseData<Map<String, Object>> updateTeamGroup(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("========== 更新小组群，id={}, request={} ==========", id, request);
        
        Map<String, Object> group = new HashMap<>();
        group.put("id", id);
        group.put("tenant_id", request.get("tenant_id") != null ? request.get("tenant_id") : request.get("tenantId"));
        group.put("agency_id", request.get("agency_id") != null ? request.get("agency_id") : request.get("agencyId"));
        group.put("group_code", request.get("group_code") != null ? request.get("group_code") : request.getOrDefault("groupCode", "GROUP" + String.format("%03d", id)));
        group.put("group_name", request.get("group_name") != null ? request.get("group_name") : request.getOrDefault("groupName", "测试小组群" + id));
        group.put("group_name_en", request.get("group_name_en") != null ? request.get("group_name_en") : request.getOrDefault("groupNameEn", "Test Team Group " + id));
        group.put("description", request.get("description"));
        group.put("sort_order", request.getOrDefault("sort_order", request.getOrDefault("sortOrder", id.intValue())));
        group.put("is_active", request.getOrDefault("is_active", request.getOrDefault("isActive", true)));
        group.put("updated_at", new Date().toString());
        
        return ResponseData.success(group);
    }

    /**
     * 删除小组群
     */
    @DeleteMapping("/{id}")
    public ResponseData<String> deleteTeamGroup(@PathVariable Long id) {
        log.info("========== 删除小组群，id={} ==========", id);
        return ResponseData.success("删除成功");
    }
}





















