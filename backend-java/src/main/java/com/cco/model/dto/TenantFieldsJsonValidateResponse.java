package com.cco.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * JSON文件校验响应DTO
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantFieldsJsonValidateResponse {
    
    /**
     * 是否校验通过
     */
    private Boolean valid;
    
    /**
     * 字段数量
     */
    private Integer fieldCount;
    
    /**
     * 版本号
     */
    private String version;
    
    /**
     * 同步时间
     */
    private String syncTime;
    
    /**
     * 错误列表（校验失败时）
     */
    private List<ValidationError> errors;
    
    /**
     * 校验错误信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        /**
         * 字段路径
         */
        private String path;
        
        /**
         * 错误消息
         */
        private String message;
    }
}

