package com.cco.service;

import com.cco.model.entity.StandardField;
import java.util.List;

/**
 * 标准字段服务接口
 */
public interface StandardFieldService {
    
    /**
     * 获取所有启用的标准字段
     */
    List<StandardField> listActive();
    
    /**
     * 根据分组ID获取标准字段
     */
    List<StandardField> listByGroupId(Long groupId);
    
    /**
     * 根据字段Key获取标准字段
     */
    StandardField getByFieldKey(String fieldKey);
}


