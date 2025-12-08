package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * 案件详情自定义字段（独立存储，文件模拟数据库）
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/detail-custom-fields")
public class DetailCustomFieldController {

    private static final String STORAGE_DIR = System.getProperty("user.home") + "/.cco-storage";

    private String path(Long tenantId) {
        return STORAGE_DIR + "/detail-custom-fields_" + tenantId + ".json";
    }

    @GetMapping
    public ResponseData<List<Map<String, Object>>> list(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long fieldGroupId,
            @RequestParam(required = false) Boolean isActive) {
        Long tid = tenantId != null ? tenantId : 1L;
        List<Map<String, Object>> data = load(tid);
        if (data == null) {
            data = mock(tid);
            save(tid, data);
        }
        if (fieldGroupId != null) data.removeIf(f -> !fieldGroupId.equals(f.get("field_group_id")));
        if (isActive != null) data.removeIf(f -> !isActive.equals(f.get("is_active")));
        return ResponseData.success(data);
    }

    @PostMapping
    public ResponseData<Map<String, Object>> create(@RequestBody Map<String, Object> req) {
        Long tid = asLong(req.get("tenant_id"), 1L);
        List<Map<String, Object>> data = load(tid);
        if (data == null) data = mock(tid);
        req.putIfAbsent("id", System.currentTimeMillis());
        data.add(req);
        save(tid, data);
        return ResponseData.success(req);
    }

    @PutMapping("/{id}")
    public ResponseData<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        Long tid = asLong(req.get("tenant_id"), 1L);
        List<Map<String, Object>> data = load(tid);
        if (data == null) data = mock(tid);
        for (int i = 0; i < data.size(); i++) {
            if (id.equals(asLong(data.get(i).get("id"), null))) {
                data.set(i, req);
                save(tid, data);
                return ResponseData.success(req);
            }
        }
        return ResponseData.error("not found");
    }

    @DeleteMapping("/{id}")
    public ResponseData<String> delete(@PathVariable Long id, @RequestParam(required = false) Long tenantId) {
        Long tid = tenantId != null ? tenantId : 1L;
        List<Map<String, Object>> data = load(tid);
        if (data == null) data = new ArrayList<>();
        data.removeIf(f -> id.equals(asLong(f.get("id"), null)));
        save(tid, data);
        return ResponseData.success("ok");
    }

    private List<Map<String, Object>> load(Long tenantId) {
        try {
            File f = new File(path(tenantId));
            if (!f.exists()) return null;
            String s = new String(Files.readAllBytes(Paths.get(f.toURI())));
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            return om.readValue(s, new com.fasterxml.jackson.core.type.TypeReference<>() {});
        } catch (Exception e) {
            log.warn("load detail-custom-fields failed: {}", e.getMessage());
            return null;
        }
    }

    private void save(Long tenantId, List<Map<String, Object>> data) {
        try {
            File dir = new File(STORAGE_DIR);
            if (!dir.exists()) dir.mkdirs();
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            String content = om.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            try (FileWriter fw = new FileWriter(path(tenantId))) {
                fw.write(content);
            }
        } catch (Exception e) {
            log.error("save detail-custom-fields failed", e);
        }
    }

    /**
     * 与列表自定义字段保持一致的初始数据
     */
    private List<Map<String, Object>> mock(Long tenantId) {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(custom(1L, tenantId, "d_custom_field_1", "详情自定义字段1", "text", 1L, 1));
        list.add(custom(2L, tenantId, "d_custom_field_2", "详情自定义字段2", "number", 1L, 2));
        list.add(custom(3L, tenantId, "d_custom_field_3", "详情自定义字段3", "date", 2L, 3));
        return list;
    }

    private Map<String, Object> custom(Long id, Long tid, String key, String name, String type, Long groupId, int order) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("tenant_id", tid);
        m.put("field_key", key);
        m.put("field_name", name);
        m.put("field_type", type);
        m.put("field_group_id", groupId);
        m.put("is_required", false);
        m.put("is_active", true);
        m.put("sort_order", order);
        return m;
    }

    private Long asLong(Object v, Long def) {
        if (v == null) return def;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.valueOf(v.toString()); } catch (Exception e) { return def; }
    }
}

