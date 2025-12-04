package com.cco.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 队列限制检查响应
 */
@Data
public class QueueLimitCheckResponse {
    
    /**
     * 是否存在队列限制
     */
    private Boolean hasLimit;
    
    /**
     * 不匹配的项目列表
     */
    private List<UnmatchedItem> unmatchedItems;
    
    /**
     * 不匹配项
     */
    @Data
    public static class UnmatchedItem {
        /**
         * 案件ID
         */
        private Long caseId;
        
        /**
         * 案件编号
         */
        private String caseCode;
        
        /**
         * 案件队列ID
         */
        private Long caseQueueId;
        
        /**
         * 案件队列名称
         */
        private String caseQueueName;
        
        /**
         * 催员ID
         */
        private Long collectorId;
        
        /**
         * 催员名称
         */
        private String collectorName;
        
        /**
         * 催员小组队列ID
         */
        private Long collectorTeamQueueId;
        
        /**
         * 催员小组队列名称
         */
        private String collectorTeamQueueName;
    }
}









