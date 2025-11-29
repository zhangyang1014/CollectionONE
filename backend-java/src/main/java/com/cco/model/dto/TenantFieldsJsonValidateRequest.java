package com.cco.model.dto;

import lombok.Data;

/**
 * JSON文件校验请求DTO
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Data
public class TenantFieldsJsonValidateRequest {
    
    /**
     * JSON文件内容（已解析为Map）
     */
    private Object fieldsJson;
}

