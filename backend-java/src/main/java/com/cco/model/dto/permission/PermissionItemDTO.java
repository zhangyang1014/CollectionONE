package com.cco.model.dto.permission;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限项响应DTO
 * 对应Python: PermissionItemResponse
 */
@Data
public class PermissionItemDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 所属模块ID
     */
    private Long moduleId;

    /**
     * 所属模块键（关联查询）
     */
    private String moduleKey;

    /**
     * 权限项键
     */
    private String itemKey;

    /**
     * 权限项名称
     */
    private String itemName;

    /**
     * 权限说明
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

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

}

