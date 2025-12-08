package com.cco.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cco.mapper.TenantFieldDisplayConfigMapper;
import com.cco.model.dto.FieldDisplayConfigDTO;
import com.cco.model.entity.TenantFieldDisplayConfig;
import com.cco.service.CustomFieldService;
import com.cco.service.FieldDisplayConfigService;
import com.cco.service.IStandardFieldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FieldDisplayConfigServiceImpl implements FieldDisplayConfigService {

    private final TenantFieldDisplayConfigMapper mapper;
    private final IStandardFieldService standardFieldService;
    private final CustomFieldService customFieldService;

    public FieldDisplayConfigServiceImpl(TenantFieldDisplayConfigMapper mapper,
                                         IStandardFieldService standardFieldService,
                                         CustomFieldService customFieldService) {
        this.mapper = mapper;
        this.standardFieldService = standardFieldService;
        this.customFieldService = customFieldService;
    }

    @Override
    public List<TenantFieldDisplayConfig> list(Long tenantId, String sceneType, String fieldKey) {
        LambdaQueryWrapper<TenantFieldDisplayConfig> wrapper = new LambdaQueryWrapper<>();

        if (tenantId != null) {
            wrapper.eq(TenantFieldDisplayConfig::getTenantId, tenantId);
        }
        if (sceneType != null && !sceneType.isEmpty()) {
            wrapper.eq(TenantFieldDisplayConfig::getSceneType, sceneType);
        }
        if (fieldKey != null && !fieldKey.isEmpty()) {
            wrapper.eq(TenantFieldDisplayConfig::getFieldKey, fieldKey);
        }

        wrapper.orderByAsc(TenantFieldDisplayConfig::getSortOrder);

        return mapper.selectList(wrapper);
    }

    @Override
    public TenantFieldDisplayConfig getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    @Transactional
    public TenantFieldDisplayConfig create(FieldDisplayConfigDTO.Create dto) {
        TenantFieldDisplayConfig entity = new TenantFieldDisplayConfig();
        BeanUtils.copyProperties(dto, entity);
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        entity.setDisplayWidth(dto.getDisplayWidth() != null ? dto.getDisplayWidth() : 0);
        entity.setColorType(dto.getColorType() != null ? dto.getColorType() : "normal");
        entity.setIsSearchable(dto.getIsSearchable() != null ? dto.getIsSearchable() : false);
        entity.setIsFilterable(dto.getIsFilterable() != null ? dto.getIsFilterable() : false);
        entity.setIsRangeSearchable(dto.getIsRangeSearchable() != null ? dto.getIsRangeSearchable() : false);
        mapper.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public TenantFieldDisplayConfig update(Long id, FieldDisplayConfigDTO.Update dto) {
        TenantFieldDisplayConfig entity = mapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("配置不存在，ID: " + id);
        }

        if (dto.getFieldName() != null) entity.setFieldName(dto.getFieldName());
        if (dto.getSortOrder() != null) entity.setSortOrder(dto.getSortOrder());
        if (dto.getDisplayWidth() != null) entity.setDisplayWidth(dto.getDisplayWidth());
        if (dto.getColorType() != null) entity.setColorType(dto.getColorType());
        if (dto.getColorRule() != null) entity.setColorRule(dto.getColorRule());
        if (dto.getHideRule() != null) entity.setHideRule(dto.getHideRule());
        if (dto.getHideForQueues() != null) entity.setHideForQueues(dto.getHideForQueues());
        if (dto.getHideForAgencies() != null) entity.setHideForAgencies(dto.getHideForAgencies());
        if (dto.getHideForTeams() != null) entity.setHideForTeams(dto.getHideForTeams());
        if (dto.getFormatRule() != null) entity.setFormatRule(dto.getFormatRule());
        if (dto.getIsSearchable() != null) entity.setIsSearchable(dto.getIsSearchable());
        if (dto.getIsFilterable() != null) entity.setIsFilterable(dto.getIsFilterable());
        if (dto.getIsRangeSearchable() != null) entity.setIsRangeSearchable(dto.getIsRangeSearchable());
        if (dto.getUpdatedBy() != null) entity.setUpdatedBy(dto.getUpdatedBy());

        mapper.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void batchUpdate(FieldDisplayConfigDTO.BatchUpdate dto) {
        for (FieldDisplayConfigDTO.ConfigUpdate configUpdate : dto.getConfigs()) {
            LambdaUpdateWrapper<TenantFieldDisplayConfig> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(TenantFieldDisplayConfig::getId, configUpdate.getId());

            if (configUpdate.getSortOrder() != null) {
                wrapper.set(TenantFieldDisplayConfig::getSortOrder, configUpdate.getSortOrder());
            }
            if (configUpdate.getDisplayWidth() != null) {
                wrapper.set(TenantFieldDisplayConfig::getDisplayWidth, configUpdate.getDisplayWidth());
            }
            if (configUpdate.getColorType() != null) {
                wrapper.set(TenantFieldDisplayConfig::getColorType, configUpdate.getColorType());
            }
            if (configUpdate.getIsSearchable() != null) {
                wrapper.set(TenantFieldDisplayConfig::getIsSearchable, configUpdate.getIsSearchable());
            }
            if (configUpdate.getIsFilterable() != null) {
                wrapper.set(TenantFieldDisplayConfig::getIsFilterable, configUpdate.getIsFilterable());
            }
            if (configUpdate.getIsRangeSearchable() != null) {
                wrapper.set(TenantFieldDisplayConfig::getIsRangeSearchable, configUpdate.getIsRangeSearchable());
            }

            mapper.update(null, wrapper);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public List<FieldDisplayConfigDTO.AvailableField> getAvailableFields(Long tenantId) {
        List<FieldDisplayConfigDTO.AvailableField> fields = new ArrayList<>();

        // 标准字段
        if (standardFieldService != null) {
            try {
                List<com.cco.model.entity.StandardField> standardFields = standardFieldService.listActiveFields();
                for (com.cco.model.entity.StandardField field : standardFields) {
                    fields.add(FieldDisplayConfigDTO.AvailableField.builder()
                            .fieldKey(field.getFieldKey())
                            .fieldName(field.getFieldName())
                            .fieldType(field.getFieldType())
                            .fieldGroupId(field.getFieldGroupId())
                            .fieldSource("standard")
                            .build());
                }
            } catch (Exception e) {
                log.warn("获取标准字段失败: {}", e.getMessage());
            }
        }

        // 自定义字段
        if (customFieldService != null && tenantId != null) {
            try {
                List<com.cco.model.entity.CustomField> customFields = customFieldService.listByTenantId(tenantId);
                for (com.cco.model.entity.CustomField field : customFields) {
                    fields.add(FieldDisplayConfigDTO.AvailableField.builder()
                            .fieldKey(field.getFieldKey())
                            .fieldName(field.getFieldName())
                            .fieldType(field.getFieldType())
                            .fieldGroupId(field.getFieldGroupId())
                            .fieldSource("custom")
                            .build());
                }
            } catch (Exception e) {
                log.warn("获取自定义字段失败: {}", e.getMessage());
            }
        }

        return fields;
    }
}

