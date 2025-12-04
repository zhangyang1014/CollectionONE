package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.entity.TeamAdminAccount;
import com.cco.service.TeamAdminAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小组管理员管理Controller - Mock实现
 * 根据PRD要求，提供独立的 /api/v1/team-admins 接口
 * 
 * @author CCO Team
 * @since 2025-01-11
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/team-admins")
public class TeamAdminController {
    
    @Autowired
    private TeamAdminAccountService teamAdminAccountService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取小组管理员列表
     * 根据PRD要求：GET /api/v1/team-admins
     */
    @GetMapping
    public ResponseData<List<TeamAdminAccount>> getTeamAdmins(
            @RequestParam(required = false) Long tenant_id,
            @RequestParam(required = false) Long agency_id,
            @RequestParam(required = false) Long team_id,
            @RequestParam(required = false) Boolean is_active,
            @RequestParam(required = false, defaultValue = "0") Integer skip,
            @RequestParam(required = false, defaultValue = "100") Integer limit) {
        log.info("========== 获取小组管理员列表，tenant_id={}, agency_id={}, team_id={}, is_active={} ==========", 
                tenant_id, agency_id, team_id, is_active);
        
        List<TeamAdminAccount> admins = teamAdminAccountService.listByConditions(tenant_id, agency_id, team_id, is_active);
        
        // 分页处理
        int start = skip != null ? skip : 0;
        int end = Math.min(start + (limit != null ? limit : 100), admins.size());
        List<TeamAdminAccount> pagedAdmins = admins.subList(Math.max(0, start), end);
        
        log.info("========== 返回小组管理员列表，数量={} ==========", pagedAdmins.size());
        return ResponseData.success(pagedAdmins);
    }

    /**
     * 创建小组管理员
     * 根据PRD要求：POST /api/v1/team-admins
     */
    @PostMapping
    public ResponseData<TeamAdminAccount> createTeamAdmin(@RequestBody Map<String, Object> request) {
        log.info("========== 创建小组管理员，request={} ==========", request);
        
        String loginId = (String) (request.get("login_id") != null ? request.get("login_id") : request.get("username"));
        if (loginId == null || loginId.isEmpty()) {
            return ResponseData.error(400, "登录ID不能为空");
        }
        
        // 检查登录ID是否已存在
        if (teamAdminAccountService.existsByLoginId(loginId, null)) {
            return ResponseData.error(400, "登录ID已存在：" + loginId);
        }
        
        String password = (String) request.get("password");
        if (password == null || password.isEmpty()) {
            return ResponseData.error(400, "密码不能为空");
        }
        
        // 创建管理员实体
        TeamAdminAccount admin = new TeamAdminAccount();
        admin.setTenantId(getLongValue(request, "tenant_id"));
        admin.setAgencyId(getLongValue(request, "agency_id"));
        admin.setTeamGroupId(getLongValue(request, "team_group_id"));
        admin.setTeamId(getLongValue(request, "team_id"));
        admin.setAccountCode((String) (request.get("account_code") != null ? request.get("account_code") : "ADMIN" + System.currentTimeMillis()));
        admin.setAccountName((String) (request.get("account_name") != null ? request.get("account_name") : request.get("name")));
        admin.setLoginId(loginId);
        admin.setPasswordHash(passwordEncoder.encode(password)); // BCrypt加密
        admin.setEmail((String) request.get("email"));
        admin.setRole((String) (request.get("role") != null ? request.get("role") : "team_leader"));
        admin.setRemark((String) request.get("remark"));
        admin.setIsActive((Boolean) request.getOrDefault("is_active", true));
        
        teamAdminAccountService.save(admin);
        log.info("========== 创建小组管理员成功，adminId={} ==========", admin.getId());
        
        return ResponseData.success(admin);
    }
    
    /**
     * 辅助方法：从Map中获取Long值
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取小组管理员详情
     * 根据PRD要求：GET /api/v1/team-admins/{admin_id}
     */
    @GetMapping("/{admin_id}")
    public ResponseData<TeamAdminAccount> getTeamAdmin(@PathVariable Long admin_id) {
        log.info("========== 获取小组管理员详情，admin_id={} ==========", admin_id);
        
        TeamAdminAccount admin = teamAdminAccountService.getById(admin_id);
        if (admin == null) {
            return ResponseData.error(404, "小组管理员不存在");
        }
        
        return ResponseData.success(admin);
    }

