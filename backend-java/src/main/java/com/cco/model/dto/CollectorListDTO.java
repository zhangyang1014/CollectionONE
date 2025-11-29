package com.cco.model.dto;

import lombok.Data;

/**
 * 催员列表DTO（用于分案弹窗）
 */
@Data
public class CollectorListDTO {
    
    /**
     * 催员ID
     */
    private Long id;
    
    /**
     * 催员名称
     */
    private String collectorName;
    
    /**
     * 催员编号
     */
    private String collectorCode;
    
    /**
     * 机构ID
     */
    private Long agencyId;
    
    /**
     * 机构名称
     */
    private String agencyName;
    
    /**
     * 小组ID
     */
    private Long teamId;
    
    /**
     * 小组名称
     */
    private String teamName;
    
    /**
     * 队列ID（从小组获取）
     */
    private Long queueId;
    
    /**
     * 队列名称
     */
    private String queueName;
    
    /**
     * 今日持案量
     */
    private Integer currentCaseCount;
    
    /**
     * 状态
     */
    private String status;
}



