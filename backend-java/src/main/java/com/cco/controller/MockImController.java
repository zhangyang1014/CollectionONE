package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Mock IM（即时通讯）控制器
 * 提供IM相关API的Mock数据，包括人脸识别等功能
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/im")
public class MockImController {

    /**
     * Mock人脸检测API
     * 前端上传照片，后端返回人脸ID
     */
    @PostMapping("/face/detect")
    public ResponseData<Map<String, Object>> detectFace(@RequestBody Map<String, Object> request) {
        log.info("人脸检测Mock API被调用");
        
        // 模拟人脸检测成功
        Map<String, Object> result = new HashMap<>();
        result.put("face_id", "MOCK_FACE_" + System.currentTimeMillis());
        result.put("confidence", 0.98);
        result.put("message", "人脸识别成功（Mock）");
        
        return ResponseData.success(result);
    }

    /**
     * Mock人脸验证API
     * 验证人脸ID是否有效
     */
    @PostMapping("/face/verify")
    public ResponseData<Map<String, Object>> verifyFace(@RequestBody Map<String, Object> request) {
        log.info("人脸验证Mock API被调用");
        
        Map<String, Object> result = new HashMap<>();
        result.put("verified", true);
        result.put("confidence", 0.96);
        result.put("message", "人脸验证成功（Mock）");
        
        return ResponseData.success(result);
    }

    /**
     * Mock IM登录API
     */
    @PostMapping("/auth/login")
    public ResponseData<Map<String, Object>> imLogin(@RequestBody Map<String, Object> request) {
        log.info("IM登录Mock API被调用，请求参数: {}", request);
        
        // 模拟登录成功
        Map<String, Object> result = new HashMap<>();
        
        // 生成Mock Token
        String token = "MOCK_IM_TOKEN_" + System.currentTimeMillis();
        result.put("token", token);
        
        // 模拟用户信息
        Map<String, Object> user = new HashMap<>();
        String collectorIdStr = (String) request.get("collectorId");
        
        // 将字符串格式的collectorId转换为数字ID（用于API调用）
        // 特殊映射：BT0001 -> 43 (对应数据库中的实际ID)
        Long numericCollectorId = 1L; // 默认值
        if (collectorIdStr != null) {
            // 特殊账号映射（匹配数据库中的实际ID）
            if ("BT0001".equals(collectorIdStr)) {
                numericCollectorId = 43L;
                log.info("催员ID [{}] 映射到数据库ID: {}", collectorIdStr, numericCollectorId);
            } else if (collectorIdStr.startsWith("BTQ")) {
                numericCollectorId = 1L;
            } else if (collectorIdStr.startsWith("BTSK")) {
                numericCollectorId = 2L;
            } else {
                // 尝试从催员ID中提取数字部分
                String numericPart = collectorIdStr.replaceAll("[^0-9]", "");
                if (!numericPart.isEmpty()) {
                    try {
                        numericCollectorId = Long.parseLong(numericPart);
                        log.info("从催员ID [{}] 提取到数字ID: {}", collectorIdStr, numericCollectorId);
                    } catch (NumberFormatException e) {
                        log.warn("无法从催员ID [{}] 提取数字，使用hashCode", collectorIdStr);
                        numericCollectorId = (long) Math.abs(collectorIdStr.hashCode() % 100);
                    }
                } else {
                    numericCollectorId = (long) Math.abs(collectorIdStr.hashCode() % 100);
                }
            }
        }
        
        // 获取tenantId并转换为整数
        Object tenantIdObj = request.get("tenantId");
        Integer tenantId = null;
        if (tenantIdObj instanceof String) {
            try {
                tenantId = Integer.parseInt((String) tenantIdObj);
            } catch (NumberFormatException e) {
                log.warn("无法解析tenantId: {}", tenantIdObj);
            }
        } else if (tenantIdObj instanceof Number) {
            tenantId = ((Number) tenantIdObj).intValue();
        }
        
        // 根据tenantId设置时区信息
        // tenantId=1 (BTQ) -> 墨西哥时区
        // tenantId=2 (BTSK) -> 印度时区
        String agencyTimezone = "America/Mexico_City"; // 机构时区（催员所在机构）
        String agencyTimezoneShort = "CST";
        Integer agencyTimezoneOffset = -6; // UTC-6
        
        String tenantTimezone = "America/Mexico_City"; // 甲方时区（客户所在时区）
        String tenantTimezoneShort = "CST";
        Integer tenantTimezoneOffset = -6; // UTC-6
        
        if (tenantId != null) {
            if (tenantId == 1) {
                // BTQ（墨西哥）
                agencyTimezone = "America/Mexico_City";
                agencyTimezoneShort = "CST";
                agencyTimezoneOffset = -6;
                tenantTimezone = "America/Mexico_City";
                tenantTimezoneShort = "CST";
                tenantTimezoneOffset = -6;
            } else if (tenantId == 2) {
                // BTSK（印度）
                agencyTimezone = "Asia/Kolkata";
                agencyTimezoneShort = "IST";
                agencyTimezoneOffset = 5; // UTC+5:30，这里用5表示
                tenantTimezone = "Asia/Kolkata";
                tenantTimezoneShort = "IST";
                tenantTimezoneOffset = 5;
            }
        }
        
        user.put("id", collectorIdStr); // 保持原始字符串ID
        user.put("tenantId", request.get("tenantId"));
        user.put("collectorId", collectorIdStr); // 保持原始字符串ID
        user.put("collectorIdNumeric", numericCollectorId); // 添加数字格式的ID
        user.put("username", "催员" + collectorIdStr);
        user.put("role", "collector");
        user.put("whatsappConnected", true);
        
        // 添加时区信息
        user.put("agencyTimezone", agencyTimezone); // 机构时区（IANA时区名称）
        user.put("agencyTimezoneShort", agencyTimezoneShort); // 机构时区缩写
        user.put("agencyTimezoneOffset", agencyTimezoneOffset); // 机构时区UTC偏移量
        user.put("tenantTimezone", tenantTimezone); // 甲方时区（IANA时区名称）
        user.put("tenantTimezoneShort", tenantTimezoneShort); // 甲方时区缩写
        user.put("tenantTimezoneOffset", tenantTimezoneOffset); // 甲方时区UTC偏移量
        
        result.put("user", user);
        result.put("message", "登录成功（Mock）");
        
        return ResponseData.success(result);
    }

