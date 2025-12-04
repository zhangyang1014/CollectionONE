package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

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
     * 邮箱（必填，用于接收系统通知）
     */
    private String email;

    /**
     * 备注信息（如工作职责、特长、注意事项等）
     */
    private String remark;

    /**
     * 状态：active/休假/离职等
     */
    private String status;

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

