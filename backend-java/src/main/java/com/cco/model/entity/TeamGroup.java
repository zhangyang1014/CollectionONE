package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 小组群表 - 介于机构和小组之间的管理层级
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("team_groups")
public class TeamGroup extends BaseEntity {

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
     * 所属催收机构ID
     */
    private Long agencyId;

    /**
     * 小组群编码
     */
    private String groupCode;

    /**
     * 小组群名称
     */
    private String groupName;

    /**
     * 小组群名称（英文）
     */
    private String groupNameEn;

    /**
     * 小组群管理员ID
     */
    private Long adminId;

    /**
     * 小组群描述
     */
    private String description;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Boolean isActive;

}

