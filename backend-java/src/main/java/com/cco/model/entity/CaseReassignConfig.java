package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 案件重新分案配置实体
 * 
 * @author CCO Team
 * @since 2025-01-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("case_reassign_configs")
public class CaseReassignConfig extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属甲方ID
     */
    private Long tenantId;

    /**
     * 配置类型: queue/agency/team
     */
    private String configType;

    /**
     * 目标ID（队列ID/机构ID/小组ID）
     */
    private Long targetId;

    /**
     * 小组ID列表（JSON数组），为空表示该队列下所有小组
     */
    private String teamIds;

    /**
     * 重新分案天数（整数）
     */
    private Integer reassignDays;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 生效日期（T+1日）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;
}

