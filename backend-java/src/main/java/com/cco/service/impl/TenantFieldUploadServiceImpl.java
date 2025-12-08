package com.cco.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cco.mapper.TenantFieldUploadMapper;
import com.cco.model.dto.TenantFieldUploadDTO;
import com.cco.model.dto.VersionCompareResponse;
import com.cco.model.entity.TenantFieldUpload;
import com.cco.service.TenantFieldUploadService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 甲方字段上传记录Service实现类
 *
 * @author CCO Team
 * @since 2025-12-08
 */
@Slf4j
@Service
public class TenantFieldUploadServiceImpl extends ServiceImpl<TenantFieldUploadMapper, TenantFieldUpload> 
        implements TenantFieldUploadService {

    @Resource
    private TenantFieldUploadMapper tenantFieldUploadMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 文件存储根目录
     */
    private static final String UPLOAD_ROOT_DIR = System.getProperty("user.home") + "/.cco-storage/uploads/tenant-fields";

    /**
     * 最大文件大小：2MB
     */
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> uploadJsonFile(String tenantId, String scene, MultipartFile file, 
                                             String uploadedBy, String versionNote) {
        log.info("开始上传JSON文件，tenantId={}, scene={}, fileName={}", tenantId, scene, file.getOriginalFilename());

        try {
            // 1. 验证文件
            Map<String, Object> validateResult = validateJsonFile(file);
            if (!(Boolean) validateResult.get("valid")) {
                throw new RuntimeException("文件验证失败：" + validateResult.get("errors"));
            }

            // 2. 解析JSON内容
            String content = new String(file.getBytes());
            Map<String, Object> jsonData = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> fields = (List<Map<String, Object>>) jsonData.get("fields");
            if (fields == null || fields.isEmpty()) {
                throw new RuntimeException("字段列表不能为空");
            }

            // 3. 获取下一个版本号
            Integer nextVersion = tenantFieldUploadMapper.getMaxVersion(tenantId, scene) + 1;
            log.info("下一个版本号: {}", nextVersion);

            // 4. 保存文件到文件系统
            String fileName = file.getOriginalFilename();
            String filePath = String.format("%s/%s/%s/v%d/fields.json", UPLOAD_ROOT_DIR, tenantId, scene, nextVersion);
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            log.info("文件已保存到: {}", filePath);

            // 5. 创建上传记录
            TenantFieldUpload upload = new TenantFieldUpload();
            upload.setTenantId(tenantId);
            upload.setScene(scene);
            upload.setVersion(nextVersion);
            upload.setFileName(fileName);
            upload.setFileSize((int) file.getSize());
            upload.setFilePath(filePath);
            upload.setFieldsCount(fields.size());
            upload.setUploadedBy(uploadedBy);
            upload.setUploadedByName(getUploadedByName(uploadedBy));  // TODO: 从用户服务获取
            upload.setUploadedAt(LocalDateTime.now());
            upload.setJsonContent(jsonData);
            upload.setIsActive(false);  // 先设为非激活
            upload.setVersionNote(versionNote);

            // 6. 保存到数据库
            save(upload);

            // 7. 将此版本设为当前生效版本
            activateVersionInternal(tenantId, scene, nextVersion);

            Map<String, Object> result = new HashMap<>();
            result.put("version", nextVersion);
            result.put("fields_count", fields.size());
            result.put("uploaded_at", upload.getUploadedAt());
            result.put("is_active", true);

            log.info("JSON文件上传成功，版本={}", nextVersion);
            return result;

        } catch (Exception e) {
            log.error("上传JSON文件失败", e);
            throw new RuntimeException("上传失败：" + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getUploadHistory(String tenantId, String scene, Integer page, Integer pageSize) {
        log.info("获取上传历史，tenantId={}, scene={}, page={}, pageSize={}", tenantId, scene, page, pageSize);

        page = page != null && page > 0 ? page : 1;
        pageSize = pageSize != null && pageSize > 0 && pageSize <= 50 ? pageSize : 10;

        try {
            // 查询总数
            QueryWrapper<TenantFieldUpload> countWrapper = new QueryWrapper<>();
            countWrapper.eq("tenant_id", tenantId).eq("scene", scene);
            long total = count(countWrapper);

            // 查询当前生效版本号
            QueryWrapper<TenantFieldUpload> activeWrapper = new QueryWrapper<>();
            activeWrapper.eq("tenant_id", tenantId).eq("scene", scene).eq("is_active", true);
            TenantFieldUpload activeVersion = getOne(activeWrapper);
            Integer currentVersion = activeVersion != null ? activeVersion.getVersion() : null;

            // 分页查询
            QueryWrapper<TenantFieldUpload> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("tenant_id", tenantId)
                       .eq("scene", scene)
                       .orderByDesc("version");
            
            int offset = (page - 1) * pageSize;
            queryWrapper.last("LIMIT " + offset + ", " + pageSize);

            List<TenantFieldUpload> uploads = list(queryWrapper);
            List<TenantFieldUploadDTO> records = uploads.stream().map(this::convertToDTO).collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("current_version", currentVersion);
            result.put("records", records);

            return result;

        } catch (Exception e) {
            log.error("获取上传历史失败", e);
            throw new RuntimeException("获取上传历史失败：" + e.getMessage(), e);
        }
    }

    @Override
    public TenantFieldUploadDTO getVersionDetail(String tenantId, String scene, Integer version) {
        log.info("获取版本详情，tenantId={}, scene={}, version={}", tenantId, scene, version);

        try {
            QueryWrapper<TenantFieldUpload> wrapper = new QueryWrapper<>();
            wrapper.eq("tenant_id", tenantId)
                   .eq("scene", scene)
                   .eq("version", version);

            TenantFieldUpload upload = getOne(wrapper);
            if (upload == null) {
                throw new RuntimeException("版本不存在");
            }

            TenantFieldUploadDTO dto = convertToDTO(upload);
            
            // 提取字段列表
            if (upload.getJsonContent() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> jsonData = (Map<String, Object>) upload.getJsonContent();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> fields = (List<Map<String, Object>>) jsonData.get("fields");
                dto.setFields(fields);
            }

            return dto;

        } catch (Exception e) {
            log.error("获取版本详情失败", e);
            throw new RuntimeException("获取版本详情失败：" + e.getMessage(), e);
        }
    }

    @Override
    public VersionCompareResponse compareVersions(String tenantId, String scene, Integer version1, Integer version2) {
        log.info("对比版本，tenantId={}, scene={}, version1={}, version2={}", tenantId, scene, version1, version2);

        try {
            // 1. 获取两个版本的数据
            TenantFieldUploadDTO v1 = getVersionDetail(tenantId, scene, version1);
            TenantFieldUploadDTO v2 = getVersionDetail(tenantId, scene, version2);

            if (v1 == null || v2 == null) {
                throw new RuntimeException("版本不存在");
            }

            // 2. 构建响应对象
            VersionCompareResponse response = new VersionCompareResponse();

            // 版本信息
            VersionCompareResponse.VersionInfo info1 = new VersionCompareResponse.VersionInfo();
            info1.setVersion(v1.getVersion());
            info1.setUploadedAt(v1.getUploadedAt());
            info1.setFieldsCount(v1.getFieldsCount());
            response.setVersion1(info1);

            VersionCompareResponse.VersionInfo info2 = new VersionCompareResponse.VersionInfo();
            info2.setVersion(v2.getVersion());
            info2.setUploadedAt(v2.getUploadedAt());
            info2.setFieldsCount(v2.getFieldsCount());
            response.setVersion2(info2);

            // 3. 对比字段
            List<Map<String, Object>> fields1 = v1.getFields();
            List<Map<String, Object>> fields2 = v2.getFields();

            Map<String, Map<String, Object>> map1 = fieldsToMap(fields1);
            Map<String, Map<String, Object>> map2 = fieldsToMap(fields2);

            List<Map<String, Object>> added = new ArrayList<>();
            List<Map<String, Object>> removed = new ArrayList<>();
            List<VersionCompareResponse.FieldChange> modified = new ArrayList<>();
            List<Map<String, Object>> unchanged = new ArrayList<>();

            // 查找新增和修改的字段
            for (Map.Entry<String, Map<String, Object>> entry : map2.entrySet()) {
                String fieldKey = entry.getKey();
                Map<String, Object> field2 = entry.getValue();

                if (!map1.containsKey(fieldKey)) {
                    // 新增字段
                    added.add(field2);
                } else {
                    // 对比字段
                    Map<String, Object> field1 = map1.get(fieldKey);
                    List<VersionCompareResponse.PropertyChange> changes = compareFields(field1, field2);
                    
                    if (!changes.isEmpty()) {
                        VersionCompareResponse.FieldChange fieldChange = new VersionCompareResponse.FieldChange();
                        fieldChange.setFieldKey(fieldKey);
                        fieldChange.setFieldName(String.valueOf(field2.get("field_name")));
                        fieldChange.setChanges(changes);
                        modified.add(fieldChange);
                    } else {
                        unchanged.add(field2);
                    }
                }
            }

            // 查找删除的字段
            for (Map.Entry<String, Map<String, Object>> entry : map1.entrySet()) {
                String fieldKey = entry.getKey();
                if (!map2.containsKey(fieldKey)) {
                    removed.add(entry.getValue());
                }
            }

            // 4. 构建摘要和详情
            VersionCompareResponse.Summary summary = new VersionCompareResponse.Summary();
            summary.setAdded(added.size());
            summary.setRemoved(removed.size());
            summary.setModified(modified.size());
            summary.setUnchanged(unchanged.size());
            response.setSummary(summary);

            VersionCompareResponse.Details details = new VersionCompareResponse.Details();
            details.setAdded(added);
            details.setRemoved(removed);
            details.setModified(modified);
            details.setUnchanged(unchanged);
            response.setDetails(details);

            log.info("版本对比完成，新增={}, 删除={}, 修改={}, 未变更={}", 
                    added.size(), removed.size(), modified.size(), unchanged.size());

            return response;

        } catch (Exception e) {
            log.error("版本对比失败", e);
            throw new RuntimeException("版本对比失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> activateVersion(String tenantId, String scene, Integer version, 
                                               String operatorId, String reason) {
        log.info("设置当前版本，tenantId={}, scene={}, version={}, operatorId={}", tenantId, scene, version, operatorId);

        try {
            // 获取旧版本号
            QueryWrapper<TenantFieldUpload> activeWrapper = new QueryWrapper<>();
            activeWrapper.eq("tenant_id", tenantId).eq("scene", scene).eq("is_active", true);
            TenantFieldUpload oldActive = getOne(activeWrapper);
            Integer oldVersion = oldActive != null ? oldActive.getVersion() : null;

            // 激活新版本
            activateVersionInternal(tenantId, scene, version);

            Map<String, Object> result = new HashMap<>();
            result.put("old_version", oldVersion);
            result.put("new_version", version);
            result.put("activated_at", LocalDateTime.now());

            log.info("版本切换成功，从版本{}切换到版本{}", oldVersion, version);
            return result;

        } catch (Exception e) {
            log.error("设置当前版本失败", e);
            throw new RuntimeException("设置当前版本失败：" + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getCurrentVersionFields(String tenantId, String scene) {
        log.info("获取当前版本字段，tenantId={}, scene={}", tenantId, scene);

        try {
            QueryWrapper<TenantFieldUpload> wrapper = new QueryWrapper<>();
            wrapper.eq("tenant_id", tenantId)
                   .eq("scene", scene)
                   .eq("is_active", true);

            TenantFieldUpload upload = getOne(wrapper);
            if (upload == null) {
                // 如果没有激活版本，回退到最新版本
                log.warn("未找到当前生效版本，尝试回退到最新版本");
                QueryWrapper<TenantFieldUpload> latestWrapper = new QueryWrapper<>();
                latestWrapper.eq("tenant_id", tenantId)
                             .eq("scene", scene)
                             .orderByDesc("version")
                             .last("LIMIT 1");
                upload = getOne(latestWrapper);
                if (upload == null) {
                    return buildMockFields(tenantId, scene);
                }
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> jsonData = (Map<String, Object>) upload.getJsonContent();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> fields = (List<Map<String, Object>>) jsonData.get("fields");

            Map<String, Object> result = new HashMap<>();
            result.put("tenant_id", tenantId);
            result.put("tenant_name", getTenantName(tenantId));  // TODO: 从租户服务获取
            result.put("scene", scene);
            result.put("version", upload.getVersion());
            result.put("fetched_at", upload.getUploadedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            result.put("fields_count", fields.size());
            result.put("uploaded_by", upload.getUploadedBy());
            result.put("uploaded_by_name", upload.getUploadedByName());
            result.put("fields", fields);

            return result;

        } catch (Exception e) {
            log.error("获取当前版本字段失败", e);
            // 数据表可能不存在时，回退到内置Mock字段，确保前端可展示
            return buildMockFields(tenantId, scene);
        }
    }

    @Override
    public Map<String, Object> getJsonTemplate(String scene) {
        log.info("获取JSON模板，scene={}", scene);

        Map<String, Object> template = new HashMap<>();
        template.put("version", "1.0");
        template.put("scene", scene);
        template.put("tenant_id", "示例甲方ID");
        template.put("tenant_name", "示例甲方名称");
        template.put("updated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        template.put("description", "list".equals(scene) ? "案件列表字段配置" : "案件详情字段配置");

        List<Map<String, Object>> fields = new ArrayList<>();

        // 示例字段
        fields.add(createTemplateField("案件编号", "case_id", "String", null, true, 1, "案件唯一标识，不可为空"));
        fields.add(createTemplateField("案件状态", "case_status", "Enum", 
                Arrays.asList("待分配", "催收中", "已完成", "已关闭"), true, 2, "案件当前状态，用于流程跟踪"));
        fields.add(createTemplateField("借款金额", "loan_amount", "Number", null, true, 3, "借款本金，单位：元"));
        fields.add(createTemplateField("逾期天数", "overdue_days", "Integer", null, false, 4, "当前逾期天数，自动计算"));
        fields.add(createTemplateField("借款人姓名", "borrower_name", "String", null, true, 5, "借款人真实姓名"));
        fields.add(createTemplateField("借款人手机", "borrower_phone", "String", null, true, 6, "借款人联系电话"));

        template.put("fields", fields);

        return template;
    }

    /**
     * 当未找到版本或数据表缺失时，回退到内置的案件列表字段Mock
     */
    private Map<String, Object> buildMockFields(String tenantId, String scene) {
        List<Map<String, Object>> fields = new ArrayList<>();

        fields.add(createTemplateField("案件编号", "case_id", "String", null, true, 1, "案件唯一标识"));
        fields.add(createTemplateField("案件状态", "case_status", "Enum",
                Arrays.asList("待分配", "催收中", "已完成", "已关闭"), true, 2, "案件当前状态"));
        fields.add(createTemplateField("逾期天数", "overdue_days", "Integer", null, false, 3, "当前逾期天数"));
        fields.add(createTemplateField("借款金额", "loan_amount", "Number", null, true, 4, "借款本金"));
        fields.add(createTemplateField("未还金额", "outstanding_amount", "Number", null, true, 5, "剩余未还金额"));
        fields.add(createTemplateField("客户姓名", "customer_name", "String", null, true, 6, "客户姓名"));
        fields.add(createTemplateField("联系电话", "phone", "String", null, true, 7, "联系电话"));
        fields.add(createTemplateField("产品名称", "product_name", "String", null, false, 8, "产品名称"));
        fields.add(createTemplateField("APP名称", "app_name", "String", null, false, 9, "APP名称"));
        fields.add(createTemplateField("商户名称", "merchant_name", "String", null, false, 10, "商户名称"));
        fields.add(createTemplateField("到期日期", "due_date", "Date", null, true, 11, "到期日期"));
        fields.add(createTemplateField("催收员", "collector", "String", null, false, 12, "当前催收员"));

        // 可筛选/范围检索开关默认打开
        for (Map<String, Object> field : fields) {
            field.put("is_filterable", true);
            field.put("is_range_searchable", true);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("tenant_id", tenantId);
        result.put("scene", scene);
        result.put("version", 0);
        result.put("fields_count", fields.size());
        result.put("fields", fields);
        result.put("uploaded_by", "system");
        result.put("uploaded_by_name", "system");
        result.put("fetched_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return result;
    }

    @Override
    public Map<String, Object> validateJsonFile(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();

        try {
            // 1. 检查文件扩展名
            String filename = file.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".json")) {
                errors.add("文件扩展名必须为 .json");
            }

            // 2. 检查文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                errors.add("文件大小超过2MB限制");
            }

            // 3. 检查JSON格式
            String content = new String(file.getBytes());
            Map<String, Object> jsonData;
            try {
                jsonData = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                errors.add("JSON格式错误：" + e.getMessage());
                result.put("valid", false);
                result.put("errors", errors);
                return result;
            }

            // 4. 检查必需字段
            if (!jsonData.containsKey("fields")) {
                errors.add("缺少必需字段：fields");
            } else {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> fields = (List<Map<String, Object>>) jsonData.get("fields");
                if (fields == null || fields.isEmpty()) {
                    errors.add("fields 数组不能为空");
                } else {
                    // 5. 验证每个字段
                    Set<String> fieldKeys = new HashSet<>();
                    for (int i = 0; i < fields.size(); i++) {
                        Map<String, Object> field = fields.get(i);
                        String prefix = "第" + (i + 1) + "个字段";

                        // field_name
                        if (!field.containsKey("field_name") || field.get("field_name") == null 
                                || field.get("field_name").toString().trim().isEmpty()) {
                            errors.add(prefix + "缺少必需属性 field_name");
                        }

                        // field_key
                        String fieldKey = (String) field.get("field_key");
                        if (fieldKey == null || fieldKey.trim().isEmpty()) {
                            errors.add(prefix + "缺少必需属性 field_key");
                        } else {
                            // 检查field_key格式（字母开头，仅包含字母数字下划线）
                            if (!fieldKey.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
                                errors.add(prefix + "的 field_key \"" + fieldKey + "\" 格式不正确，必须以字母开头，仅包含字母数字下划线");
                            }
                            // 检查重复
                            if (fieldKeys.contains(fieldKey)) {
                                errors.add("字段标识 \"" + fieldKey + "\" 重复出现");
                            }
                            fieldKeys.add(fieldKey);
                        }

                        // field_type
                        String fieldType = (String) field.get("field_type");
                        if (fieldType == null || fieldType.trim().isEmpty()) {
                            errors.add(prefix + "缺少必需属性 field_type");
                        } else {
                            List<String> validTypes = Arrays.asList("String", "Integer", "Number", "Boolean", "Date", "DateTime", "Enum");
                            if (!validTypes.contains(fieldType)) {
                                errors.add(prefix + "的 field_type \"" + fieldType + "\" 不在支持类型列表中。支持的类型：" + String.join(", ", validTypes));
                            }

                            // 如果是Enum类型，检查enum_values
                            if ("Enum".equals(fieldType)) {
                                @SuppressWarnings("unchecked")
                                List<String> enumValues = (List<String>) field.get("enum_values");
                                if (enumValues == null || enumValues.size() < 2) {
                                    errors.add(prefix + "的 field_type 为 Enum，但 enum_values 缺失或少于2个元素");
                                }
                            }
                        }

                        // is_required
                        if (!field.containsKey("is_required")) {
                            errors.add(prefix + "缺少必需属性 is_required");
                        }

                        // sort_order
                        if (!field.containsKey("sort_order")) {
                            errors.add(prefix + "缺少必需属性 sort_order");
                        } else {
                            Object sortOrder = field.get("sort_order");
                            if (sortOrder instanceof Number) {
                                if (((Number) sortOrder).intValue() <= 0) {
                                    errors.add(prefix + "的 sort_order 必须大于0");
                                }
                            }
                        }
                    }
                }
            }

            result.put("valid", errors.isEmpty());
            result.put("errors", errors);

        } catch (Exception e) {
            log.error("验证JSON文件失败", e);
            errors.add("验证过程出错：" + e.getMessage());
            result.put("valid", false);
            result.put("errors", errors);
        }

        return result;
    }

    /**
     * 激活指定版本（内部方法）
     */
    @Transactional(rollbackFor = Exception.class)
    public void activateVersionInternal(String tenantId, String scene, Integer version) {
        // 1. 将所有版本设为非激活
        tenantFieldUploadMapper.deactivateAllVersions(tenantId, scene);

        // 2. 将指定版本设为激活
        QueryWrapper<TenantFieldUpload> wrapper = new QueryWrapper<>();
        wrapper.eq("tenant_id", tenantId)
               .eq("scene", scene)
               .eq("version", version);

        TenantFieldUpload upload = getOne(wrapper);
        if (upload == null) {
            throw new RuntimeException("版本不存在");
        }

        upload.setIsActive(true);
        updateById(upload);

        log.info("已激活版本，tenantId={}, scene={}, version={}", tenantId, scene, version);
    }

    /**
     * 将字段列表转换为Map（key为field_key）
     */
    private Map<String, Map<String, Object>> fieldsToMap(List<Map<String, Object>> fields) {
        if (fields == null) {
            return new HashMap<>();
        }
        return fields.stream()
                .collect(Collectors.toMap(
                        f -> String.valueOf(f.get("field_key")),
                        f -> f,
                        (a, b) -> a
                ));
    }

    /**
     * 对比两个字段的差异
     */
    private List<VersionCompareResponse.PropertyChange> compareFields(Map<String, Object> field1, Map<String, Object> field2) {
        List<VersionCompareResponse.PropertyChange> changes = new ArrayList<>();

        // 对比各个属性
        compareProperty("field_name", field1, field2, changes);
        compareProperty("field_type", field1, field2, changes);
        compareProperty("is_required", field1, field2, changes);
        compareProperty("sort_order", field1, field2, changes);
        compareProperty("description", field1, field2, changes);

        // 对比枚举值（特殊处理）
        Object enum1 = field1.get("enum_values");
        Object enum2 = field2.get("enum_values");
        if (!Objects.equals(enum1, enum2)) {
            VersionCompareResponse.PropertyChange change = new VersionCompareResponse.PropertyChange();
            change.setProperty("enum_values");
            change.setOldValue(enum1);
            change.setNewValue(enum2);
            changes.add(change);
        }

        return changes;
    }

    /**
     * 对比单个属性
     */
    private void compareProperty(String property, Map<String, Object> field1, Map<String, Object> field2,
                                 List<VersionCompareResponse.PropertyChange> changes) {
        Object value1 = field1.get(property);
        Object value2 = field2.get(property);

        if (!Objects.equals(value1, value2)) {
            VersionCompareResponse.PropertyChange change = new VersionCompareResponse.PropertyChange();
            change.setProperty(property);
            change.setOldValue(value1);
            change.setNewValue(value2);
            changes.add(change);
        }
    }

    /**
     * 创建模板字段
     */
    private Map<String, Object> createTemplateField(String name, String key, String type, 
                                                   List<String> enumValues, boolean required, 
                                                   int order, String description) {
        Map<String, Object> field = new HashMap<>();
        field.put("field_name", name);
        field.put("field_key", key);
        field.put("field_type", type);
        field.put("enum_values", enumValues);
        field.put("is_required", required);
        field.put("sort_order", order);
        field.put("description", description);
        return field;
    }

    /**
     * 转换为DTO
     */
    private TenantFieldUploadDTO convertToDTO(TenantFieldUpload entity) {
        TenantFieldUploadDTO dto = new TenantFieldUploadDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setTenantName(getTenantName(entity.getTenantId()));
        return dto;
    }

    /**
     * 获取甲方名称（TODO: 从租户服务获取）
     */
    private String getTenantName(String tenantId) {
        // TODO: 从租户服务获取
        return "甲方" + tenantId;
    }

    /**
     * 获取上传人姓名（TODO: 从用户服务获取）
     */
    private String getUploadedByName(String uploadedBy) {
        // TODO: 从用户服务获取
        if ("admin".equals(uploadedBy)) {
            return "管理员";
        }
        return uploadedBy;
    }
}
