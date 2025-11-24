package com.cco.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cco.mapper.TenantFieldDisplayConfigMapper;
import com.cco.model.dto.FieldDisplayConfigDTO;
import com.cco.model.entity.TenantFieldDisplayConfig;
import com.cco.service.FieldDisplayConfigService;
import com.cco.service.IStandardFieldService;
import com.cco.service.CustomFieldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 字段展示配置服务实现
 * 
 * @author CCO Team
 * @since 2025-11-22
 */
@Slf4j
@Service
public class FieldDisplayConfigServiceImpl implements FieldDisplayConfigService {
    
    public FieldDisplayConfigServiceImpl() {
        log.info("FieldDisplayConfigServiceImpl 初始化");
    }

    @Autowired(required = false)
    private TenantFieldDisplayConfigMapper mapper;

    @Autowired(required = false)
    private IStandardFieldService standardFieldService;

    @Autowired(required = false)
    private CustomFieldService customFieldService;

    @Override
    public List<TenantFieldDisplayConfig> list(Long tenantId, String sceneType, String fieldKey) {
        if (mapper == null) {
            log.warn("TenantFieldDisplayConfigMapper未注入（Mock模式），返回空列表");
            return new ArrayList<>();
        }
        
        try {
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
            
            // 按排序顺序排序
            wrapper.orderByAsc(TenantFieldDisplayConfig::getSortOrder);
            
            return mapper.selectList(wrapper);
        } catch (Exception e) {
            // 如果数据库不可用（Mock模式），返回空列表
            log.warn("数据库查询失败（可能是Mock模式），返回空列表: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public TenantFieldDisplayConfig getById(Long id) {
        try {
            return mapper.selectById(id);
        } catch (Exception e) {
            log.warn("数据库查询失败（可能是Mock模式）: {}", e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional
    public TenantFieldDisplayConfig create(FieldDisplayConfigDTO.Create dto) {
        if (mapper == null) {
            log.warn("TenantFieldDisplayConfigMapper未注入（Mock模式），返回模拟对象");
            // 返回一个模拟对象，不实际保存
            TenantFieldDisplayConfig entity = new TenantFieldDisplayConfig();
            entity.setId(System.currentTimeMillis());
            entity.setTenantId(dto.getTenantId());
            entity.setSceneType(dto.getSceneType());
            entity.setSceneName(dto.getSceneName());
            entity.setFieldKey(dto.getFieldKey());
            entity.setFieldName(dto.getFieldName());
            entity.setFieldDataType(dto.getFieldDataType());
            entity.setFieldSource(dto.getFieldSource());
            entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
            entity.setDisplayWidth(dto.getDisplayWidth() != null ? dto.getDisplayWidth() : 0);
            entity.setColorType(dto.getColorType() != null ? dto.getColorType() : "normal");
            return entity;
        }
        
        try {
            TenantFieldDisplayConfig entity = new TenantFieldDisplayConfig();
            BeanUtils.copyProperties(dto, entity);
            
            // 设置字段名映射
            entity.setTenantId(dto.getTenantId());
            entity.setSceneType(dto.getSceneType());
            entity.setSceneName(dto.getSceneName());
            entity.setFieldKey(dto.getFieldKey());
            entity.setFieldName(dto.getFieldName());
            entity.setFieldDataType(dto.getFieldDataType());
            entity.setFieldSource(dto.getFieldSource());
            entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
            entity.setDisplayWidth(dto.getDisplayWidth() != null ? dto.getDisplayWidth() : 0);
            entity.setColorType(dto.getColorType() != null ? dto.getColorType() : "normal");
            entity.setColorRule(dto.getColorRule());
            entity.setHideRule(dto.getHideRule());
            entity.setHideForQueues(dto.getHideForQueues());
            entity.setHideForAgencies(dto.getHideForAgencies());
            entity.setHideForTeams(dto.getHideForTeams());
            entity.setFormatRule(dto.getFormatRule());
            entity.setIsSearchable(dto.getIsSearchable() != null ? dto.getIsSearchable() : false);
            entity.setIsFilterable(dto.getIsFilterable() != null ? dto.getIsFilterable() : false);
            entity.setIsRangeSearchable(dto.getIsRangeSearchable() != null ? dto.getIsRangeSearchable() : false);
            entity.setCreatedBy(dto.getCreatedBy());
            
            mapper.insert(entity);
            return entity;
        } catch (Exception e) {
            log.error("创建字段展示配置失败: {}", e.getMessage());
            throw new RuntimeException("创建字段展示配置失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public TenantFieldDisplayConfig update(Long id, FieldDisplayConfigDTO.Update dto) {
        if (mapper == null) {
            log.warn("TenantFieldDisplayConfigMapper未注入（Mock模式），返回模拟对象");
            // 返回一个模拟对象，不实际更新
            TenantFieldDisplayConfig entity = new TenantFieldDisplayConfig();
            entity.setId(id);
            if (dto.getSortOrder() != null) {
                entity.setSortOrder(dto.getSortOrder());
            }
            if (dto.getDisplayWidth() != null) {
                entity.setDisplayWidth(dto.getDisplayWidth());
            }
            if (dto.getColorType() != null) {
                entity.setColorType(dto.getColorType());
            }
            return entity;
        }
        
        try {
            TenantFieldDisplayConfig entity = mapper.selectById(id);
            if (entity == null) {
                throw new RuntimeException("配置不存在，ID: " + id);
            }
            
            // 更新字段
            if (dto.getFieldName() != null) {
                entity.setFieldName(dto.getFieldName());
            }
            if (dto.getSortOrder() != null) {
                entity.setSortOrder(dto.getSortOrder());
            }
            if (dto.getDisplayWidth() != null) {
                entity.setDisplayWidth(dto.getDisplayWidth());
            }
            if (dto.getColorType() != null) {
                entity.setColorType(dto.getColorType());
            }
            if (dto.getColorRule() != null) {
                entity.setColorRule(dto.getColorRule());
            }
            if (dto.getHideRule() != null) {
                entity.setHideRule(dto.getHideRule());
            }
            if (dto.getHideForQueues() != null) {
                entity.setHideForQueues(dto.getHideForQueues());
            }
            if (dto.getHideForAgencies() != null) {
                entity.setHideForAgencies(dto.getHideForAgencies());
            }
            if (dto.getHideForTeams() != null) {
                entity.setHideForTeams(dto.getHideForTeams());
            }
            if (dto.getFormatRule() != null) {
                entity.setFormatRule(dto.getFormatRule());
            }
            if (dto.getIsSearchable() != null) {
                entity.setIsSearchable(dto.getIsSearchable());
            }
            if (dto.getIsFilterable() != null) {
                entity.setIsFilterable(dto.getIsFilterable());
            }
            if (dto.getIsRangeSearchable() != null) {
                entity.setIsRangeSearchable(dto.getIsRangeSearchable());
            }
            if (dto.getUpdatedBy() != null) {
                entity.setUpdatedBy(dto.getUpdatedBy());
            }
            
            mapper.updateById(entity);
            return entity;
        } catch (Exception e) {
            log.error("更新字段展示配置失败: {}", e.getMessage());
            throw new RuntimeException("更新字段展示配置失败: " + e.getMessage(), e);
        }
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
        
        // 获取标准字段
        if (standardFieldService != null) {
            try {
                List<com.cco.model.entity.StandardField> standardFields = standardFieldService.listActiveFields();
                for (com.cco.model.entity.StandardField field : standardFields) {
                    FieldDisplayConfigDTO.AvailableField availableField = new FieldDisplayConfigDTO.AvailableField();
                    availableField.setFieldKey(field.getFieldKey());
                    availableField.setFieldName(field.getFieldName());
                    availableField.setFieldType(field.getFieldType());
                    availableField.setFieldSource("standard");
                    availableField.setFieldGroupId(field.getFieldGroupId());
                    availableField.setIsExtended(false);
                    availableField.setDescription(field.getDescription());
                    fields.add(availableField);
                }
            } catch (Exception e) {
                // 如果标准字段服务不可用，继续处理自定义字段
                log.warn("获取标准字段失败: " + e.getMessage());
            }
        }
        
        // 获取自定义字段（如果有tenantId）
        if (tenantId != null && customFieldService != null) {
            try {
                List<com.cco.model.entity.CustomField> customFields = customFieldService.listByTenantId(tenantId);
                for (com.cco.model.entity.CustomField field : customFields) {
                    FieldDisplayConfigDTO.AvailableField availableField = new FieldDisplayConfigDTO.AvailableField();
                    availableField.setFieldKey(field.getFieldKey());
                    availableField.setFieldName(field.getFieldName());
                    availableField.setFieldType(field.getFieldType());
                    availableField.setFieldSource("custom");
                    availableField.setFieldGroupId(null);
                    availableField.setIsExtended(false);
                    availableField.setDescription(field.getDescription());
                    fields.add(availableField);
                }
            } catch (Exception e) {
                log.warn("获取自定义字段失败: " + e.getMessage());
            }
        }
        
        return fields;
    }
}

