package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.dto.request.LoginRequest;
import com.cco.model.dto.response.LoginResponse;
import com.cco.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台认证Controller
 * 对应Python: app/api/auth.py
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/admin/auth")
public class AuthController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 管理后台登录
     * 对应Python: @router.post("/login")
     */
    @PostMapping("/login")
    public ResponseData<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("管理后台登录请求: loginId={}", request.getLoginId());

        String loginId = request.getLoginId();
        String password = request.getPassword();

        // TODO: 从数据库验证用户
        // 临时实现：硬编码验证
        Map<String, Object> userInfo = null;

        if ("superadmin".equalsIgnoreCase(loginId) && "123456".equals(password)) {
            // SuperAdmin登录
            userInfo = new HashMap<>();
            userInfo.put("id", 1);
            userInfo.put("loginId", "superadmin");
            userInfo.put("username", "superadmin");
            userInfo.put("role", Constants.Role.SUPER_ADMIN);
            userInfo.put("email", "admin@cco.com");
            userInfo.put("name", "超级管理员");
        } else if ("tenantadmin".equalsIgnoreCase(loginId) && "admin123".equals(password)) {
            // TenantAdmin登录
            userInfo = new HashMap<>();
            userInfo.put("id", 2);
            userInfo.put("loginId", "tenantadmin");
            userInfo.put("username", "tenantadmin");
            userInfo.put("role", Constants.Role.TENANT_ADMIN);
            userInfo.put("email", "tenant@cco.com");
            userInfo.put("name", "甲方管理员");
        }

        if (userInfo == null) {
            return ResponseData.error(401, "登录ID或密码错误");
        }

        // 生成JWT Token
        String token = jwtTokenProvider.generateToken(loginId);

        LoginResponse response = new LoginResponse(token, userInfo);
        return ResponseData.success("登录成功", response);
    }

    /**
     * 管理后台登出
     * 对应Python: @router.post("/logout")
     */
    @PostMapping("/logout")
    public ResponseData<Void> logout() {
        log.info("管理后台登出请求");
        // JWT是无状态的，登出只需要客户端删除token
        return ResponseData.success("登出成功", null);
    }

    /**
     * 获取当前用户信息
     * 对应Python: @router.get("/me")
     */
    @GetMapping("/me")
    public ResponseData<Map<String, Object>> getCurrentUser() {
        // TODO: 从SecurityContext获取当前用户信息
        log.info("获取当前用户信息");
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1);
        userInfo.put("loginId", "superadmin");
        userInfo.put("username", "superadmin");
        userInfo.put("role", Constants.Role.SUPER_ADMIN);
        return ResponseData.success("获取成功", userInfo);
    }

}

