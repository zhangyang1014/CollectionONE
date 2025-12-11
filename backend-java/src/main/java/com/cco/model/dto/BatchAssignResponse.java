package com.cco.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量分案响应
 */
@Data
public class BatchAssignResponse {
    
    /**
     * 成功分配的案件数量
     */
    private Integer successCount;
    
    /**
     * 失败分配的案件数量
     */
    private Integer failureCount;
    
    /**
     * 分配详情列表
     */
    private List<AssignmentDetail> assignments;
    
    /**
     * 分配详情
     */
    @Data
    public static class AssignmentDetail {
        /**
         * 案件ID
         */
        private Long caseId;
        
        /**
         * 催员ID
         */
        private Long collectorId;
        
        /**
         * 状态：success/failure
         */
        private String status;
        
        /**
         * 错误信息（失败时）
         */
        private String errorMessage;
    }
}

























