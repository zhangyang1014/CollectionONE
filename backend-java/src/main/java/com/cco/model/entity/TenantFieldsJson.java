package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 甲方字段JSON版本表
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tenant_fields_json", autoResultMap = true)
public class TenantFieldsJson extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 甲方ID
     */
    private Long tenantId;

    /**
     * JSON版本号
     */
    private String version;

    /**
     * 同步时间（版本时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime syncTime;

    /**
     * 字段定义JSON（完整JSON）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> fieldsJson;

    /**
     * 是否当前版本（1=当前，0=历史）
     */
    private Boolean isCurrent;

    /**
     * 上传人
     */
    private String uploadedBy;

    /**
     * 上传时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadedAt;

}

