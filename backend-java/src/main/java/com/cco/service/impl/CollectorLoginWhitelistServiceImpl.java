package com.cco.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cco.mapper.CollectorLoginWhitelistMapper;
import com.cco.model.entity.CollectorLoginWhitelist;
import com.cco.service.CollectorLoginWhitelistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 催员登录白名单IP配置服务实现
 * 
 * @author CCO Team
 * @since 2025-01-22
 */
@Slf4j
@Service
public class CollectorLoginWhitelistServiceImpl extends ServiceImpl<CollectorLoginWhitelistMapper, CollectorLoginWhitelist> 
        implements CollectorLoginWhitelistService {
    
    @Autowired
    private CollectorLoginWhitelistMapper whitelistMapper;
    
    // IPv4地址正则表达式
    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );
    
    // CIDR格式正则表达式（IPv4）
    private static final Pattern CIDR_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)/([0-9]|[12][0-9]|3[0-2])$"
    );
    
    @Override
    @Transactional
    public CollectorLoginWhitelist createWhitelist(CollectorLoginWhitelist whitelist) {
        log.info("创建白名单IP配置，tenantId={}, ipAddress={}", whitelist.getTenantId(), whitelist.getIpAddress());
        
        // 验证IP地址格式
        if (!isValidIpAddress(whitelist.getIpAddress())) {
            throw new IllegalArgumentException("IP地址格式不正确：" + whitelist.getIpAddress());
        }
        
        // 检查是否已存在相同的IP地址
        LambdaQueryWrapper<CollectorLoginWhitelist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectorLoginWhitelist::getTenantId, whitelist.getTenantId())
               .eq(CollectorLoginWhitelist::getIpAddress, whitelist.getIpAddress());
        if (this.count(wrapper) > 0) {
            throw new IllegalArgumentException("该IP地址已存在：" + whitelist.getIpAddress());
        }
        
        // 设置默认值
        if (whitelist.getIsActive() == null) {
            whitelist.setIsActive(true);
        }
        if (whitelist.getIsEnabled() == null) {
            whitelist.setIsEnabled(false);
        }
        
        this.save(whitelist);
        log.info("白名单IP配置创建成功，id={}", whitelist.getId());
        
        return whitelist;
    }
    
    @Override
    @Transactional
    public CollectorLoginWhitelist updateWhitelist(CollectorLoginWhitelist whitelist) {
        log.info("更新白名单IP配置，id={}", whitelist.getId());
        
        CollectorLoginWhitelist existing = this.getById(whitelist.getId());
        if (existing == null) {
            throw new IllegalArgumentException("白名单IP配置不存在，id=" + whitelist.getId());
        }
        
        // 如果修改了IP地址，需要验证格式和检查重复
        if (whitelist.getIpAddress() != null && !whitelist.getIpAddress().equals(existing.getIpAddress())) {
            if (!isValidIpAddress(whitelist.getIpAddress())) {
                throw new IllegalArgumentException("IP地址格式不正确：" + whitelist.getIpAddress());
            }
            
            LambdaQueryWrapper<CollectorLoginWhitelist> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CollectorLoginWhitelist::getTenantId, existing.getTenantId())
                   .eq(CollectorLoginWhitelist::getIpAddress, whitelist.getIpAddress())
                   .ne(CollectorLoginWhitelist::getId, whitelist.getId());
            if (this.count(wrapper) > 0) {
                throw new IllegalArgumentException("该IP地址已存在：" + whitelist.getIpAddress());
            }
        }
        
        this.updateById(whitelist);
        log.info("白名单IP配置更新成功，id={}", whitelist.getId());
        
        return whitelist;
    }
    
    @Override
    @Transactional
    public void deleteWhitelist(Long id) {
        log.info("删除白名单IP配置，id={}", id);
        this.removeById(id);
        log.info("白名单IP配置删除成功，id={}", id);
    }
    
    @Override
    public List<CollectorLoginWhitelist> listWhitelists(Long tenantId) {
        log.info("查询白名单IP配置列表，tenantId={}", tenantId);
        
        LambdaQueryWrapper<CollectorLoginWhitelist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectorLoginWhitelist::getTenantId, tenantId)
               .orderByDesc(CollectorLoginWhitelist::getCreatedAt);
        
        return this.list(wrapper);
    }
    
    @Override
    @Transactional
    public void setWhitelistEnabled(Long tenantId, Boolean enabled) {
        log.info("设置白名单IP登录管理状态，tenantId={}, enabled={}", tenantId, enabled);
        
        // 更新该甲方下所有记录的isEnabled字段
        LambdaQueryWrapper<CollectorLoginWhitelist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectorLoginWhitelist::getTenantId, tenantId);
        
        List<CollectorLoginWhitelist> whitelists = this.list(wrapper);
        for (CollectorLoginWhitelist whitelist : whitelists) {
            whitelist.setIsEnabled(enabled);
            this.updateById(whitelist);
        }
        
        // 如果没有记录，创建一条默认记录
        if (whitelists.isEmpty()) {
            CollectorLoginWhitelist defaultWhitelist = new CollectorLoginWhitelist();
            defaultWhitelist.setTenantId(tenantId);
            defaultWhitelist.setIsEnabled(enabled);
            defaultWhitelist.setIpAddress("0.0.0.0/0"); // 默认允许所有IP（仅作为占位符）
            defaultWhitelist.setDescription("默认配置");
            defaultWhitelist.setIsActive(false); // 默认不启用这条记录
            this.save(defaultWhitelist);
        }
        
        log.info("白名单IP登录管理状态设置成功，tenantId={}, enabled={}", tenantId, enabled);
    }
    
    @Override
    public Boolean isWhitelistEnabled(Long tenantId) {
        try {
            return whitelistMapper.checkWhitelistEnabled(tenantId);
        } catch (Exception e) {
            // 数据库表不存在或查询失败时，默认禁用白名单（允许所有IP登录）
            log.warn("查询白名单状态失败（可能表不存在），默认禁用白名单，tenantId={}, error={}", tenantId, e.getMessage());
            return false;
        }
    }
    
    @Override
    public Boolean isIpAllowed(Long tenantId, String ipAddress) {
        log.info("检查IP地址是否在白名单中，tenantId={}, ipAddress={}", tenantId, ipAddress);
        
        try {
            // 如果未启用白名单，允许所有IP
            if (!isWhitelistEnabled(tenantId)) {
                log.info("该甲方未启用白名单IP登录管理，允许登录");
                return true;
            }
            
            // 查询该甲方下所有启用的白名单IP
            List<CollectorLoginWhitelist> whitelists = whitelistMapper.selectEnabledWhitelistByTenantId(tenantId);
            
            if (whitelists.isEmpty()) {
                log.warn("该甲方启用了白名单但没有任何白名单IP，拒绝登录");
                return false;
            }
            
            // 检查IP是否匹配任何白名单规则
            for (CollectorLoginWhitelist whitelist : whitelists) {
                if (isIpMatch(ipAddress, whitelist.getIpAddress())) {
                    log.info("IP地址匹配白名单规则，允许登录，ipAddress={}, whitelist={}", ipAddress, whitelist.getIpAddress());
                    return true;
                }
            }
            
            log.warn("IP地址不在白名单中，拒绝登录，ipAddress={}", ipAddress);
            return false;
        } catch (Exception e) {
            // 数据库查询失败时，默认允许登录（降级策略）
            log.warn("检查IP白名单失败（可能数据库表不存在），默认允许登录，tenantId={}, ipAddress={}, error={}", 
                    tenantId, ipAddress, e.getMessage());
            return true;
        }
    }
    
    /**
     * 验证IP地址格式（支持IPv4和CIDR格式）
     */
    private boolean isValidIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return false;
        }
        
        // 检查是否是CIDR格式
        if (ipAddress.contains("/")) {
            return CIDR_PATTERN.matcher(ipAddress).matches();
        }
        
        // 检查是否是普通IPv4格式
        return IPV4_PATTERN.matcher(ipAddress).matches();
    }
    
    /**
     * 检查IP地址是否匹配白名单规则（支持CIDR格式）
     */
    private boolean isIpMatch(String ipAddress, String whitelistIp) {
        if (ipAddress == null || whitelistIp == null) {
            return false;
        }
        
        // 精确匹配
        if (ipAddress.equals(whitelistIp)) {
            return true;
        }
        
        // CIDR格式匹配
        if (whitelistIp.contains("/")) {
            return isIpInCidr(ipAddress, whitelistIp);
        }
        
        // 普通IP匹配
        return ipAddress.equals(whitelistIp);
    }
    
    /**
     * 检查IP地址是否在CIDR网段内
     */
    private boolean isIpInCidr(String ipAddress, String cidr) {
        try {
            String[] parts = cidr.split("/");
            String networkIp = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);
            
            long ipLong = ipToLong(ipAddress);
            long networkLong = ipToLong(networkIp);
            long mask = (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;
            
            return (ipLong & mask) == (networkLong & mask);
        } catch (Exception e) {
            log.error("CIDR匹配失败，ipAddress={}, cidr={}", ipAddress, cidr, e);
            return false;
        }
    }
    
    /**
     * 将IP地址转换为长整型
     */
    private long ipToLong(String ipAddress) {
        String[] parts = ipAddress.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result = (result << 8) + Integer.parseInt(parts[i]);
        }
        return result;
    }
}


