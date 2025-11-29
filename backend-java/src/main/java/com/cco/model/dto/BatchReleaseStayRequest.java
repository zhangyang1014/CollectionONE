package com.cco.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量解放停留请求
 */
@Data
public class BatchReleaseStayRequest {
    
    /**
     * 案件ID列表
     */
    private List<Long> caseIds;
}

