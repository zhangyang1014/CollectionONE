package com.cco.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * IM端登录请求DTO
 */
@Data
public class ImLoginRequest {

    /**
     * 催员ID（登录ID）
     */
    @JsonProperty("collectorId")
    @NotBlank(message = "催员ID不能为空")
    private String collectorId;

    /**
     * 密码
     */
    @JsonProperty("password")
    @NotBlank(message = "密码不能为空")
    private String password;

    // 手动添加getter/setter以确保JSON反序列化正常工作
    public String getCollectorId() {
        return collectorId;
    }

    public void setCollectorId(String collectorId) {
        this.collectorId = collectorId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
















