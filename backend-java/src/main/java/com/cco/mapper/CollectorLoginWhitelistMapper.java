package com.cco.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cco.model.entity.CollectorLoginWhitelist;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 催员登录白名单IP配置Mapper
 * 
 * @author CCO Team
 * @since 2025-01-22
 */
public interface CollectorLoginWhitelistMapper extends BaseMapper<CollectorLoginWhitelist> {
    
    /**
     * 根据甲方ID查询所有启用的白名单IP
     * 
     * @param tenantId 甲方ID
     * @return 白名单IP列表
     */
    List<CollectorLoginWhitelist> selectEnabledWhitelistByTenantId(@Param("tenantId") Long tenantId);
    
    /**
     * 检查甲方是否启用了白名单IP登录管理
     * 
     * @param tenantId 甲方ID
     * @return 如果该甲方有任何一条记录的isEnabled=true，返回true
     */
    Boolean checkWhitelistEnabled(@Param("tenantId") Long tenantId);
}



