package com.cco.model.dto;

import lombok.Data;

/**
 * 版本对比请求DTO
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Data
public class TenantFieldsJsonCompareRequest {
    
    /**
     * 新版本的JSON数据
     */
    private Object fieldsJson;
}

