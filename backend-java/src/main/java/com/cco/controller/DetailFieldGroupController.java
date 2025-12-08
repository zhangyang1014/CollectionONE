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
 * 案件详情字段分组管理（独立存储，文件模拟数据库）
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/detail-field-groups")
public class DetailFieldGroupController {

    private static final String STORAGE_DIR = System.getProperty("user.home") + "/.cco-storage";

    private String path(Long tenantId) {
        return STORAGE_DIR + "/detail-field-groups_" + tenantId + ".json";
    }

    @GetMapping
    public ResponseData<List<Map<String, Object>>> list(@RequestParam(required = false) Long tenantId) {
        Long tid = tenantId != null ? tenantId : 1L;
        List<Map<String, Object>> data = load(tid);
        if (data == null) {
            data = mock(tid);
            save(tid, data);
        }
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
            log.warn("load detail-field-groups failed: {}", e.getMessage());
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
            log.error("save detail-field-groups failed", e);
        }
    }

    /**
     * 与列表字段分组保持一致的初始数据
     */
    private List<Map<String, Object>> mock(Long tenantId) {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(group(1L, tenantId, "detail_basic", "详情-基础信息", 1));
        list.add(group(2L, tenantId, "detail_finance", "详情-财务信息", 2));
        list.add(group(3L, tenantId, "detail_contact", "详情-联系方式", 3));
        return list;
    }

    private Map<String, Object> group(Long id, Long tid, String key, String name, int order) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("tenant_id", tid);
        m.put("group_key", key);
        m.put("group_name", name);
        m.put("sort_order", order);
        m.put("is_active", true);
        return m;
    }

    private Long asLong(Object v, Long def) {
        if (v == null) return def;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.valueOf(v.toString()); } catch (Exception e) { return def; }
    }
}

