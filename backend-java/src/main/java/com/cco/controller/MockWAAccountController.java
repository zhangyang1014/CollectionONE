package com.cco.controller;

import com.cco.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * WhatsApp账号管理Mock控制器
 * 提供个人WA账号管理的Mock数据
 * 
 * API路径映射：
 * - 账号列表: GET  /api/v1/wa/accounts/personal
 * - 创建设备: POST /api/v1/wa/devices/create
 * - 设备状态: GET  /api/v1/wa/devices/{deviceId}/status
 * - 重新绑定: POST /api/v1/wa/devices/{deviceId}/rebind
 * - 解绑设备: POST /api/v1/wa/devices/{deviceId}/unbind
 */
@Tag(name = "WhatsApp账号管理（Mock）", description = "个人WA账号绑定、查询、管理等功能的Mock实现")
@RestController
public class MockWAAccountController {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    // Mock数据存储（实际应该存储在数据库）
    private static final Map<String, List<Map<String, Object>>> COLLECTOR_ACCOUNTS = new HashMap<>();
    
    /**
     * 获取催员的个人WA账号列表
     */
    @Operation(summary = "获取个人WA账号列表", description = "获取催员绑定的所有个人WhatsApp账号")
    @GetMapping("/api/v1/wa/accounts/personal")
    public Result<Map<String, Object>> getPersonalWAAccounts(
            @Parameter(description = "催员ID") @RequestParam String collectorId
    ) {
        // 获取或初始化该催员的账号列表
        List<Map<String, Object>> accounts = COLLECTOR_ACCOUNTS.computeIfAbsent(
            collectorId, 
            k -> new ArrayList<>()
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("accounts", accounts);
        response.put("totalCount", accounts.size());
        response.put("maxCount", 3); // 每个催员最多3个个人WA账号
        
        return Result.success(response);
    }
    
    /**
     * 创建WA设备（生成二维码）
     */
    @Operation(summary = "创建WA设备", description = "创建云设备并生成WhatsApp绑定二维码")
    @PostMapping("/api/v1/wa/devices/create")
    public Result<Map<String, Object>> createWADevice(
            @RequestBody Map<String, Object> request
    ) {
        String collectorId = (String) request.get("collectorId");
        String deviceType = (String) request.get("deviceType");
        
        // 生成设备ID
        String deviceId = "device_" + UUID.randomUUID().toString().substring(0, 8);
        
        // 生成Mock二维码数据（实际应该调用WhatsApp Business API）
        String qrCode = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=wa://device/" + deviceId;
        
        // 计算过期时间（2分钟后）
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(2);
        
        Map<String, Object> response = new HashMap<>();
        response.put("deviceId", deviceId);
        response.put("qrCode", qrCode);
        response.put("expiresAt", expiresAt.format(ISO_FORMATTER) + "Z");
        
        return Result.success(response);
    }
    
    /**
     * 查询设备绑定状态
     */
    @Operation(summary = "查询设备绑定状态", description = "查询云设备的WhatsApp绑定状态")
    @GetMapping("/api/v1/wa/devices/{deviceId}/status")
    public Result<Map<String, Object>> getDeviceStatus(
            @Parameter(description = "设备ID") @PathVariable String deviceId
    ) {
        Map<String, Object> response = new HashMap<>();
        response.put("deviceId", deviceId);
        response.put("status", "pending"); // pending, paired, unpaired, failed
        response.put("phoneNumber", null);
        response.put("pairedAt", null);
        response.put("errorMessage", null);
        
        return Result.success(response);
    }
    
    /**
     * 重新绑定WA设备
     */
    @Operation(summary = "重新绑定WA设备", description = "为掉线的个人WA账号重新生成绑定二维码")
    @PostMapping("/api/v1/wa/devices/{deviceId}/rebind")
    public Result<Map<String, Object>> rebindWADevice(
            @Parameter(description = "设备ID") @PathVariable String deviceId
    ) {
        // 生成新的二维码
        String qrCode = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=wa://rebind/" + deviceId;
        
        // 计算过期时间（2分钟后）
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(2);
        
        Map<String, Object> response = new HashMap<>();
        response.put("deviceId", deviceId);
        response.put("qrCode", qrCode);
        response.put("expiresAt", expiresAt.format(ISO_FORMATTER) + "Z");
        
        return Result.success(response);
    }
    
    /**
     * 解绑WA设备
     */
    @Operation(summary = "解绑WA设备", description = "解绑个人WhatsApp账号")
    @PostMapping("/api/v1/wa/devices/{deviceId}/unbind")
    public Result<Void> unbindWADevice(
            @Parameter(description = "设备ID") @PathVariable String deviceId
    ) {
        // Mock实现：从所有催员的账号列表中移除该设备
        COLLECTOR_ACCOUNTS.values().forEach(accounts -> 
            accounts.removeIf(account -> deviceId.equals(account.get("deviceId")))
        );
        
        return Result.success();
    }
    
    /**
     * Mock方法：模拟绑定成功（用于测试）
     */
    @Operation(summary = "模拟绑定成功（测试用）", description = "模拟设备绑定成功，添加到催员的账号列表")
    @PostMapping("/api/v1/wa/devices/{deviceId}/mock-bind")
    public Result<Void> mockBindDevice(
            @Parameter(description = "设备ID") @PathVariable String deviceId,
            @RequestParam String collectorId,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String accountName
    ) {
        List<Map<String, Object>> accounts = COLLECTOR_ACCOUNTS.computeIfAbsent(
            collectorId, 
            k -> new ArrayList<>()
        );
        
        // 创建新账号
        Map<String, Object> newAccount = new HashMap<>();
        newAccount.put("deviceId", deviceId);
        newAccount.put("phoneNumber", phoneNumber != null ? phoneNumber : "+1234567890");
        newAccount.put("accountName", accountName != null ? accountName : "My WhatsApp");
        newAccount.put("status", "paired");
        newAccount.put("pairedAt", LocalDateTime.now().format(ISO_FORMATTER) + "Z");
        newAccount.put("createdAt", LocalDateTime.now().format(ISO_FORMATTER) + "Z");
        newAccount.put("updatedAt", LocalDateTime.now().format(ISO_FORMATTER) + "Z");
        
        accounts.add(newAccount);
        
        return Result.success();
    }
}

