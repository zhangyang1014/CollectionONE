package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 甲方管理员表
 * 根据PRD要求，甲方管理员在创建甲方时同时创建
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tenant_admins")
public class TenantAdmin extends BaseEntity {

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
     * 账号编码
     */
    private String accountCode;

    /**
     * 账号名称（管理员姓名）
     */
    private String accountName;

    /**
     * 登录ID（唯一）
     */
    private String loginId;

    /**
     * 密码哈希（BCrypt加密）
     */
    private String passwordHash;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 最近登录时间
     */
    private LocalDateTime lastLoginAt;
}

