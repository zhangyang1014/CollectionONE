package com.cco.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cco.mapper.TenantMapper;
import com.cco.model.entity.Tenant;
import com.cco.service.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 甲方服务实现
 * 
 * @author CCO Team
 * @since 2025-01-11
 */
@Slf4j
@Service
public class TenantServiceImpl extends ServiceImpl<TenantMapper, Tenant> implements TenantService {
    
    @Override
    public boolean existsByTenantCode(String tenantCode, Long excludeId) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tenant::getTenantCode, tenantCode);
        
        if (excludeId != null) {
            wrapper.ne(Tenant::getId, excludeId);
        }
        
        return this.count(wrapper) > 0;
    }
    
    @Override
    public List<Tenant> searchTenants(String keyword, Boolean isActive) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Tenant::getTenantName, keyword)
                    .or()
                    .like(Tenant::getTenantCode, keyword));
        }
        
        if (isActive != null) {
            wrapper.eq(Tenant::getIsActive, isActive);
        }
        
        wrapper.orderByDesc(Tenant::getCreatedAt);
        
        return this.list(wrapper);
    }
}

