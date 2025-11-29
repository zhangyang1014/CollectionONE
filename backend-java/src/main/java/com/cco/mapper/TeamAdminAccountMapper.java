package com.cco.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cco.model.entity.TeamAdminAccount;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 小组管理员账号Mapper
 * 注意：使用MapperConfig手动注册，不使用@Mapper注解
 * 
 * @author CCO Team
 * @since 2025-01-11
 */
public interface TeamAdminAccountMapper extends BaseMapper<TeamAdminAccount> {
    
    /**
     * 根据登录ID查询管理员
     * 
     * @param loginId 登录ID
     * @return 管理员信息
     */
    TeamAdminAccount selectByLoginId(@Param("loginId") String loginId);
    
    /**
     * 检查登录ID是否已存在
     * 
     * @param loginId 登录ID
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByLoginId(@Param("loginId") String loginId, @Param("excludeId") Long excludeId);
    
    /**
     * 根据条件查询管理员列表
     * 
     * @param tenantId 甲方ID（可选）
     * @param agencyId 机构ID（可选）
     * @param teamId 小组ID（可选）
     * @param isActive 是否启用（可选）
     * @return 管理员列表
     */
    List<TeamAdminAccount> selectByConditions(
            @Param("tenantId") Long tenantId,
            @Param("agencyId") Long agencyId,
            @Param("teamId") Long teamId,
            @Param("isActive") Boolean isActive
    );
}

