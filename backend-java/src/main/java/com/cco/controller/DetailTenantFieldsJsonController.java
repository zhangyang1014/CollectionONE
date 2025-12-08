package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.service.TenantFieldsJsonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * 案件详情 甲方字段查看（独立存储，文件模拟数据库）
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/tenants/{tenantId}/detail-fields-json")
@RequiredArgsConstructor
public class DetailTenantFieldsJsonController {

    private static final String STORAGE_DIR = System.getProperty("user.home") + "/.cco-storage";
    private final TenantFieldsJsonService tenantFieldsJsonService;

    private String path(Long tenantId) {
        return STORAGE_DIR + "/detail-fields-json_" + tenantId + ".json";
    }

    @GetMapping
    public ResponseData<Map<String, Object>> get(@PathVariable Long tenantId) {
        Map<String, Object> data = load(tenantId);
        if (data == null) {
            // 优先复用列表甲方字段JSON
            data = tenantFieldsJsonService.getCurrentVersion(tenantId);
            if (data == null || data.isEmpty()) {
                data = new HashMap<>();
                data.put("fetched_at", new Date().toString());
                data.put("fields", mockFields(tenantId));
            }
            save(tenantId, data);
        }
        return ResponseData.success(data);
    }

    @PostMapping
    public ResponseData<Map<String, Object>> saveJson(@PathVariable Long tenantId, @RequestBody Map<String, Object> body) {
        save(tenantId, body);
        return ResponseData.success(body);
    }

    private Map<String, Object> load(Long tenantId) {
        try {
            File f = new File(path(tenantId));
            if (!f.exists()) return null;
            String s = new String(Files.readAllBytes(Paths.get(f.toURI())));
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            return om.readValue(s, new com.fasterxml.jackson.core.type.TypeReference<>() {});
        } catch (Exception e) {
            log.warn("load detail-fields-json failed: {}", e.getMessage());
            return null;
        }
    }

    private void save(Long tenantId, Map<String, Object> data) {
        try {
            File dir = new File(STORAGE_DIR);
            if (!dir.exists()) dir.mkdirs();
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            String content = om.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            try (FileWriter fw = new FileWriter(path(tenantId))) {
                fw.write(content);
            }
        } catch (Exception e) {
            log.error("save detail-fields-json failed", e);
        }
    }

    private List<Map<String, Object>> mockFields(Long tenantId) {
        List<Map<String, Object>> fields = new ArrayList<>();
        fields.add(field("detail_case_code", "案件编号", "String"));
        fields.add(field("detail_user_name", "客户姓名", "String"));
        fields.add(field("detail_mobile", "手机号", "String"));
        fields.add(field("detail_address", "地址", "String"));
        fields.add(field("detail_last_call_time", "最后通话时间", "DateTime"));
        return fields;
    }

    private Map<String, Object> field(String key, String name, String type) {
        Map<String, Object> m = new HashMap<>();
        m.put("field_key", key);
        m.put("field_name", name);
        m.put("field_type", type);
        return m;
    }
}

