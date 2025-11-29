package com.cco.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cco.mapper.TenantAdminMapper;
import com.cco.model.entity.TenantAdmin;
import com.cco.service.TenantAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 甲方管理员服务实现
 * 
 * @author CCO Team
 * @since 2025-01-11
 */
@Slf4j
@Service
public class TenantAdminServiceImpl extends ServiceImpl<TenantAdminMapper, TenantAdmin> implements TenantAdminService {
    
    @Override
    public TenantAdmin getByLoginId(String loginId) {
        LambdaQueryWrapper<TenantAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantAdmin::getLoginId, loginId);
        return this.getOne(wrapper);
    }
    
    @Override
    public boolean existsByLoginId(String loginId, Long excludeId) {
        LambdaQueryWrapper<TenantAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantAdmin::getLoginId, loginId);
        
        if (excludeId != null) {
            wrapper.ne(TenantAdmin::getId, excludeId);
        }
        
        return this.count(wrapper) > 0;
    }
    
    @Override
    public TenantAdmin getByTenantId(Long tenantId) {
        LambdaQueryWrapper<TenantAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantAdmin::getTenantId, tenantId);
        return this.getOne(wrapper);
    }
}

