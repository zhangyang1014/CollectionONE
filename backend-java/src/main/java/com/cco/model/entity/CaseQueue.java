package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 案件队列表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("case_queues")
public class CaseQueue extends BaseEntity {

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
     * 队列编码（如：M1, M2, M3+, LEGAL）
     */
    private String queueCode;

    /**
     * 队列名称
     */
    private String queueName;

    /**
     * 队列名称（英文）
     */
    private String queueNameEn;

    /**
     * 队列描述
     */
    private String queueDescription;

    /**
     * 逾期天数起始值（null表示负无穷）
     */
    private Integer overdueDaysStart;

    /**
     * 逾期天数结束值（null表示正无穷）
     */
    private Integer overdueDaysEnd;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Boolean isActive;

}

