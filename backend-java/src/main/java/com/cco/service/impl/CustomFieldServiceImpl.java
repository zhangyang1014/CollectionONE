package com.cco.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cco.mapper.CustomFieldMapper;
import com.cco.model.entity.CustomField;
import com.cco.service.CustomFieldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomFieldServiceImpl implements CustomFieldService {

    private final CustomFieldMapper mapper;

    @Override
    public List<CustomField> listByTenantId(Long tenantId) {
        LambdaQueryWrapper<CustomField> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomField::getTenantId, tenantId);
        wrapper.eq(CustomField::getIsDeleted, false);
        wrapper.orderByAsc(CustomField::getSortOrder);
        return mapper.selectList(wrapper);
    }

    @Override
    public CustomField getByTenantIdAndFieldKey(Long tenantId, String fieldKey) {
        LambdaQueryWrapper<CustomField> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomField::getTenantId, tenantId);
        wrapper.eq(CustomField::getFieldKey, fieldKey);
        wrapper.eq(CustomField::getIsDeleted, false);
        return mapper.selectOne(wrapper);
    }

    @Override
    @Transactional
    public CustomField save(CustomField field) {
        if (field.getId() == null) {
            mapper.insert(field);
        } else {
            mapper.updateById(field);
        }
        return field;
    }
}

