package com.cco.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cco.model.entity.TenantAdmin;

/**
 * 甲方管理员服务接口
 * 
 * @author CCO Team
 * @since 2025-01-11
 */
public interface TenantAdminService extends IService<TenantAdmin> {
    
    /**
     * 根据登录ID查询管理员
     * 
     * @param loginId 登录ID
     * @return 管理员信息
     */
    TenantAdmin getByLoginId(String loginId);
    
    /**
     * 检查登录ID是否已存在
     * 
     * @param loginId 登录ID
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByLoginId(String loginId, Long excludeId);
    
    /**
     * 根据甲方ID查询管理员
     * 
     * @param tenantId 甲方ID
     * @return 管理员信息
     */
    TenantAdmin getByTenantId(Long tenantId);
}

