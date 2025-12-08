package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.dto.TenantFieldUploadDTO;
import com.cco.model.dto.VersionCompareResponse;
import com.cco.service.TenantFieldUploadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 甲方字段上传管理Controller
 *
 * @author CCO Team
 * @since 2025-12-08
 */
@Slf4j
@RestController
@ConditionalOnProperty(prefix = "feature.tenant-field-upload", name = "enabled", havingValue = "true", matchIfMissing = false)
@RequestMapping(Constants.API_V1_PREFIX + "/tenants")
public class TenantFieldUploadController {

    @Resource
    private TenantFieldUploadService tenantFieldUploadService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取当前生效版本的字段JSON
     * GET /api/v1/tenants/{tenantId}/fields-json?scene=list
     */
    @GetMapping("/{tenantId}/fields-json")
    public ResponseData<Map<String, Object>> getCurrentVersionFields(
            @PathVariable String tenantId,
            @RequestParam(required = false, defaultValue = "list") String scene) {
        
        log.info("获取当前生效版本的字段JSON，tenantId={}, scene={}", tenantId, scene);

        try {
            Map<String, Object> result = tenantFieldUploadService.getCurrentVersionFields(tenantId, scene);
            return ResponseData.success(result);
        } catch (Exception e) {
            log.error("获取字段JSON失败", e);
            return ResponseData.error("获取字段JSON失败：" + e.getMessage());
        }
    }

