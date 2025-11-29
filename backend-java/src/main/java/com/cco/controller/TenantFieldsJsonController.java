package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.dto.*;
import com.cco.service.TenantFieldsJsonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 甲方字段JSON管理Controller
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/tenants/{tenantId}/fields-json")
public class TenantFieldsJsonController {
    
    @Autowired
    private TenantFieldsJsonService tenantFieldsJsonService;
    
    /**
     * 获取甲方字段JSON数据（当前版本）
     */
    @GetMapping
    public ResponseData<Map<String, Object>> getTenantFieldsJson(@PathVariable Long tenantId) {
        log.info("========== 获取甲方字段JSON数据，tenantId={} ==========", tenantId);
        
        try {
            Map<String, Object> result = tenantFieldsJsonService.getCurrentVersion(tenantId);
            if (result == null) {
                // 如果没有当前版本，返回空数据
                result = new HashMap<>();
                result.put("fetched_at", new java.util.Date().toString());
                result.put("fields", new java.util.ArrayList<>());
            }
            return ResponseData.success(result);
        } catch (Exception e) {
            // 如果表不存在或其他错误，返回空数据
            log.warn("获取甲方字段JSON数据失败（可能是表不存在），返回空数据。错误：{}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("fetched_at", new java.util.Date().toString());
            result.put("fields", new java.util.ArrayList<>());
            return ResponseData.success(result);
        }
    }
    
    /**
     * 校验JSON文件格式
     */
    @PostMapping("/validate")
    public ResponseData<TenantFieldsJsonValidateResponse> validateJson(
            @PathVariable Long tenantId,
            @RequestParam("file") MultipartFile file) {
        log.info("========== 校验JSON文件格式，tenantId={}, fileName={} ==========", tenantId, file.getOriginalFilename());
        
        try {
            // 1. 读取文件内容
            String content = new String(file.getBytes(), "UTF-8");
            
            // 2. 解析JSON
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> fieldsJson = objectMapper.readValue(content, Map.class);
            
            // 3. 校验格式
            TenantFieldsJsonValidateResponse response = tenantFieldsJsonService.validateJson(tenantId, fieldsJson);
            
            if (response.getValid()) {
                return ResponseData.success("校验通过", response);
            } else {
                return ResponseData.error(400, "校验失败");
            }
        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            log.error("JSON格式错误：", e);
            TenantFieldsJsonValidateResponse response = new TenantFieldsJsonValidateResponse();
            response.setValid(false);
            response.setErrors(List.of(
                new TenantFieldsJsonValidateResponse.ValidationError(
                    "JSON", "JSON格式错误：" + e.getMessage()
                )
            ));
            return ResponseData.error(400, "JSON格式错误");
        } catch (Exception e) {
            log.error("校验JSON文件失败：", e);
            return ResponseData.error(500, "校验失败：" + e.getMessage());
        }
    }
    
    /**
     * 对比版本差异
     */
    @PostMapping("/compare")
    public ResponseData<TenantFieldsJsonCompareResponse> compareVersions(
            @PathVariable Long tenantId,
            @RequestBody TenantFieldsJsonCompareRequest request) {
        log.info("========== 对比版本差异，tenantId={} ==========", tenantId);
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> fieldsJson = (Map<String, Object>) request.getFieldsJson();
            TenantFieldsJsonCompareResponse response = tenantFieldsJsonService.compareVersions(tenantId, fieldsJson);
            return ResponseData.success("对比完成", response);
        } catch (Exception e) {
            log.error("对比版本差异失败：", e);
            return ResponseData.error(500, "对比失败：" + e.getMessage());
        }
    }
    
    /**
     * 上传并保存JSON文件
     */
    @PostMapping("/upload")
    public ResponseData<Map<String, Object>> uploadJson(
            @PathVariable Long tenantId,
            @RequestBody TenantFieldsJsonUploadRequest request) {
        log.info("========== 上传并保存JSON文件，tenantId={}, version={} ==========", tenantId, request.getVersion());
        
        try {
            // TODO: 从SecurityContext获取当前用户
            String uploadedBy = "admin"; // 临时使用固定值
            
            Map<String, Object> result = tenantFieldsJsonService.uploadJson(tenantId, request, uploadedBy);
            return ResponseData.success("保存成功", result);
        } catch (Exception e) {
            log.error("上传JSON文件失败：", e);
            return ResponseData.error(500, "保存失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取历史版本列表
     */
    @GetMapping("/history")
    public ResponseData<List<Map<String, Object>>> getHistoryVersions(@PathVariable Long tenantId) {
        log.info("========== 获取历史版本列表，tenantId={} ==========", tenantId);
        
        List<Map<String, Object>> versions = tenantFieldsJsonService.getHistoryVersions(tenantId);
        return ResponseData.success(versions);
    }
}

