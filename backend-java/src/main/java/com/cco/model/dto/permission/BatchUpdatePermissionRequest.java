package com.cco.model.dto.permission;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量更新权限配置请求DTO
 * 对应Python: BatchUpdatePermissionRequest
 */
@Data
public class BatchUpdatePermissionRequest {

    /**
     * 甲方ID，NULL表示系统默认配置
     */
    private Long tenantId;

    /**
     * 更新列表
     */
    @NotEmpty(message = "更新列表不能为空")
    @Valid
    private List<PermissionConfigUpdateDTO> updates;

}

