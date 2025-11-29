package com.cco.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cco.model.entity.TenantFieldsJson;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 甲方字段JSON版本Mapper
 * 注意：使用MapperConfig手动注册，不使用@Mapper注解
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
public interface TenantFieldsJsonMapper extends BaseMapper<TenantFieldsJson> {
    
    /**
     * 获取当前版本的JSON数据
     * 
     * @param tenantId 甲方ID
     * @return 当前版本的JSON数据
     */
    TenantFieldsJson selectCurrentVersion(@Param("tenantId") Long tenantId);
    
    /**
     * 获取历史版本列表（不包括当前版本）
     * 
     * @param tenantId 甲方ID
     * @return 历史版本列表
     */
    List<TenantFieldsJson> selectHistoryVersions(@Param("tenantId") Long tenantId);
    
    /**
     * 将当前版本标记为历史版本
     * 
     * @param tenantId 甲方ID
     * @return 更新的记录数
     */
    int updateCurrentToHistory(@Param("tenantId") Long tenantId);
    
    /**
     * 删除最旧的历史版本（只保留1个历史版本）
     * 
     * @param tenantId 甲方ID
     * @return 删除的记录数
     */
    int deleteOldestHistoryVersion(@Param("tenantId") Long tenantId);
}

