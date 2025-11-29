package com.cco.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cco.model.entity.Tenant;

import java.util.List;

/**
 * 甲方服务接口
 * 
 * @author CCO Team
 * @since 2025-01-11
 */
public interface TenantService extends IService<Tenant> {
    
    /**
     * 检查甲方编码是否已存在
     * 
     * @param tenantCode 甲方编码
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByTenantCode(String tenantCode, Long excludeId);
    
    /**
     * 根据关键词搜索甲方
     * 
     * @param keyword 关键词（搜索名称或编码）
     * @param isActive 是否启用（可选）
     * @return 甲方列表
     */
    List<Tenant> searchTenants(String keyword, Boolean isActive);
}

