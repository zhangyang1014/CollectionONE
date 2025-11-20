package com.cco.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cco.model.entity.StandardField;

import java.util.List;

/**
 * 标准字段服务接口
 */
public interface IStandardFieldService extends IService<StandardField> {
    
    /**
     * 获取所有活跃的标准字段（按排序顺序）
     */
    List<StandardField> listActiveFields();
    
    /**
     * 根据字段分组获取标准字段
     */
    List<StandardField> listByGroupId(Long groupId);
    
    /**
     * 更新字段排序
     */
    boolean updateFieldOrder(List<Long> fieldIds);
    
}

