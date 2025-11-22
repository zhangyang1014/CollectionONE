package com.cco.model.dto.permission;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色权限配置响应DTO
 * 对应Python: RolePermissionConfigResponse
 */
@Data
public class RolePermissionConfigDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 甲方ID
     */
    private Long tenantId;

    /**
     * 角色代码
     */
    private String roleCode;

    /**
     * 权限项ID
     */
    private Long permissionItemId;

    /**
     * 权限级别
     */
    private String permissionLevel;

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

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 更新人ID
     */
    private Long updatedBy;

}

