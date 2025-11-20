package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.entity.StandardField;
import com.cco.service.IStandardFieldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标准字段Controller
 * 对应Python: app/api/standard_fields.py
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/fields/standard")
public class StandardFieldController {

    @Autowired
    private IStandardFieldService standardFieldService;

    /**
     * 获取所有标准字段
     */
    @GetMapping
    public ResponseData<List<StandardField>> list() {
        log.info("获取所有标准字段");
        List<StandardField> fields = standardFieldService.listActiveFields();
        return ResponseData.success(fields);
    }

    /**
     * 根据分组获取标准字段
     */
    @GetMapping("/by-group/{groupId}")
    public ResponseData<List<StandardField>> listByGroup(@PathVariable Long groupId) {
        log.info("根据分组获取标准字段: groupId={}", groupId);
        List<StandardField> fields = standardFieldService.listByGroupId(groupId);
        return ResponseData.success(fields);
    }

    /**
     * 获取单个标准字段
     */
    @GetMapping("/{id}")
    public ResponseData<StandardField> getById(@PathVariable Long id) {
        log.info("获取标准字段详情: id={}", id);
        StandardField field = standardFieldService.getById(id);
        if (field == null) {
            return ResponseData.notFound("字段不存在");
        }
        return ResponseData.success(field);
    }

    /**
     * 创建标准字段
     */
    @PostMapping
    public ResponseData<StandardField> create(@RequestBody StandardField field) {
        log.info("创建标准字段: {}", field.getFieldName());
        boolean success = standardFieldService.save(field);
        if (success) {
            return ResponseData.success("创建成功", field);
        }
        return ResponseData.error("创建失败");
    }

    /**
     * 更新标准字段
     */
    @PutMapping("/{id}")
    public ResponseData<StandardField> update(@PathVariable Long id, @RequestBody StandardField field) {
        log.info("更新标准字段: id={}", id);
        field.setId(id);
        boolean success = standardFieldService.updateById(field);
        if (success) {
            return ResponseData.success("更新成功", field);
        }
        return ResponseData.error("更新失败");
    }

    /**
     * 删除标准字段（软删除）
     */
    @DeleteMapping("/{id}")
    public ResponseData<Void> delete(@PathVariable Long id) {
        log.info("删除标准字段: id={}", id);
        StandardField field = new StandardField();
        field.setId(id);
        field.setIsDeleted(true);
        boolean success = standardFieldService.updateById(field);
        if (success) {
            return ResponseData.success("删除成功", null);
        }
        return ResponseData.error("删除失败");
    }

    /**
     * 更新字段排序
     */
    @PutMapping("/order")
    public ResponseData<Void> updateOrder(@RequestBody List<Long> fieldIds) {
        log.info("更新字段排序: fieldIds={}", fieldIds);
        boolean success = standardFieldService.updateFieldOrder(fieldIds);
        if (success) {
            return ResponseData.success("排序更新成功", null);
        }
        return ResponseData.error("排序更新失败");
    }

}