    /**
     * 更新小组管理员
     * 根据PRD要求：PUT /api/v1/team-admins/{admin_id}
     */
    @PutMapping("/{admin_id}")
    public ResponseData<TeamAdminAccount> updateTeamAdmin(
            @PathVariable Long admin_id,
            @RequestBody Map<String, Object> request) {
        log.info("========== 更新小组管理员，admin_id={}, request={} ==========", admin_id, request);
        
        TeamAdminAccount admin = teamAdminAccountService.getById(admin_id);
        if (admin == null) {
            return ResponseData.error(404, "小组管理员不存在");
        }
        
        // 更新字段
        if (request.containsKey("account_name") || request.containsKey("name")) {
            admin.setAccountName((String) (request.get("account_name") != null ? request.get("account_name") : request.get("name")));
        }
        if (request.containsKey("login_id") || request.containsKey("username")) {
            String newLoginId = (String) (request.get("login_id") != null ? request.get("login_id") : request.get("username"));
            if (newLoginId != null && !newLoginId.equals(admin.getLoginId())) {
                // 检查新登录ID是否已存在
                if (teamAdminAccountService.existsByLoginId(newLoginId, admin_id)) {
                    return ResponseData.error(400, "登录ID已存在：" + newLoginId);
                }
                admin.setLoginId(newLoginId);
            }
        }
        if (request.containsKey("email")) {
            admin.setEmail((String) request.get("email"));
        }
        if (request.containsKey("role")) {
            admin.setRole((String) request.get("role"));
        }
        if (request.containsKey("team_id")) {
            admin.setTeamId(getLongValue(request, "team_id"));
        }
        if (request.containsKey("team_group_id")) {
            admin.setTeamGroupId(getLongValue(request, "team_group_id"));
        }
        if (request.containsKey("remark")) {
            admin.setRemark((String) request.get("remark"));
        }
        if (request.containsKey("is_active") || request.containsKey("isActive")) {
            admin.setIsActive((Boolean) request.getOrDefault("is_active", request.get("isActive")));
        }
        
        teamAdminAccountService.updateById(admin);
        log.info("========== 更新小组管理员成功，adminId={} ==========", admin_id);
        
        return ResponseData.success(admin);
    }

    /**
     * 重置小组管理员密码
     * 根据PRD要求：PUT /api/v1/team-admins/{admin_id}/password
     */
    @PutMapping("/{admin_id}/password")
    public ResponseData<Map<String, Object>> resetTeamAdminPassword(
            @PathVariable Long admin_id,
            @RequestBody Map<String, Object> request) {
        log.info("========== 重置小组管理员密码，admin_id={} ==========", admin_id);
        
        TeamAdminAccount admin = teamAdminAccountService.getById(admin_id);
        if (admin == null) {
            return ResponseData.error(404, "小组管理员不存在");
        }
        
        String newPassword = (String) request.get("new_password");
        if (newPassword == null || newPassword.isEmpty()) {
            return ResponseData.error(400, "新密码不能为空");
        }
        
        // 更新密码
        admin.setPasswordHash(passwordEncoder.encode(newPassword)); // BCrypt加密
        teamAdminAccountService.updateById(admin);
        
        Map<String, Object> result = new HashMap<>();
        result.put("admin_id", admin_id);
        result.put("message", "密码重置成功");
        
        log.info("========== 密码重置成功，adminId={} ==========", admin_id);
        return ResponseData.success(result);
    }

    /**
     * 启用/禁用小组管理员
     * 根据PRD要求：PUT /api/v1/team-admins/{admin_id}/status
     */
    @PutMapping("/{admin_id}/status")
    public ResponseData<Map<String, Object>> updateTeamAdminStatus(
            @PathVariable Long admin_id,
            @RequestBody Map<String, Object> request) {
        log.info("========== 更新小组管理员状态，admin_id={}, is_active={} ==========", 
                admin_id, request.get("is_active"));
        
        TeamAdminAccount admin = teamAdminAccountService.getById(admin_id);
        if (admin == null) {
            return ResponseData.error(404, "小组管理员不存在");
        }
        
        Boolean isActive = (Boolean) request.get("is_active");
        if (isActive == null) {
            return ResponseData.error(400, "is_active参数不能为空");
        }
        
        admin.setIsActive(isActive);
        teamAdminAccountService.updateById(admin);
        
        Map<String, Object> result = new HashMap<>();
        result.put("admin_id", admin_id);
        result.put("is_active", isActive);
        result.put("message", "状态更新成功");
        
        log.info("========== 状态更新成功，adminId={}, isActive={} ==========", admin_id, isActive);
        return ResponseData.success(result);
    }
    
    /**
     * 小组管理员不支持删除操作，只能通过启用/禁用来管理
     * 如需禁用小组管理员，请使用状态更新接口
     */
    // 删除接口已移除，小组管理员不支持删除
}

