package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 案件分配记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("case_assignments")
public class CaseAssignment extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 案件ID
     */
    private Long caseId;

    /**
     * 催员ID
     */
    private Long collectorId;

    /**
     * 分配人ID
     */
    private Long assignedBy;

    /**
     * 分配时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignedAt;

    /**
     * 是否忽略队列限制
     */
    private Boolean ignoreQueueLimit;
}























