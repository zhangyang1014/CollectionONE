package com.cco.service.impl;

import com.cco.service.AgencyWorkingHoursService;
import com.cco.service.BusinessRulesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 业务规则服务实现
 * 
 * @author CCO Team
 * @since 2025-01-11
 */
@Slf4j
@Service
public class BusinessRulesServiceImpl implements BusinessRulesService {
    
    @Autowired
    private AgencyWorkingHoursService agencyWorkingHoursService;
    
    @Override
    public boolean checkWorkingHours(Long agencyId, LocalDateTime datetime) {
        return agencyWorkingHoursService.isWorkingHours(agencyId, datetime);
    }
}

