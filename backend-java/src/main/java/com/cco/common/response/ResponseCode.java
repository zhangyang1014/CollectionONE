package com.cco.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResponseCode {
    
    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),
    
    /**
     * 失败
     */
    ERROR(500, "操作失败"),
    
    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权，请先登录"),
    
    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问"),
    
    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),
    
    /**
     * 参数错误
     */
    BAD_REQUEST(400, "请求参数错误"),
    
    /**
     * 登录失败
     */
    LOGIN_FAILED(401, "登录ID或密码错误"),
    
    /**
     * Token 无效
     */
    TOKEN_INVALID(401, "Token 无效或已过期"),
    
    /**
     * 数据已存在
     */
    DATA_EXISTS(400, "数据已存在"),
    
    /**
     * 数据不存在
     */
    DATA_NOT_FOUND(404, "数据不存在");
    
    private final Integer code;
    private final String message;
    
}

