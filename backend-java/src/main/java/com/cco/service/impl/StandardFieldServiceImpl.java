package com.cco.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cco.mapper.StandardFieldMapper;
import com.cco.model.entity.StandardField;
import com.cco.service.IStandardFieldService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 标准字段服务实现
 * 对应Python: app/api/standard_fields.py
 */
@Service
public class StandardFieldServiceImpl extends ServiceImpl<StandardFieldMapper, StandardField> 
        implements IStandardFieldService {

    @Override
    public List<StandardField> listActiveFields() {
        LambdaQueryWrapper<StandardField> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StandardField::getIsActive, true)
                .eq(StandardField::getIsDeleted, false)
                .orderByAsc(StandardField::getSortOrder);
        return list(wrapper);
    }

    @Override
    public List<StandardField> listByGroupId(Long groupId) {
        LambdaQueryWrapper<StandardField> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StandardField::getFieldGroupId, groupId)
                .eq(StandardField::getIsActive, true)
                .eq(StandardField::getIsDeleted, false)
                .orderByAsc(StandardField::getSortOrder);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFieldOrder(List<Long> fieldIds) {
        for (int i = 0; i < fieldIds.size(); i++) {
            StandardField field = new StandardField();
            field.setId(fieldIds.get(i));
            field.setSortOrder(i);
            updateById(field);
        }
        return true;
    }

}

