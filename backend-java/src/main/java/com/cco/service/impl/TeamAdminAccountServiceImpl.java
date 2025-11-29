package com.cco.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cco.mapper.TeamAdminAccountMapper;
import com.cco.model.entity.TeamAdminAccount;
import com.cco.service.TeamAdminAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 小组管理员账号服务实现
 * 
 * @author CCO Team
 * @since 2025-01-11
 */
@Slf4j
@Service
public class TeamAdminAccountServiceImpl extends ServiceImpl<TeamAdminAccountMapper, TeamAdminAccount> implements TeamAdminAccountService {
    
    @Override
    public TeamAdminAccount getByLoginId(String loginId) {
        LambdaQueryWrapper<TeamAdminAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamAdminAccount::getLoginId, loginId);
        return this.getOne(wrapper);
    }
    
    @Override
    public boolean existsByLoginId(String loginId, Long excludeId) {
        LambdaQueryWrapper<TeamAdminAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamAdminAccount::getLoginId, loginId);
        
        if (excludeId != null) {
            wrapper.ne(TeamAdminAccount::getId, excludeId);
        }
        
        return this.count(wrapper) > 0;
    }
    
    @Override
    public List<TeamAdminAccount> listByConditions(Long tenantId, Long agencyId, Long teamId, Boolean isActive) {
        LambdaQueryWrapper<TeamAdminAccount> wrapper = new LambdaQueryWrapper<>();
        
        if (tenantId != null) {
            wrapper.eq(TeamAdminAccount::getTenantId, tenantId);
        }
        
        if (agencyId != null) {
            wrapper.eq(TeamAdminAccount::getAgencyId, agencyId);
        }
        
        if (teamId != null) {
            wrapper.eq(TeamAdminAccount::getTeamId, teamId);
        }
        
        if (isActive != null) {
            wrapper.eq(TeamAdminAccount::getIsActive, isActive);
        }
        
        wrapper.orderByDesc(TeamAdminAccount::getCreatedAt);
        
        return this.list(wrapper);
    }
}

