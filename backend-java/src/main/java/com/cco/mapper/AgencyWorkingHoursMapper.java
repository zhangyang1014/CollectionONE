package com.cco.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cco.model.entity.AgencyWorkingHours;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 机构作息时间Mapper
 * 注意：使用MapperConfig手动注册，不使用@Mapper注解
 * 
 * @author CCO Team
 * @since 2025-01-11
 */
public interface AgencyWorkingHoursMapper extends BaseMapper<AgencyWorkingHours> {
    
    /**
     * 根据机构ID查询作息时间（返回7天的数据）
     * 
     * @param agencyId 机构ID
     * @return 作息时间列表（按day_of_week排序）
     */
    List<AgencyWorkingHours> selectByAgencyId(@Param("agencyId") Long agencyId);
    
    /**
     * 根据机构ID和星期几查询作息时间
     * 
     * @param agencyId 机构ID
     * @param dayOfWeek 星期几（1-7，1=周一）
     * @return 作息时间
     */
    AgencyWorkingHours selectByAgencyIdAndDay(@Param("agencyId") Long agencyId, @Param("dayOfWeek") Integer dayOfWeek);
    
    /**
     * 删除机构的所有作息时间
     * 
     * @param agencyId 机构ID
     * @return 删除的记录数
     */
    int deleteByAgencyId(@Param("agencyId") Long agencyId);
}

