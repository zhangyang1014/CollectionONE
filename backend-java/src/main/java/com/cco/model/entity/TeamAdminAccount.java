package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 小组管理员账号表
 * 根据PRD要求，小组管理员可以管理小组下的催员
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("team_admin_accounts")
public class TeamAdminAccount extends BaseEntity {

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
     * 所属小组群ID（可选）
     */
    private Long teamGroupId;

    /**
     * 所属小组ID（可选，SPV可以不关联小组）
     */
    private Long teamId;

    /**
     * 账号编码
     */
    private String accountCode;

    /**
     * 账号名称
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
     * 角色：spv/team_leader/quality_inspector/statistician
     */
    private String role;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 最近登录时间
     */
    private LocalDateTime lastLoginAt;
}

