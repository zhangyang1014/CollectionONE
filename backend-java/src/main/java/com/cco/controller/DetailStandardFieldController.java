package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.entity.StandardField;
import com.cco.service.StandardFieldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 案件详情标准字段管理（独立存储，文件模拟数据库）
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/detail-standard-fields")
@RequiredArgsConstructor
public class DetailStandardFieldController {

    private static final String STORAGE_DIR = System.getProperty("user.home") + "/.cco-storage";
    private final StandardFieldService standardFieldService;

    private String getFilePath(Long tenantId) {
        return STORAGE_DIR + "/detail-standard-fields_" + tenantId + ".json";
    }

    @GetMapping
    public ResponseData<List<Map<String, Object>>> list(@RequestParam(required = false) Long tenantId) {
        Long tid = tenantId != null ? tenantId : 1L;
        List<Map<String, Object>> list = load(tid);
        if (list == null) {
            // 优先同步列表标准字段
            list = fromStandardService(tid);
            if (list == null || list.isEmpty()) {
                list = mock(tid);
            }
            save(tid, list);
        }
        return ResponseData.success(list);
    }

    @PostMapping
    public ResponseData<String> create(@RequestBody Map<String, Object> req) {
        Long tid = asLong(req.get("tenant_id"), 1L);
        List<Map<String, Object>> list = load(tid);
        if (list == null) list = mock(tid);
        req.putIfAbsent("id", System.currentTimeMillis());
        list.add(req);
        save(tid, list);
        return ResponseData.success("ok");
    }

    /**
     * 从标准字段服务同步（列表侧数据）
     */
    private List<Map<String, Object>> fromStandardService(Long tenantId) {
        try {
            List<StandardField> fields = standardFieldService.listActive();
            List<Map<String, Object>> list = new ArrayList<>();
            int idx = 1;
            for (StandardField f : fields) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", f.getId() != null ? f.getId() : idx);
                m.put("tenant_id", tenantId);
                m.put("field_key", f.getFieldKey());
                m.put("field_name", f.getFieldName());
                m.put("field_type", f.getFieldType());
                m.put("sort_order", f.getSortOrder() != null ? f.getSortOrder() : idx);
                m.put("is_active", f.getIsActive() == null ? true : f.getIsActive());
                list.add(m);
                idx++;
            }
            log.info("同步列表标准字段到详情，数量={}", list.size());
            return list;
        } catch (Exception e) {
            log.warn("同步标准字段失败，使用mock，原因: {}", e.getMessage());
            return null;
        }
    }

    @PutMapping("/{id}")
    public ResponseData<String> update(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        Long tid = asLong(req.get("tenant_id"), 1L);
        List<Map<String, Object>> list = load(tid);
        if (list == null) list = mock(tid);
        for (int i = 0; i < list.size(); i++) {
            if (id.equals(asLong(list.get(i).get("id"), null))) {
                list.set(i, req);
                save(tid, list);
                return ResponseData.success("ok");
            }
        }
        return ResponseData.error("not found");
    }

    @DeleteMapping("/{id}")
    public ResponseData<String> delete(@PathVariable Long id, @RequestParam(required = false) Long tenantId) {
        Long tid = tenantId != null ? tenantId : 1L;
        List<Map<String, Object>> list = load(tid);
        if (list == null) list = new ArrayList<>();
        list.removeIf(m -> id.equals(asLong(m.get("id"), null)));
        save(tid, list);
        return ResponseData.success("ok");
    }

    private List<Map<String, Object>> load(Long tenantId) {
        try {
            File f = new File(getFilePath(tenantId));
            if (!f.exists()) return null;
            String s = new String(Files.readAllBytes(Paths.get(f.toURI())));
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            return om.readValue(s, new com.fasterxml.jackson.core.type.TypeReference<>() {});
        } catch (Exception e) {
            log.warn("load detail-standard-fields failed: {}", e.getMessage());
            return null;
        }
    }

    private void save(Long tenantId, List<Map<String, Object>> data) {
        try {
            File dir = new File(STORAGE_DIR);
            if (!dir.exists()) dir.mkdirs();
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            String content = om.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            try (FileWriter fw = new FileWriter(getFilePath(tenantId))) {
                fw.write(content);
            }
        } catch (Exception e) {
            log.error("save detail-standard-fields failed", e);
        }
    }

    private List<Map<String, Object>> mock(Long tenantId) {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(field(1L, tenantId, "detail_case_code", "案件编号", "String", 1));
        list.add(field(2L, tenantId, "detail_user_name", "客户姓名", "String", 2));
        list.add(field(3L, tenantId, "detail_mobile", "手机号", "String", 3));
        list.add(field(4L, tenantId, "detail_id_card", "身份证号", "String", 4));
        list.add(field(5L, tenantId, "detail_outstanding_amount", "未还金额", "Decimal", 5));
        return list;
    }

    private Map<String, Object> field(Long id, Long tenantId, String key, String name, String type, int order) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("tenant_id", tenantId);
        m.put("field_key", key);
        m.put("field_name", name);
        m.put("field_type", type);
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

