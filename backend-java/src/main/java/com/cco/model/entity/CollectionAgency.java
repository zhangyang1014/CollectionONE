package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 催收机构表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("collection_agencies")
public class CollectionAgency extends BaseEntity {

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
     * 机构编码
     */
    private String agencyCode;

    /**
     * 机构名称
     */
    private String agencyName;

    /**
     * 机构名称（英文）
     */
    private String agencyNameEn;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 机构地址
     */
    private String address;

    /**
     * 机构描述
     */
    private String description;

    /**
     * 时区偏移量（UTC偏移，范围-12到+14）
     */
    private Integer timezone;

    /**
     * 机构类型：real=真实机构，virtual=虚拟机构
     */
    private String agencyType;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Boolean isActive;

}

