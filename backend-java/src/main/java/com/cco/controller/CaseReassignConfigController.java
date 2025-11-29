package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.entity.CaseReassignConfig;
import com.cco.service.CaseReassignConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 案件重新分案配置管理Controller
 * 
 * @author CCO Team
 * @since 2025-01-22
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/case-reassign-configs")
public class CaseReassignConfigController {
    
    @Autowired
    private CaseReassignConfigService configService;
    
    /**
     * 创建重新分案配置（带替换选项）
     * POST /api/v1/case-reassign-configs?replace=true
     */
    @PostMapping
    public ResponseData<?> createConfig(
            @RequestBody CaseReassignConfig config,
            @RequestParam(required = false, defaultValue = "false") Boolean replace) {
        log.info("========== 创建重新分案配置，tenantId={}, configType={}, targetId={}, reassignDays={} ==========",
                config.getTenantId(), config.getConfigType(), config.getTargetId(), config.getReassignDays());
        
        try {
            // 验证必填字段
            if (config.getTenantId() == null) {
                return ResponseData.error(400, "tenantId不能为空");
            }
            if (config.getConfigType() == null || config.getConfigType().isEmpty()) {
                return ResponseData.error(400, "configType不能为空");
            }
            if (config.getTargetId() == null) {
                return ResponseData.error(400, "targetId不能为空");
            }
            if (config.getReassignDays() == null || config.getReassignDays() <= 0) {
                return ResponseData.error(400, "reassignDays必须大于0");
            }
            
            // 验证configType值（只支持队列）
            if (!"queue".equals(config.getConfigType())) {
                return ResponseData.error(400, "configType必须是queue（队列）");
            }
            
            // 检查是否有冲突配置（队列-小组维度）
            List<Long> teamIds = parseTeamIds(config.getTeamIds());
            List<CaseReassignConfig> conflicts = configService.checkConflictConfigs(
                    config.getTenantId(), config.getTargetId(), teamIds);
            
            // 如果有冲突且用户选择替换，先删除冲突配置
            if (!conflicts.isEmpty() && Boolean.TRUE.equals(replace)) {
                configService.deleteConflictConfigs(config.getTenantId(), config.getTargetId(), teamIds);
                log.info("已删除{}个冲突配置", conflicts.size());
            } else if (!conflicts.isEmpty()) {
                // 如果有冲突但用户未选择替换，返回冲突信息
                Map<String, Object> result = new HashMap<>();
                result.put("hasConflict", true);
                result.put("conflicts", conflicts);
                result.put("message", "存在冲突的配置，请确认是否替换");
                // 返回特殊状态码，前端可以根据这个判断
                ResponseData<Map<String, Object>> response = new ResponseData<>(409, "存在冲突的配置", result);
                return response;
            }
            
            CaseReassignConfig created = configService.createConfig(config);
            log.info("========== 创建重新分案配置成功，id={}, effectiveDate={} ==========", 
                    created.getId(), created.getEffectiveDate());
            
            return ResponseData.success(created);
            
        } catch (Exception e) {
            log.error("创建重新分案配置失败", e);
            return ResponseData.error(500, "创建配置失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新重新分案配置
     * PUT /api/v1/case-reassign-configs/{id}
     */
    @PutMapping("/{id}")
    public ResponseData<CaseReassignConfig> updateConfig(
            @PathVariable Long id,
            @RequestBody CaseReassignConfig config) {
        log.info("========== 更新重新分案配置，id={} ==========", id);
        
        try {
            config.setId(id);
            CaseReassignConfig updated = configService.updateConfig(config);
            log.info("========== 更新重新分案配置成功，id={} ==========", id);
            
            return ResponseData.success(updated);
            
        } catch (Exception e) {
            log.error("更新重新分案配置失败，id={}", id, e);
            return ResponseData.error(500, "更新配置失败：" + e.getMessage());
        }
    }
    
    /**
     * 检查队列-小组维度是否有冲突配置
     * GET /api/v1/case-reassign-configs/check-conflict
     */
    @GetMapping("/check-conflict")
    public ResponseData<Map<String, Object>> checkConflict(
            @RequestParam Long tenantId,
            @RequestParam Long queueId,
            @RequestParam(required = false) String teamIds) {
        log.info("========== 检查冲突配置，tenantId={}, queueId={}, teamIds={} ==========", 
                tenantId, queueId, teamIds);
        
        try {
            List<Long> teamIdList = parseTeamIds(teamIds);
            List<CaseReassignConfig> conflicts = configService.checkConflictConfigs(
                    tenantId, queueId, teamIdList);
            
            Map<String, Object> result = new HashMap<>();
            result.put("hasConflict", !conflicts.isEmpty());
            result.put("conflicts", conflicts);
            
            return ResponseData.success(result);
            
        } catch (Exception e) {
            log.error("检查冲突配置失败", e);
            return ResponseData.error(500, "检查冲突失败：" + e.getMessage());
        }
    }
    
    /**
     * 解析teamIds字符串为List
     */
    private List<Long> parseTeamIds(String teamIdsStr) {
        if (teamIdsStr == null || teamIdsStr.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return objectMapper.readValue(teamIdsStr, new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {});
        } catch (Exception e) {
            log.error("解析teamIds失败，teamIds={}", teamIdsStr, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 删除重新分案配置
     * DELETE /api/v1/case-reassign-configs/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseData<Map<String, Object>> deleteConfig(@PathVariable Long id) {
        log.info("========== 删除重新分案配置，id={} ==========", id);
        
        try {
            configService.deleteConfig(id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", id);
            result.put("message", "删除成功");
            
            log.info("========== 删除重新分案配置成功，id={} ==========", id);
            return ResponseData.success(result);
            
        } catch (Exception e) {
            log.error("删除重新分案配置失败，id={}", id, e);
            return ResponseData.error(500, "删除配置失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询重新分案配置列表
     * GET /api/v1/case-reassign-configs
     */
    @GetMapping
    public ResponseData<List<CaseReassignConfig>> listConfigs(
            @RequestParam(required = false) Long tenant_id,
            @RequestParam(required = false) String config_type) {
        log.info("========== 查询重新分案配置列表，tenant_id={}, config_type={} ==========", 
                tenant_id, config_type);
        
        List<CaseReassignConfig> configs = configService.listConfigs(tenant_id, config_type);
        log.info("========== 返回重新分案配置列表，数量={} ==========", configs.size());
        
        return ResponseData.success(configs);
    }
    
    /**
     * 获取重新分案配置详情
     * GET /api/v1/case-reassign-configs/{id}
     */
    @GetMapping("/{id}")
    public ResponseData<CaseReassignConfig> getConfig(@PathVariable Long id) {
        log.info("========== 获取重新分案配置详情，id={} ==========", id);
        
        CaseReassignConfig config = configService.getById(id);
        if (config == null) {
            return ResponseData.error(404, "配置不存在");
        }
        
        return ResponseData.success(config);
    }
}

