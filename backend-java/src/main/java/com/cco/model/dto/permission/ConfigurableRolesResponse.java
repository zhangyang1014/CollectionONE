package com.cco.model.dto.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 可配置角色列表响应DTO
 * 对应Python: ConfigurableRolesResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurableRolesResponse {

    /**
     * 当前角色
     */
    private String currentRole;

    /**
     * 可配置的角色列表
     */
    private List<Map<String, String>> configurableRoles;

}

