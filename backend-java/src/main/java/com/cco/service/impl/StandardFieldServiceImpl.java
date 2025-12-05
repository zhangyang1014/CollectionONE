package com.cco.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cco.mapper.StandardFieldMapper;
import com.cco.model.entity.StandardField;
import com.cco.service.StandardFieldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标准字段服务实现
 * 
 * @author CCO Team
 * @since 2025-12-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StandardFieldServiceImpl implements StandardFieldService {
    
    private final StandardFieldMapper standardFieldMapper;
    
    @Override
    public List<StandardField> listActive() {
        log.info("获取所有启用的标准字段");
        QueryWrapper<StandardField> wrapper = new QueryWrapper<>();
        wrapper.eq("is_active", true)
               .eq("is_deleted", false)
               .orderByAsc("field_group_id", "sort_order");
        return standardFieldMapper.selectList(wrapper);
    }
    
    @Override
    public List<StandardField> listByGroupId(Long groupId) {
        log.info("根据分组ID获取标准字段，groupId={}", groupId);
        QueryWrapper<StandardField> wrapper = new QueryWrapper<>();
        wrapper.eq("field_group_id", groupId)
               .eq("is_active", true)
               .eq("is_deleted", false)
               .orderByAsc("sort_order");
        return standardFieldMapper.selectList(wrapper);
    }
    
    @Override
    public StandardField getByFieldKey(String fieldKey) {
        log.info("根据字段Key获取标准字段，fieldKey={}", fieldKey);
        QueryWrapper<StandardField> wrapper = new QueryWrapper<>();
        wrapper.eq("field_key", fieldKey)
               .eq("is_active", true)
               .eq("is_deleted", false);
        return standardFieldMapper.selectOne(wrapper);
    }
}