    /**
     * Mock IM登出API
     */
    @PostMapping("/auth/logout")
    public ResponseData<Map<String, Object>> imLogout() {
        log.info("IM登出Mock API被调用");
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "登出成功（Mock）");
        
        return ResponseData.success(result);
    }

    /**
     * Mock刷新Token API
     */
    @PostMapping("/auth/refresh-token")
    public ResponseData<Map<String, Object>> refreshToken() {
        log.info("IM刷新Token Mock API被调用");
        
        Map<String, Object> result = new HashMap<>();
        String newToken = "MOCK_IM_TOKEN_REFRESHED_" + System.currentTimeMillis();
        result.put("token", newToken);
        result.put("message", "Token刷新成功（Mock）");
        
        return ResponseData.success(result);
    }

    /**
     * Mock获取IM用户信息API
     */
    @GetMapping("/user/info")
    public ResponseData<Map<String, Object>> getImUserInfo() {
        log.info("获取IM用户信息Mock API被调用");
        
        Map<String, Object> user = new HashMap<>();
        user.put("id", 37L);
        user.put("tenantId", 1L);
        user.put("collectorId", 37L);
        user.put("username", "催员37");
        user.put("role", "collector");
        user.put("whatsappConnected", true);
        user.put("phone", "13800138037");
        
        return ResponseData.success(user);
    }

    /**
     * Mock检查会话状态API
     */
    @GetMapping("/session/check")
    public ResponseData<Map<String, Object>> checkSession() {
        log.info("检查会话状态Mock API被调用");
        
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("message", "会话有效（Mock）");
        
        return ResponseData.success(result);
    }
}


