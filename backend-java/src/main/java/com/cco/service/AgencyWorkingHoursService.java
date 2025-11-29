package com.cco.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cco.model.entity.AgencyWorkingHours;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 机构作息时间服务接口
 * 
 * @author CCO Team
 * @since 2025-01-11
 */
public interface AgencyWorkingHoursService extends IService<AgencyWorkingHours> {
    
    /**
     * 根据机构ID获取作息时间（返回7天的数据）
     * 
     * @param agencyId 机构ID
     * @return 作息时间列表（按day_of_week排序，1-7）
     */
    List<AgencyWorkingHours> getByAgencyId(Long agencyId);
    
    /**
     * 批量更新机构作息时间（先删除旧的，再创建新的）
     * 
     * @param agencyId 机构ID
     * @param workingHours 作息时间列表（7天的数据）
     * @return 更新后的作息时间列表
     */
    List<AgencyWorkingHours> batchUpdate(Long agencyId, List<AgencyWorkingHours> workingHours);
    
    /**
     * 检查指定时间是否在机构的营业时间内
     * 
     * @param agencyId 机构ID
     * @param datetime 要检查的日期时间
     * @return 是否在营业时间内
     */
    boolean isWorkingHours(Long agencyId, LocalDateTime datetime);
}

