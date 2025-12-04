package com.cco.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cco.model.entity.CollectorLoginWhitelist;

import java.util.List;

/**
 * 催员登录白名单IP配置服务接口
 * 
 * @author CCO Team
 * @since 2025-01-22
 */
public interface CollectorLoginWhitelistService extends IService<CollectorLoginWhitelist> {
    
    /**
     * 创建白名单IP配置
     * 
     * @param whitelist 白名单IP配置信息
     * @return 创建的配置
     */
    CollectorLoginWhitelist createWhitelist(CollectorLoginWhitelist whitelist);
    
    /**
     * 更新白名单IP配置
     * 
     * @param whitelist 白名单IP配置信息
     * @return 更新后的配置
     */
    CollectorLoginWhitelist updateWhitelist(CollectorLoginWhitelist whitelist);
    
    /**
     * 删除白名单IP配置
     * 
     * @param id 配置ID
     */
    void deleteWhitelist(Long id);
    
    /**
     * 查询白名单IP配置列表
     * 
     * @param tenantId 甲方ID（必填）
     * @return 白名单IP配置列表
     */
    List<CollectorLoginWhitelist> listWhitelists(Long tenantId);
    
    /**
     * 启用/禁用甲方的白名单IP登录管理
     * 
     * @param tenantId 甲方ID
     * @param enabled 是否启用
     */
    void setWhitelistEnabled(Long tenantId, Boolean enabled);
    
    /**
     * 检查甲方是否启用了白名单IP登录管理
     * 
     * @param tenantId 甲方ID
     * @return 是否启用
     */
    Boolean isWhitelistEnabled(Long tenantId);
    
    /**
     * 检查IP地址是否在白名单中
     * 
     * @param tenantId 甲方ID
     * @param ipAddress IP地址
     * @return 是否在白名单中
     */
    Boolean isIpAllowed(Long tenantId, String ipAddress);
}



