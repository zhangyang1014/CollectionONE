package com.cco.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cco.model.entity.TeamAdminAccount;

import java.util.List;

/**
 * 小组管理员账号服务接口
 * 
 * @author CCO Team
 * @since 2025-01-11
 */
public interface TeamAdminAccountService extends IService<TeamAdminAccount> {
    
    /**
     * 根据登录ID查询管理员
     * 
     * @param loginId 登录ID
     * @return 管理员信息
     */
    TeamAdminAccount getByLoginId(String loginId);
    
    /**
     * 检查登录ID是否已存在
     * 
     * @param loginId 登录ID
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByLoginId(String loginId, Long excludeId);
    
    /**
     * 根据条件查询管理员列表
     * 
     * @param tenantId 甲方ID（可选）
     * @param agencyId 机构ID（可选）
     * @param teamId 小组ID（可选）
     * @param isActive 是否启用（可选）
     * @return 管理员列表
     */
    List<TeamAdminAccount> listByConditions(Long tenantId, Long agencyId, Long teamId, Boolean isActive);
}

