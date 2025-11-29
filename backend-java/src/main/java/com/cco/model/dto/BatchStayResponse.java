package com.cco.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量标记停留响应
 */
@Data
public class BatchStayResponse {
    
    /**
     * 成功停留的案件数量
     */
    private Integer successCount;
    
    /**
     * 失败停留的案件数量
     */
    private Integer failureCount;
    
    /**
     * 失败详情列表
     */
    private List<FailureDetail> failures;
    
    /**
     * 失败详情
     */
    @Data
    public static class FailureDetail {
        /**
         * 案件ID
         */
        private Long caseId;
        
        /**
         * 错误信息
         */
        private String errorMessage;
    }
}

