package com.cco.service;

import com.cco.model.entity.CustomField;
import java.util.List;

/**
 * 自定义字段服务接口
 */
public interface CustomFieldService {
    
    /**
     * 根据甲方ID获取自定义字段列表
     */
    List<CustomField> listByTenantId(Long tenantId);
    
    /**
     * 根据甲方ID和字段Key获取自定义字段
     */
    CustomField getByTenantIdAndFieldKey(Long tenantId, String fieldKey);
}


