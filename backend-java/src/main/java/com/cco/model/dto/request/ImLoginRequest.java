package com.cco.model.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * IM端登录请求DTO
 */
@Data
public class ImLoginRequest {

    /**
     * 机构ID（租户ID）
     */
    @NotBlank(message = "机构ID不能为空")
    private String tenantId;

    /**
     * 催员ID（登录ID）
     */
    @NotBlank(message = "催员ID不能为空")
    private String collectorId;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

}














