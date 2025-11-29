package com.cco.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cco.model.entity.CaseReassignConfig;

import java.util.List;

/**
 * 案件重新分案配置服务接口
 * 
 * @author CCO Team
 * @since 2025-01-22
 */
public interface CaseReassignConfigService extends IService<CaseReassignConfig> {
    
    /**
     * 创建配置（自动设置生效日期为T+1）
     * 
     * @param config 配置信息
     * @return 创建的配置
     */
    CaseReassignConfig createConfig(CaseReassignConfig config);
    
    /**
     * 更新配置
     * 
     * @param config 配置信息
     * @return 更新后的配置
     */
    CaseReassignConfig updateConfig(CaseReassignConfig config);
    
    /**
     * 删除配置
     * 
     * @param id 配置ID
     */
    void deleteConfig(Long id);
    
    /**
     * 查询配置列表
     * 
     * @param tenantId 甲方ID（可选）
     * @param configType 配置类型（可选）
     * @return 配置列表
     */
    List<CaseReassignConfig> listConfigs(Long tenantId, String configType);
    
    /**
     * 获取已生效的配置列表
     * 
     * @param tenantId 甲方ID（可选）
     * @return 已生效的配置列表
     */
    List<CaseReassignConfig> getEffectiveConfigs(Long tenantId);
    
    /**
     * 检查队列-小组维度是否有重复配置
     * 
     * @param tenantId 甲方ID
     * @param queueId 队列ID
     * @param teamIds 小组ID列表
     * @return 冲突的配置列表
     */
    List<CaseReassignConfig> checkConflictConfigs(Long tenantId, Long queueId, List<Long> teamIds);
    
    /**
     * 删除冲突的配置（用于替换）
     * 
     * @param tenantId 甲方ID
     * @param queueId 队列ID
     * @param teamIds 小组ID列表
     */
    void deleteConflictConfigs(Long tenantId, Long queueId, List<Long> teamIds);
}

