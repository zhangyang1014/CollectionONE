package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 甲方字段上传记录实体类
 *
 * @author CCO Team
 * @since 2025-12-08
 */
@Data
@TableName("tenant_field_uploads")
public class TenantFieldUpload {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 甲方ID
     */
    private String tenantId;

    /**
     * 场景：list-案件列表, detail-案件详情
     */
    private String scene;

    /**
     * 版本号，按甲方+场景自增
     */
    private Integer version;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Integer fileSize;

    /**
     * 存储路径
     */
    private String filePath;

    /**
     * 字段数量
     */
    private Integer fieldsCount;

    /**
     * 上传人ID
     */
    private String uploadedBy;

    /**
     * 上传人姓名
     */
    private String uploadedByName;

    /**
     * 上传时间
     */
    private LocalDateTime uploadedAt;

    /**
     * JSON文件内容
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Object jsonContent;

    /**
     * 是否为当前生效版本
     */
    private Boolean isActive;

    /**
     * 版本说明（可选）
     */
    private String versionNote;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
