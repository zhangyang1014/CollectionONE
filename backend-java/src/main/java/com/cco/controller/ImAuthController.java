package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.dto.request.ImLoginRequest;
import com.cco.model.dto.response.LoginResponse;
import com.cco.security.JwtTokenProvider;
import com.cco.service.CollectorLoginWhitelistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * IM端认证Controller - Mock实现
 * 处理IM端催员登录、登出等认证相关接口
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/im/auth")
public class ImAuthController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private CollectorLoginWhitelistService whitelistService;

    /**
     * IM端催员登录
     */
    @PostMapping("/login")
    public ResponseData<LoginResponse> login(@RequestBody ImLoginRequest request, HttpServletRequest httpRequest) {
        log.info("========== IM端登录请求，collectorId={} ==========", 
                request.getCollectorId());
        
        try {
            String collectorId = request.getCollectorId();
            String password = request.getPassword();
            
            // Mock数据：验证催员信息
            // 这里使用简单的Mock验证，实际应该查询数据库
            // 根据催员ID（login_id）查询催员，因为login_id是唯一的
            Map<String, Object> mockCollector = getMockCollector(collectorId);
            
            if (mockCollector == null) {
                log.warn("催员不存在，collectorId={}", collectorId);
                return ResponseData.error(401, "催员不存在或已被禁用");
            }
            
            // 验证密码（Mock模式：密码为123456）
            if (!"123456".equals(password)) {
                log.warn("密码验证失败，collectorId={}", collectorId);
                return ResponseData.error(401, "密码错误");
            }
            
            // 获取客户端IP地址
            String clientIp = getClientIpAddress(httpRequest);
            log.info("客户端IP地址：{}", clientIp);
            
            // 获取催员的甲方ID（从Mock数据中获取）
            Long tenantId = null;
            try {
                Object tenantIdObj = mockCollector.get("tenantId");
                if (tenantIdObj instanceof String) {
                    tenantId = Long.parseLong((String) tenantIdObj);
                } else if (tenantIdObj instanceof Number) {
                    tenantId = ((Number) tenantIdObj).longValue();
                }
            } catch (Exception e) {
                log.warn("无法解析tenantId，跳过IP白名单检查", e);
            }
            
            // 检查IP白名单（如果tenantId有效）
            if (tenantId != null) {
                Boolean isIpAllowed = whitelistService.isIpAllowed(tenantId, clientIp);
                if (!isIpAllowed) {
                    log.warn("IP地址不在白名单中，拒绝登录，tenantId={}, ipAddress={}", tenantId, clientIp);
                    return ResponseData.error(403, "Access denied: Login from this IP address is not allowed.");
                }
            }
            
            // 生成JWT Token
            String token = jwtTokenProvider.generateToken(collectorId);
            
            // 构建用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", mockCollector.get("id"));
            // ✅ 使用collectorCode作为collectorId，这样前端会显示真实的催员ID（如：BTQ001）
            userInfo.put("collectorId", mockCollector.get("collectorCode") != null ? mockCollector.get("collectorCode") : collectorId);
            userInfo.put("collectorCode", mockCollector.get("collectorCode"));
            userInfo.put("collectorName", mockCollector.get("collectorName"));
            userInfo.put("tenantId", mockCollector.get("tenantId"));
            userInfo.put("tenantName", mockCollector.get("tenantName"));
            userInfo.put("agencyId", mockCollector.get("agencyId"));
            userInfo.put("agencyName", mockCollector.get("agencyName"));
            userInfo.put("teamId", mockCollector.get("teamId"));
            userInfo.put("teamName", mockCollector.get("teamName"));
            userInfo.put("mobile", mockCollector.get("mobile"));
            userInfo.put("email", mockCollector.get("email"));
            userInfo.put("collectorLevel", mockCollector.get("collectorLevel"));
            userInfo.put("status", "active");
            userInfo.put("currentCaseCount", mockCollector.get("currentCaseCount"));
            userInfo.put("maxCaseCount", mockCollector.get("maxCaseCount"));
            userInfo.put("role", "collector");
            userInfo.put("permissions", Arrays.asList("case:view", "case:call", "message:send"));
            
            // 构建响应
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(token);
            loginResponse.setUser(userInfo);
            
            log.info("========== IM端登录成功，collectorId={}, collectorName={} ==========", 
                    collectorId, mockCollector.get("collectorName"));
            return ResponseData.success(loginResponse);
            
        } catch (Exception e) {
            log.error("IM端登录失败", e);
            e.printStackTrace();
            return ResponseData.error(500, "登录失败: " + e.getMessage());
        }
    }

    /**
     * IM端登出
     */
    @PostMapping("/logout")
    public ResponseData<String> logout() {
        log.info("========== IM端登出请求 ==========");
        // JWT是无状态的，登出主要是前端删除token
        return ResponseData.success("登出成功");
    }

    /**
     * 获取当前催员信息
     */
    @GetMapping("/user-info")
    public ResponseData<Map<String, Object>> getUserInfo() {
        log.info("========== 获取IM端当前用户信息 ==========");
        
        // Mock数据：返回默认催员信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1);
        userInfo.put("collectorId", "BTQ001");
        userInfo.put("collectorName", "Carlos Méndez");
        userInfo.put("tenantId", "1");
        userInfo.put("tenantName", "百腾企业");
        userInfo.put("role", "collector");
        userInfo.put("permissions", Arrays.asList("case:view", "case:call", "message:send"));
        
        return ResponseData.success(userInfo);
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh-token")
    public ResponseData<Map<String, Object>> refreshToken() {
        log.info("========== IM端刷新Token请求 ==========");
        
        // 生成新的Token
        String newToken = jwtTokenProvider.generateToken("BTQ001");
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", newToken);
        result.put("expires_in", 1800); // 30分钟
        
        return ResponseData.success(result);
    }

    /**
     * 检查会话状态
     */
    @GetMapping("/check-session")
    public ResponseData<Map<String, Object>> checkSession() {
        log.info("========== IM端检查会话状态 ==========");
        
        Map<String, Object> result = new HashMap<>();
        result.put("is_valid", true);
        result.put("expires_at", System.currentTimeMillis() + 1800000); // 30分钟后
        
        return ResponseData.success(result);
    }

    /**
     * 获取Mock催员数据
     * 根据collectorId（login_id）返回对应的催员信息
     * 注意：login_id在数据库中是唯一的，所以只需要根据collectorId查询即可
     */
    private Map<String, Object> getMockCollector(String collectorId) {
        // Mock数据：模拟催员信息
        // 实际应该从数据库查询：SELECT * FROM collectors WHERE login_id = collectorId
        // 因为login_id有唯一约束，所以查询结果最多只有一条
        
        // 示例催员数据
        Map<String, Object> collector = new HashMap<>();
        
        // 根据collectorId返回不同的Mock数据
        if ("BTQ001".equals(collectorId)) {
            collector.put("id", 1);
            collector.put("collectorCode", "BTQ001");
            collector.put("collectorName", "Carlos Méndez");
            collector.put("tenantId", "1");
            collector.put("tenantName", "百腾企业");
            collector.put("agencyId", 1);
            collector.put("agencyName", "百腾企业-机构1");
            collector.put("teamId", 1);
            collector.put("teamName", "百腾企业-机构1-小组1");
            collector.put("mobile", "+52 55 1234 5001");
            collector.put("email", "carlos.mendez@btq.mx");
            collector.put("collectorLevel", "高级催员");
            collector.put("currentCaseCount", 45);
            collector.put("maxCaseCount", 100);
            return collector;
        }
        
        if ("BTSK001".equals(collectorId)) {
            collector.put("id", 2);
            collector.put("collectorCode", "BTSK001");
            collector.put("collectorName", "BTSK催员1");
            collector.put("tenantId", "2");
            collector.put("tenantName", "BTSK机构");
            collector.put("agencyId", 2);
            collector.put("agencyName", "BTSK机构-机构1");
            collector.put("teamId", 2);
            collector.put("teamName", "BTSK机构-机构1-小组1");
            collector.put("mobile", "+86 138 0000 0002");
            collector.put("email", collectorId + "@test.com");
            collector.put("collectorLevel", "普通催员");
            collector.put("currentCaseCount", 0);
            collector.put("maxCaseCount", 100);
            return collector;
        }
        
        // 默认催员数据
        collector.put("id", 1);
        collector.put("collectorCode", collectorId);
        collector.put("collectorName", "催员" + collectorId);
        collector.put("tenantId", "1");
        collector.put("tenantName", "测试机构");
        collector.put("agencyId", 1);
        collector.put("agencyName", "测试机构-机构1");
        collector.put("teamId", 1);
        collector.put("teamName", "测试机构-机构1-小组1");
        collector.put("mobile", "+86 138 0000 0000");
        collector.put("email", collectorId + "@test.com");
        collector.put("collectorLevel", "普通催员");
        collector.put("currentCaseCount", 0);
        collector.put("maxCaseCount", 100);
        
        return collector;
    }
    
    /**
     * 获取客户端真实IP地址
     * 考虑代理服务器的情况（X-Forwarded-For, X-Real-IP等）
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 如果IP包含多个地址（通过代理），取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        // 处理IPv6的本地地址
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "127.0.0.1";
        }
        
        return ip;
    }
}
















