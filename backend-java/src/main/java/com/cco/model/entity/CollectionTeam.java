package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 催收小组表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("collection_teams")
public class CollectionTeam extends BaseEntity {

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
     * 所属小组群ID
     */
    private Long teamGroupId;

    /**
     * 关联的催收队列ID（必选）
     */
    private Long queueId;

    /**
     * 小组编码
     */
    private String teamCode;

    /**
     * 小组名称
     */
    private String teamName;

    /**
     * 小组名称（英文）
     */
    private String teamNameEn;

    /**
     * 组长ID（催员ID）
     */
    private Long teamLeaderId;

    /**
     * 小组类型（如：电催组、外访组、法务组等）
     */
    private String teamType;

    /**
     * 小组描述
     */
    private String description;

    /**
     * 最大案件数量（0表示不限制）
     */
    private Integer maxCaseCount;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Boolean isActive;

}

