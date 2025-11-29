package com.cco.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 队列限制检查请求
 */
@Data
public class QueueLimitCheckRequest {
    
    /**
     * 案件ID列表
     */
    private List<Long> caseIds;
    
    /**
     * 催员ID列表
     */
    private List<Long> collectorIds;
}



