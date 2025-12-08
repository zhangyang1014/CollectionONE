package com.cco.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cco.model.entity.FieldGroup;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FieldGroupMapper extends BaseMapper<FieldGroup> {
}
package com.cco.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cco.model.entity.FieldGroup;

/**
 * 字段分组Mapper
 * 注意：使用MapperConfig手动注册，不使用@Mapper注解
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
public interface FieldGroupMapper extends BaseMapper<FieldGroup> {
    
}

