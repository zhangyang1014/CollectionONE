package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.dto.CaseStandardFieldVO;
import com.cco.model.entity.StandardField;
import com.cco.service.StandardFieldService;
import com.cco.support.CaseStandardFieldProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 标准字段管理Controller
 * 
 * @author CCO Team
 * @since 2025-12-05
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/standard-fields")
@RequiredArgsConstructor
public class StandardFieldController {

    private final StandardFieldService standardFieldService;

    /**
     * 获取标准字段列表
     * 支持按分组ID过滤
     */
    @GetMapping
    public ResponseData<List<StandardField>> getStandardFields(
            @RequestParam(required = false) Long field_group_id
    ) {
        log.info("========== 获取标准字段列表，field_group_id={} ==========", field_group_id);
        
        List<StandardField> fields;
        if (field_group_id != null) {
            // 根据分组ID过滤
            fields = standardFieldService.listByGroupId(field_group_id);
        } else {
            // 获取所有启用的字段
            fields = standardFieldService.listActive();
        }
        
        log.info("========== 返回标准字段列表，数量={} ==========", fields.size());
        return ResponseData.success(fields);
    }

    /**
     * 获取标准字段详情
     */
    @GetMapping("/{id:\\d+}")
    public ResponseData<StandardField> getStandardField(@PathVariable Long id) {
        log.info("========== 获取标准字段详情，id={} ==========", id);
        
        // 按字段Key查询（历史实现习惯），仅用于只读查看
        StandardField field = standardFieldService.getByFieldKey(String.valueOf(id));
        if (field == null) {
            return ResponseData.error("字段不存在");
        }
        
        return ResponseData.success(field);
    }

    /**
     * 创建标准字段
     */
    @PostMapping
    public ResponseData<String> createStandardField(@RequestBody StandardField standardField) {
        log.info("========== 创建标准字段，request={} ==========", standardField);
        
        // 只读模式：禁止新增，保留提示
        log.warn("标准字段处于只读模式，禁止新增");
        return ResponseData.error("标准字段为统一定义，仅支持查看，禁止新增");
    }

    /**
     * 更新标准字段
     */
    @PutMapping("/{id:\\d+}")
    public ResponseData<String> updateStandardField(
            @PathVariable Long id,
            @RequestBody StandardField standardField
    ) {
        log.info("========== 更新标准字段，id={}, request={} ==========", id, standardField);
        
        // 只读模式：禁止修改，保留提示
        log.warn("标准字段处于只读模式，禁止修改");
        
        return ResponseData.error("标准字段为统一定义，仅支持查看，禁止修改");
    }

    /**
     * 删除标准字段（软删除）
     */
    @DeleteMapping("/{id:\\d+}")
    public ResponseData<String> deleteStandardField(@PathVariable Long id) {
        log.info("========== 删除标准字段，id={} ==========", id);
        
        // 只读模式：禁止删除，保留提示
        log.warn("标准字段处于只读模式，禁止删除");
        
        return ResponseData.error("标准字段为统一定义，仅支持查看，禁止删除");
    }

    /**
     * 更新字段排序
     */
    @PutMapping("/sort")
    public ResponseData<String> updateFieldSort(@RequestBody Map<String, Object> request) {
        log.info("========== 更新字段排序，request={} ==========", request);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> fields = (List<Map<String, Object>>) request.get("fields");
        
        if (fields != null) {
            log.info("更新{}个字段的排序", fields.size());
        }
        
        // 只读模式：禁止调整排序
        log.warn("标准字段处于只读模式，禁止调整排序");
        
        return ResponseData.error("标准字段为统一定义，仅支持查看，禁止调整排序");
    }

    /**
     * 案件列表标准字段（只读）
     */
    @GetMapping("/case-list")
    public ResponseData<List<CaseStandardFieldVO>> getCaseListStandardFields() {
        log.info("获取案件列表标准字段（只读）");
        return ResponseData.success(CaseStandardFieldProvider.getCaseListFields());
    }

    /**
     * 案件详情标准字段（只读）
     */
    @GetMapping("/case-detail")
    public ResponseData<List<CaseStandardFieldVO>> getCaseDetailStandardFields() {
        log.info("获取案件详情标准字段（只读）");
        return ResponseData.success(CaseStandardFieldProvider.getCaseDetailFields());
    }

}




