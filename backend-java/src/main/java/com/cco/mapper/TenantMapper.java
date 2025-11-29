package com.cco.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cco.model.entity.Tenant;
import org.apache.ibatis.annotations.Param;

/**
 * 甲方Mapper
 * 注意：使用MapperConfig手动注册，不使用@Mapper注解
 * 
 * @author CCO Team
 * @since 2025-01-11
 */
public interface TenantMapper extends BaseMapper<Tenant> {
    
    /**
     * 检查甲方编码是否已存在
     * 
     * @param tenantCode 甲方编码
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByTenantCode(@Param("tenantCode") String tenantCode, @Param("excludeId") Long excludeId);
}

