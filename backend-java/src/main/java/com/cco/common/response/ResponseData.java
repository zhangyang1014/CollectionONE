package com.cco.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应数据封装类
 * 用于保持与前端 API 接口的兼容性
 * 
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseData<T> {
    
    /**
     * 响应状态码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 成功响应（无数据）
     */
    public static <T> ResponseData<T> success() {
        return new ResponseData<>(200, "success", null);
    }
    
    /**
     * 成功响应（带数据）
     */
    public static <T> ResponseData<T> success(T data) {
        return new ResponseData<>(200, "success", data);
    }
    
    /**
     * 成功响应（带消息和数据）
     */
    public static <T> ResponseData<T> success(String message, T data) {
        return new ResponseData<>(200, message, data);
    }
    
    /**
     * 失败响应
     */
    public static <T> ResponseData<T> error(String message) {
        return new ResponseData<>(500, message, null);
    }
    
    /**
     * 失败响应（带状态码）
     */
    public static <T> ResponseData<T> error(Integer code, String message) {
        return new ResponseData<>(code, message, null);
    }
    
    /**
     * 未授权响应
     */
    public static <T> ResponseData<T> unauthorized(String message) {
        return new ResponseData<>(401, message, null);
    }
    
    /**
     * 禁止访问响应
     */
    public static <T> ResponseData<T> forbidden(String message) {
        return new ResponseData<>(403, message, null);
    }
    
    /**
     * 资源不存在响应
     */
    public static <T> ResponseData<T> notFound(String message) {
        return new ResponseData<>(404, message, null);
    }
    
}

