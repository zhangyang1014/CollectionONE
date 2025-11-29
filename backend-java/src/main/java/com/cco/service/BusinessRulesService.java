package com.cco.service;

import java.time.LocalDateTime;

/**
 * 业务规则服务接口
 * 
 * @author CCO Team
 * @since 2025-01-11
 */
public interface BusinessRulesService {
    
    /**
     * 检查是否在营业时间内
     * 
     * @param agencyId 机构ID
     * @param datetime 要检查的日期时间
     * @return 是否在营业时间内
     */
    boolean checkWorkingHours(Long agencyId, LocalDateTime datetime);
}

