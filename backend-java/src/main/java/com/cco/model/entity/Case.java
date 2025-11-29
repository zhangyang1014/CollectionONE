package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 案件主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cases")
public class Case extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 案件唯一标识
     */
    private String caseCode;

    /**
     * 所属甲方ID
     */
    private Long tenantId;

    /**
     * 所属催收机构ID
     */
    private Long agencyId;

    /**
     * 所属催收小组ID
     */
    private Long teamId;

    /**
     * 分配催员ID
     */
    private Long collectorId;

    /**
     * 所属队列ID
     */
    private Long queueId;

    /**
     * 用户编号
     */
    private String userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 案件状态：pending_repayment/partial_repayment/normal_settlement/extension_settlement
     */
    private String caseStatus;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * App名称
     */
    private String appName;

    /**
     * 逾期天数（用于自动分配队列）
     */
    private Integer overdueDays;

    /**
     * 贷款金额
     */
    private BigDecimal loanAmount;

    /**
     * 已还款金额
     */
    private BigDecimal repaidAmount;

    /**
     * 逾期金额
     */
    private BigDecimal outstandingAmount;

    /**
     * 到期日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueDate;

    /**
     * 结清日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime settlementDate;

    /**
     * 分配时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignedAt;

    /**
     * 最后联系时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastContactAt;

    /**
     * 下次跟进时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextFollowUpAt;

    /**
     * 是否停留（独立状态字段，与case_status分离）
     */
    private Boolean isStay;

    /**
     * 停留时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime stayAt;

    /**
     * 停留操作人ID
     */
    private Long stayBy;

    /**
     * 解放停留时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime stayReleasedAt;

    /**
     * 解放停留操作人ID
     */
    private Long stayReleasedBy;

}

