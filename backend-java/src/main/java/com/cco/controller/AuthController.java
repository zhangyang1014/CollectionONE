package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.dto.request.LoginRequest;
import com.cco.model.dto.response.LoginResponse;
import com.cco.security.JwtTokenProvider;
import com.cco.security.UserDetailsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证Controller - 处理登录、登出等认证相关接口
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/admin/auth")
public class AuthController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * 管理后台登录
     */
    @PostMapping("/login")
    public ResponseData<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("========== 管理后台登录请求，loginId={} ==========", request.getLoginId());
        
        try {
            // 验证用户名和密码
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getLoginId());
            
            // 验证密码（使用BCrypt）
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder = 
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            
            if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
                log.warn("密码验证失败，loginId={}", request.getLoginId());
                return ResponseData.error(401, "登录ID或密码错误");
            }
            
            // 生成JWT Token
            String token = jwtTokenProvider.generateToken(userDetails.getUsername());
            
            // 构建用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", 1);
            userInfo.put("loginId", request.getLoginId());
            userInfo.put("username", request.getLoginId());
            
            // 根据用户名设置角色
            String role = "SuperAdmin";
            String name = "超级管理员";
            if ("tenantadmin".equalsIgnoreCase(request.getLoginId())) {
                role = "TenantAdmin";
                name = "甲方管理员";
            }
            
            userInfo.put("role", role);
            userInfo.put("email", request.getLoginId() + "@cco.com");
            userInfo.put("name", name);
            
            // 构建响应
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(token);
            loginResponse.setUser(userInfo);
            
            log.info("========== 登录成功，loginId={}, role={} ==========", request.getLoginId(), role);
            return ResponseData.success(loginResponse);
            
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            log.warn("用户不存在，loginId={}", request.getLoginId());
            return ResponseData.error(401, "登录ID或密码错误");
        } catch (Exception e) {
            log.error("登录失败", e);
            e.printStackTrace();
            return ResponseData.error(500, "登录失败: " + e.getMessage());
        }
    }

    /**
     * 管理后台登出
     */
    @PostMapping("/logout")
    public ResponseData<String> logout() {
        log.info("========== 管理后台登出请求 ==========");
        // JWT是无状态的，登出主要是前端删除token
        return ResponseData.success("登出成功");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseData<Map<String, Object>> getCurrentUser() {
        log.info("========== 获取当前用户信息 ==========");
        
        // 从SecurityContext获取当前用户
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseData.error(401, "未登录");
        }
        
        String username = authentication.getName();
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1);
        userInfo.put("loginId", username);
        userInfo.put("username", username);
        
        // 根据用户名设置角色
        String role = "SuperAdmin";
        String name = "超级管理员";
        if ("tenantadmin".equalsIgnoreCase(username)) {
            role = "TenantAdmin";
            name = "甲方管理员";
        }
        
        userInfo.put("role", role);
        userInfo.put("email", username + "@cco.com");
        userInfo.put("name", name);
        
        return ResponseData.success(userInfo);
    }
}

