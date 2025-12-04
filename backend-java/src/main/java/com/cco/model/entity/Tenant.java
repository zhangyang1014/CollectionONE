package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 甲方配置表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tenants")
public class Tenant extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 甲方编码
     */
    private String tenantCode;

    /**
     * 甲方名称
     */
    private String tenantName;

    /**
     * 甲方名称（英文）
     */
    private String tenantNameEn;

    /**
     * 国家代码
     */
    private String countryCode;

    /**
     * 时区偏移量（UTC偏移，范围-12到+14）
     */
    private Integer timezone;

    /**
     * 货币代码
     */
    private String currencyCode;

    /**
     * 默认语言（Locale，如zh-CN, en-US）
     * 该语言将作为该甲方下所有催员的默认界面语言
     */
    private String defaultLanguage;

    /**
     * 是否启用
     */
    private Boolean isActive;

}

