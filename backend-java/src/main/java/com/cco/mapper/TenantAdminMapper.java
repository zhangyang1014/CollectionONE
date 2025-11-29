package com.cco.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cco.model.entity.TenantAdmin;
import org.apache.ibatis.annotations.Param;

/**
 * 甲方管理员Mapper
 * 注意：使用MapperConfig手动注册，不使用@Mapper注解
 * 
 * @author CCO Team
 * @since 2025-01-11
 */
public interface TenantAdminMapper extends BaseMapper<TenantAdmin> {
    
    /**
     * 根据登录ID查询管理员
     * 
     * @param loginId 登录ID
     * @return 管理员信息
     */
    TenantAdmin selectByLoginId(@Param("loginId") String loginId);
    
    /**
     * 检查登录ID是否已存在
     * 
     * @param loginId 登录ID
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByLoginId(@Param("loginId") String loginId, @Param("excludeId") Long excludeId);
}