    /**
     * 上传JSON文件
     * POST /api/v1/tenants/{tenantId}/fields-json/upload
     */
    @PostMapping("/{tenantId}/fields-json/upload")
    public ResponseData<Map<String, Object>> uploadJsonFile(
            @PathVariable String tenantId,
            @RequestParam MultipartFile file,
            @RequestParam(required = false, defaultValue = "list") String scene,
            @RequestParam(required = false, defaultValue = "admin") String uploadedBy,
            @RequestParam(required = false) String versionNote) {
        
        log.info("上传JSON文件，tenantId={}, scene={}, fileName={}", tenantId, scene, file.getOriginalFilename());

        try {
            Map<String, Object> result = tenantFieldUploadService.uploadJsonFile(
                    tenantId, scene, file, uploadedBy, versionNote);
            return ResponseData.success(result);
        } catch (Exception e) {
            log.error("上传JSON文件失败", e);
            return ResponseData.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 验证JSON文件
     * POST /api/v1/tenants/{tenantId}/fields-json/validate
     */
    @PostMapping("/{tenantId}/fields-json/validate")
    public ResponseData<Map<String, Object>> validateJsonFile(
            @PathVariable String tenantId,
            @RequestParam MultipartFile file) {
        
        log.info("验证JSON文件，tenantId={}, fileName={}", tenantId, file.getOriginalFilename());

        try {
            Map<String, Object> result = tenantFieldUploadService.validateJsonFile(file);
            return ResponseData.success(result);
        } catch (Exception e) {
            log.error("验证JSON文件失败", e);
            return ResponseData.error("验证失败：" + e.getMessage());
        }
    }

    /**
     * 获取上传历史
     * GET /api/v1/tenants/{tenantId}/fields-json/history?scene=list&page=1&page_size=10
     */
    @GetMapping("/{tenantId}/fields-json/history")
    public ResponseData<Map<String, Object>> getUploadHistory(
            @PathVariable String tenantId,
            @RequestParam(required = false, defaultValue = "list") String scene,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        
        log.info("获取上传历史，tenantId={}, scene={}, page={}, pageSize={}", tenantId, scene, page, pageSize);

        try {
            Map<String, Object> result = tenantFieldUploadService.getUploadHistory(tenantId, scene, page, pageSize);
            return ResponseData.success(result);
        } catch (Exception e) {
            log.error("获取上传历史失败", e);
            return ResponseData.error("获取上传历史失败：" + e.getMessage());
        }
    }

    /**
     * 获取特定版本详情
     * GET /api/v1/tenants/{tenantId}/fields-json/version/{version}?scene=list
     */
    @GetMapping("/{tenantId}/fields-json/version/{version}")
    public ResponseData<TenantFieldUploadDTO> getVersionDetail(
            @PathVariable String tenantId,
            @PathVariable Integer version,
            @RequestParam(required = false, defaultValue = "list") String scene) {
        
        log.info("获取版本详情，tenantId={}, scene={}, version={}", tenantId, scene, version);

        try {
            TenantFieldUploadDTO result = tenantFieldUploadService.getVersionDetail(tenantId, scene, version);
            return ResponseData.success(result);
        } catch (Exception e) {
            log.error("获取版本详情失败", e);
            return ResponseData.error("获取版本详情失败：" + e.getMessage());
        }
    }

    /**
     * 版本对比
     * GET /api/v1/tenants/{tenantId}/fields-json/compare?scene=list&version1=1&version2=2
     */
    @GetMapping("/{tenantId}/fields-json/compare")
    public ResponseData<VersionCompareResponse> compareVersions(
            @PathVariable String tenantId,
            @RequestParam(required = false, defaultValue = "list") String scene,
            @RequestParam Integer version1,
            @RequestParam Integer version2) {
        
        log.info("版本对比，tenantId={}, scene={}, version1={}, version2={}", tenantId, scene, version1, version2);

        try {
            VersionCompareResponse result = tenantFieldUploadService.compareVersions(tenantId, scene, version1, version2);
            return ResponseData.success(result);
        } catch (Exception e) {
            log.error("版本对比失败", e);
            return ResponseData.error("版本对比失败：" + e.getMessage());
        }
    }

    /**
     * 设置当前版本
     * PUT /api/v1/tenants/{tenantId}/fields-json/activate/{version}?scene=list
     */
    @PutMapping("/{tenantId}/fields-json/activate/{version}")
    public ResponseData<Map<String, Object>> activateVersion(
            @PathVariable String tenantId,
            @PathVariable Integer version,
            @RequestParam(required = false, defaultValue = "list") String scene,
            @RequestBody(required = false) Map<String, String> body) {
        
        log.info("设置当前版本，tenantId={}, scene={}, version={}", tenantId, scene, version);

        try {
            String operatorId = body != null ? body.get("operator_id") : "admin";
            String reason = body != null ? body.get("reason") : "";
            
            Map<String, Object> result = tenantFieldUploadService.activateVersion(
                    tenantId, scene, version, operatorId, reason);
            return ResponseData.success(result);
        } catch (Exception e) {
            log.error("设置当前版本失败", e);
            return ResponseData.error("设置当前版本失败：" + e.getMessage());
        }
    }

    /**
     * 下载历史版本
     * GET /api/v1/tenants/{tenantId}/fields-json/download/{version}?scene=list
     */
    @GetMapping("/{tenantId}/fields-json/download/{version}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadVersion(
            @PathVariable String tenantId,
            @PathVariable Integer version,
            @RequestParam(required = false, defaultValue = "list") String scene) {
        
        log.info("下载历史版本，tenantId={}, scene={}, version={}", tenantId, scene, version);

        try {
            TenantFieldUploadDTO dto = tenantFieldUploadService.getVersionDetail(tenantId, scene, version);
            if (dto == null) {
                return ResponseEntity.notFound().build();
            }

            // 直接使用数据库中的字段内容生成JSON返回
            Map<String, Object> jsonData = new HashMap<>();
            jsonData.put("version", "1.0");
            jsonData.put("scene", scene);
            jsonData.put("tenant_id", tenantId);
            jsonData.put("fields", dto.getFields());

            String jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonData);
            byte[] bytes = jsonContent.getBytes(StandardCharsets.UTF_8);

            String filename = String.format("tenant_%s_%s_v%d.json", tenantId, scene, version);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(new org.springframework.core.io.ByteArrayResource(bytes));

        } catch (Exception e) {
            log.error("下载历史版本失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 下载JSON模板
     * GET /api/v1/tenants/fields-json/template?scene=list
     */
    @GetMapping("/fields-json/template")
    public ResponseEntity<String> downloadTemplate(
            @RequestParam(required = false, defaultValue = "list") String scene) {
        
        log.info("下载JSON模板，scene={}", scene);

        try {
            Map<String, Object> template = tenantFieldUploadService.getJsonTemplate(scene);
            String jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(template);
            
            String filename = String.format("tenant_fields_%s_template.json", scene);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(jsonContent);

        } catch (Exception e) {
            log.error("下载JSON模板失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
