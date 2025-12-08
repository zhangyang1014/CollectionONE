package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.dto.CaseStandardFieldVO;
import com.cco.support.CaseStandardFieldProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 案件详情标准字段管理（独立存储，文件模拟数据库）
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/detail-standard-fields")
@RequiredArgsConstructor
public class DetailStandardFieldController {

    @GetMapping
    public ResponseData<List<CaseStandardFieldVO>> list(@RequestParam(required = false) Long tenantId) {
        Long tid = tenantId != null ? tenantId : 1L;
        log.info("获取案件详情标准字段（只读），tenantId={}", tid);
        return ResponseData.success(CaseStandardFieldProvider.getCaseDetailFields());
    }

    @PostMapping
    public ResponseData<String> create(@RequestBody Map<String, Object> req) {
        log.warn("详情标准字段处于只读模式，禁止新增");
        return ResponseData.error("标准字段为统一定义，仅支持查看，禁止新增");
    }

    @PutMapping("/{id}")
    public ResponseData<String> update(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        log.warn("详情标准字段处于只读模式，禁止修改，id={}", id);
        return ResponseData.error("标准字段为统一定义，仅支持查看，禁止修改");
    }

    @DeleteMapping("/{id}")
    public ResponseData<String> delete(@PathVariable Long id, @RequestParam(required = false) Long tenantId) {
        log.warn("详情标准字段处于只读模式，禁止删除，id={}", id);
        return ResponseData.error("标准字段为统一定义，仅支持查看，禁止删除");
    }
}

