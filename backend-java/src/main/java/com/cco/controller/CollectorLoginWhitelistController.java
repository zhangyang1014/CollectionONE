package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.entity.CollectorLoginWhitelist;
import com.cco.service.CollectorLoginWhitelistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 催员登录白名单IP管理Controller
 * 
 * @author CCO Team
 * @since 2025-01-22
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/collector-login-whitelist")
public class CollectorLoginWhitelistController {
    
    @Autowired
    private CollectorLoginWhitelistService whitelistService;
    
    /**
     * 创建白名单IP配置
     * POST /api/v1/collector-login-whitelist
     */
    @PostMapping
    public ResponseData<CollectorLoginWhitelist> createWhitelist(@RequestBody CollectorLoginWhitelist whitelist) {
        log.info("========== 创建白名单IP配置，tenantId={}, ipAddress={} ==========",
                whitelist.getTenantId(), whitelist.getIpAddress());
        
        try {
            // 验证必填字段
            if (whitelist.getTenantId() == null) {
                return ResponseData.error(400, "tenantId不能为空");
            }
            if (whitelist.getIpAddress() == null || whitelist.getIpAddress().isEmpty()) {
                return ResponseData.error(400, "ipAddress不能为空");
            }
            
            CollectorLoginWhitelist created = whitelistService.createWhitelist(whitelist);
            log.info("========== 创建白名单IP配置成功，id={} ==========", created.getId());
            
            return ResponseData.success(created);
            
        } catch (IllegalArgumentException e) {
            log.warn("创建白名单IP配置失败：{}", e.getMessage());
            return ResponseData.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("创建白名单IP配置失败", e);
            return ResponseData.error(500, "创建配置失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新白名单IP配置
     * PUT /api/v1/collector-login-whitelist/{id}
     */
    @PutMapping("/{id}")
    public ResponseData<CollectorLoginWhitelist> updateWhitelist(
            @PathVariable Long id,
            @RequestBody CollectorLoginWhitelist whitelist) {
        log.info("========== 更新白名单IP配置，id={} ==========", id);
        
        try {
            whitelist.setId(id);
            CollectorLoginWhitelist updated = whitelistService.updateWhitelist(whitelist);
            log.info("========== 更新白名单IP配置成功，id={} ==========", id);
            
            return ResponseData.success(updated);
            
        } catch (IllegalArgumentException e) {
            log.warn("更新白名单IP配置失败：{}", e.getMessage());
            return ResponseData.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("更新白名单IP配置失败，id={}", id, e);
            return ResponseData.error(500, "更新配置失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除白名单IP配置
     * DELETE /api/v1/collector-login-whitelist/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseData<Map<String, Object>> deleteWhitelist(@PathVariable Long id) {
        log.info("========== 删除白名单IP配置，id={} ==========", id);
        
        try {
            whitelistService.deleteWhitelist(id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", id);
            result.put("message", "删除成功");
            
            log.info("========== 删除白名单IP配置成功，id={} ==========", id);
            return ResponseData.success(result);
            
        } catch (Exception e) {
            log.error("删除白名单IP配置失败，id={}", id, e);
            return ResponseData.error(500, "删除配置失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询白名单IP配置列表
     * GET /api/v1/collector-login-whitelist?tenant_id=1
     */
    @GetMapping
    public ResponseData<List<CollectorLoginWhitelist>> listWhitelists(
            @RequestParam(required = false) Long tenant_id) {
        log.info("========== 查询白名单IP配置列表，tenant_id={} ==========", tenant_id);
        
        if (tenant_id == null) {
            return ResponseData.error(400, "tenant_id不能为空");
        }
        
        List<CollectorLoginWhitelist> whitelists = whitelistService.listWhitelists(tenant_id);
        log.info("========== 返回白名单IP配置列表，数量={} ==========", whitelists.size());
        
        return ResponseData.success(whitelists);
    }
    
    /**
     * 获取白名单IP配置详情
     * GET /api/v1/collector-login-whitelist/{id}
     */
    @GetMapping("/{id}")
    public ResponseData<CollectorLoginWhitelist> getWhitelist(@PathVariable Long id) {
        log.info("========== 获取白名单IP配置详情，id={} ==========", id);
        
        CollectorLoginWhitelist whitelist = whitelistService.getById(id);
        if (whitelist == null) {
            return ResponseData.error(404, "配置不存在");
        }
        
        return ResponseData.success(whitelist);
    }
    
    /**
     * 启用/禁用甲方的白名单IP登录管理
     * PUT /api/v1/collector-login-whitelist/enable?tenant_id=1&enabled=true
     */
    @PutMapping("/enable")
    public ResponseData<Map<String, Object>> setWhitelistEnabled(
            @RequestParam Long tenant_id,
            @RequestParam Boolean enabled) {
        log.info("========== 设置白名单IP登录管理状态，tenant_id={}, enabled={} ==========", tenant_id, enabled);
        
        try {
            whitelistService.setWhitelistEnabled(tenant_id, enabled);
            
            Map<String, Object> result = new HashMap<>();
            result.put("tenant_id", tenant_id);
            result.put("enabled", enabled);
            result.put("message", "设置成功");
            
            log.info("========== 设置白名单IP登录管理状态成功 ==========");
            return ResponseData.success(result);
            
        } catch (Exception e) {
            log.error("设置白名单IP登录管理状态失败", e);
            return ResponseData.error(500, "设置失败：" + e.getMessage());
        }
    }
    
    /**
     * 检查甲方是否启用了白名单IP登录管理
     * GET /api/v1/collector-login-whitelist/check-enabled?tenant_id=1
     */
    @GetMapping("/check-enabled")
    public ResponseData<Map<String, Object>> checkWhitelistEnabled(@RequestParam Long tenant_id) {
        log.info("========== 检查白名单IP登录管理状态，tenant_id={} ==========", tenant_id);
        
        Boolean enabled = whitelistService.isWhitelistEnabled(tenant_id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("tenant_id", tenant_id);
        result.put("enabled", enabled != null && enabled);
        
        return ResponseData.success(result);
    }
}



