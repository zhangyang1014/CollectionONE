package com.cco.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量分案请求
 */
@Data
public class BatchAssignRequest {
    
    /**
     * 案件ID列表
     */
    private List<Long> caseIds;
    
    /**
     * 催员ID列表
     */
    private List<Long> collectorIds;
    
    /**
     * 是否忽略队列限制
     */
    private Boolean ignoreQueueLimit = false;
}























