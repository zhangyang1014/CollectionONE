package com.cco.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 甲方字段上传记录DTO
 *
 * @author CCO Team
 * @since 2025-12-08
 */
@Data
public class TenantFieldUploadDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 甲方ID
     */
    private String tenantId;

    /**
     * 甲方名称
     */
    private String tenantName;

    /**
     * 场景
     */
    private String scene;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Integer fileSize;

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
     * 是否为当前生效版本
     */
    private Boolean isActive;

    /**
     * 版本说明
     */
    private String versionNote;

    /**
     * 字段列表（仅在需要时返回）
     */
    private List<Map<String, Object>> fields;
}



