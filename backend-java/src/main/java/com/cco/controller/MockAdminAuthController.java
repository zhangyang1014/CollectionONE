package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.security.JwtTokenProvider;
import com.cco.security.UserDetailsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock 管理端认证控制器
 * 提供管理后台登录、登出等API的Mock实现
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/admin/auth")
public class MockAdminAuthController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 管理后台登录
     */
    @PostMapping("/login")
    public ResponseData<Map<String, Object>> adminLogin(@RequestBody Map<String, Object> request) {
        log.info("管理后台登录API被调用，请求参数: {}", request);
        
        try {
            String loginId = (String) request.get("loginId");
            String password = (String) request.get("password");
            
            if (loginId == null || loginId.trim().isEmpty()) {
                return ResponseData.error(400, "登录ID不能为空");
            }
            
            if (password == null || password.trim().isEmpty()) {
                return ResponseData.error(400, "密码不能为空");
            }
            
            // 验证用户名和密码
            UserDetails userDetails;
            try {
                userDetails = userDetailsService.loadUserByUsername(loginId);
            } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
                log.warn("用户不存在: {}", loginId);
                return ResponseData.unauthorized("登录ID或密码错误");
            }
            
            // 验证密码
            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                log.warn("密码错误: {}", loginId);
                return ResponseData.unauthorized("登录ID或密码错误");
            }
            
            // 生成JWT Token
            String token = jwtTokenProvider.generateToken(loginId);
            
            // 构建用户信息
            Map<String, Object> user = new HashMap<>();
            user.put("id", loginId.equalsIgnoreCase("superadmin") ? 1L : 2L);
            user.put("loginId", loginId);
            user.put("username", loginId);
            
            // 从权限中提取角色
            String role = Constants.Role.SUPER_ADMIN;
            if (loginId.equalsIgnoreCase("tenantadmin")) {
                role = Constants.Role.TENANT_ADMIN;
            }
            user.put("role", role);
            
            if (loginId.equalsIgnoreCase("superadmin")) {
                user.put("email", "admin@cco.com");
                user.put("name", "超级管理员");
            } else if (loginId.equalsIgnoreCase("tenantadmin")) {
                user.put("email", "tenant@cco.com");
                user.put("name", "甲方管理员");
            }
            
            // 构建响应数据
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", user);
            
            log.info("管理后台登录成功: {}", loginId);
            return ResponseData.success("登录成功", result);
            
        } catch (Exception e) {
            log.error("管理后台登录失败", e);
            return ResponseData.error(500, "登录失败: " + e.getMessage());
        }
    }

    /**
     * 管理后台登出
     */
    @PostMapping("/logout")
    public ResponseData<Map<String, Object>> adminLogout() {
        log.info("管理后台登出API被调用");
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "登出成功");
        
        return ResponseData.success(result);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseData<Map<String, Object>> getAdminUserInfo() {
        log.info("获取管理后台用户信息API被调用");
        
        // 从SecurityContext获取当前用户
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseData.unauthorized("未登录");
        }
        
        String loginId = authentication.getName();
        
        Map<String, Object> user = new HashMap<>();
        user.put("id", loginId.equalsIgnoreCase("superadmin") ? 1L : 2L);
        user.put("loginId", loginId);
        user.put("username", loginId);
        
        // 从权限中提取角色
        String role = Constants.Role.SUPER_ADMIN;
        if (loginId.equalsIgnoreCase("tenantadmin")) {
            role = Constants.Role.TENANT_ADMIN;
        }
        user.put("role", role);
        
        if (loginId.equalsIgnoreCase("superadmin")) {
            user.put("email", "admin@cco.com");
            user.put("name", "超级管理员");
        } else if (loginId.equalsIgnoreCase("tenantadmin")) {
            user.put("email", "tenant@cco.com");
            user.put("name", "甲方管理员");
        }
        
        return ResponseData.success(user);
    }
}

