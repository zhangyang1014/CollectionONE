package com.cco.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cco.model.entity.CaseReassignConfig;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 案件重新分案配置Mapper
 * 
 * @author CCO Team
 * @since 2025-01-22
 */
public interface CaseReassignConfigMapper extends BaseMapper<CaseReassignConfig> {
    
    /**
     * 查询已生效的配置列表
     * 
     * @param tenantId 甲方ID（可选）
     * @param effectiveDate 生效日期（当前日期）
     * @return 已生效的配置列表
     */
    List<CaseReassignConfig> selectEffectiveConfigs(
            @Param("tenantId") Long tenantId,
            @Param("effectiveDate") LocalDate effectiveDate
    );
}

