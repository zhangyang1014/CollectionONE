package com.cco.service;

import com.cco.model.dto.FieldDisplayConfigDTO;
import com.cco.model.entity.TenantFieldDisplayConfig;

import java.util.List;

/**
 * 字段展示配置服务接口
 * 
 * @author CCO Team
 * @since 2025-11-22
 */
public interface FieldDisplayConfigService {

    /**
     * 获取配置列表
     *
     * @param tenantId 甲方ID(可选)
     * @param sceneType 场景类型(可选)
     * @param fieldKey 字段标识(可选)
     * @return 配置列表
     */
    List<TenantFieldDisplayConfig> list(Long tenantId, String sceneType, String fieldKey);

    /**
     * 根据ID获取配置
     *
     * @param id 配置ID
     * @return 配置对象
     */
    TenantFieldDisplayConfig getById(Long id);

    /**
     * 创建配置
     *
     * @param dto 创建DTO
     * @return 创建的配置对象
     */
    TenantFieldDisplayConfig create(FieldDisplayConfigDTO.Create dto);

    /**
     * 更新配置
     *
     * @param id 配置ID
     * @param dto 更新DTO
     * @return 更新后的配置对象
     */
    TenantFieldDisplayConfig update(Long id, FieldDisplayConfigDTO.Update dto);

    /**
     * 批量更新配置
     *
     * @param dto 批量更新DTO
     */
    void batchUpdate(FieldDisplayConfigDTO.BatchUpdate dto);

    /**
     * 删除配置
     *
     * @param id 配置ID
     */
    void deleteById(Long id);

    /**
     * 获取可用字段列表(用于添加配置时选择)
     *
     * @param tenantId 甲方ID(可选,用于获取自定义字段)
     * @return 可用字段列表
     */
    List<FieldDisplayConfigDTO.AvailableField> getAvailableFields(Long tenantId);
}



