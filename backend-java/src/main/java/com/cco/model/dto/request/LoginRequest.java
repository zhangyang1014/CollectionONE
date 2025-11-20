package com.cco.model.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {

    /**
     * 登录ID
     */
    @NotBlank(message = "登录ID不能为空")
    private String loginId;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

}

