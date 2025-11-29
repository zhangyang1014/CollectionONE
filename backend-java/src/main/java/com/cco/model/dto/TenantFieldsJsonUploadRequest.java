package com.cco.model.dto;

import lombok.Data;

/**
 * JSON文件上传请求DTO
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Data
public class TenantFieldsJsonUploadRequest {
    
    /**
     * 版本号
     */
    private String version;
    
    /**
     * 同步时间（ISO8601格式）
     */
    private String syncTime;
    
    /**
     * 字段数组
     */
    private Object fields;
}

