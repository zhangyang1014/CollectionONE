package com.cco.model.dto.permission;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限模块响应DTO
 * 对应Python: PermissionModuleResponse
 */
@Data
public class PermissionModuleDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 模块键
     */
    private String moduleKey;

    /**
     * 模块名称
     */
    private String moduleName;

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

