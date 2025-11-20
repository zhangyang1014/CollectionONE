package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 催员表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "collectors", autoResultMap = true)
public class Collector extends BaseEntity {

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
     * 所属机构ID
     */
    private Long agencyId;

    /**
     * 所属小组ID
     */
    private Long teamId;

    /**
     * 催员编码
     */
    private String collectorCode;

    /**
     * 催员姓名
     */
    private String collectorName;

    /**
     * 登录ID
     */
    private String loginId;

    /**
     * 密码哈希
     */
    private String passwordHash;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 工号
     */
    private String employeeNo;

    /**
     * 催员等级（初级/中级/高级/资深）
     */
    private String collectorLevel;

    /**
     * 最大案件数量
     */
    private Integer maxCaseCount;

    /**
     * 当前案件数量
     */
    private Integer currentCaseCount;

    /**
     * 擅长领域（JSON数组，如：['高额案件','法务处理']）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> specialties;

    /**
     * 绩效评分
     */
    private BigDecimal performanceScore;

    /**
     * 状态：active/休假/离职等
     */
    private String status;

    /**
     * 入职日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;

}

