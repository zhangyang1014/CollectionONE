package com.cco.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {

    /**
     * 登录ID
     */
    @JsonProperty("loginId")
    @NotBlank(message = "登录ID不能为空")
    private String loginId;

    /**
     * 密码
     */
    @JsonProperty("password")
    @NotBlank(message = "密码不能为空")
    private String password;

    // 手动添加getter/setter以确保JSON反序列化正常工作
    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

