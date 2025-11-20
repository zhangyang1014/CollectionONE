package com.cco.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cco.model.entity.StandardField;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标准字段Mapper
 * 对应Python: app/models/standard_field.py
 */
@Mapper
public interface StandardFieldMapper extends BaseMapper<StandardField> {
    
}

