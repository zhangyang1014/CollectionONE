package com.cco.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cco.model.entity.Case;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 案件Mapper
 * 注意：使用MapperConfig手动注册，不使用@Mapper注解
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
public interface CaseMapper extends BaseMapper<Case> {
    
    /**
     * 查询案件列表（支持搜索）
     * 
     * @param tenantId 甲方ID
     * @param collectorId 催员ID（可选）
     * @param caseStatus 案件状态（可选）
     * @param queueId 队列ID（可选）
     * @param agencyId 机构ID（可选）
     * @param teamId 小组ID（可选）
     * @param userId 用户ID（可选）
     * @param searchKeyword 搜索关键词（可选，搜索案件编号、客户姓名、客户ID、手机号码）
     * @param dueDateStart 到期日期开始（可选）
     * @param dueDateEnd 到期日期结束（可选）
     * @param settlementDateStart 结清日期开始（可选）
     * @param settlementDateEnd 结清日期结束（可选）
     * @param sortBy 排序方式（可选，如：collection_value）
     * @param skip 跳过数量
     * @param limit 限制数量
     * @return 案件列表
     */
    List<Case> selectCaseList(
            @Param("tenantId") Long tenantId,
            @Param("collectorId") Long collectorId,
            @Param("caseStatus") String caseStatus,
            @Param("queueId") Long queueId,
            @Param("agencyId") Long agencyId,
            @Param("teamId") Long teamId,
            @Param("userId") String userId,
            @Param("searchKeyword") String searchKeyword,
            @Param("dueDateStart") String dueDateStart,
            @Param("dueDateEnd") String dueDateEnd,
            @Param("settlementDateStart") String settlementDateStart,
            @Param("settlementDateEnd") String settlementDateEnd,
            @Param("sortBy") String sortBy,
            @Param("skip") Integer skip,
            @Param("limit") Integer limit
    );
    
    /**
     * 统计案件总数（支持搜索）
     * 
     * @param tenantId 甲方ID
     * @param collectorId 催员ID（可选）
     * @param caseStatus 案件状态（可选）
     * @param queueId 队列ID（可选）
     * @param agencyId 机构ID（可选）
     * @param teamId 小组ID（可选）
     * @param userId 用户ID（可选）
     * @param searchKeyword 搜索关键词（可选）
     * @param dueDateStart 到期日期开始（可选）
     * @param dueDateEnd 到期日期结束（可选）
     * @param settlementDateStart 结清日期开始（可选）
     * @param settlementDateEnd 结清日期结束（可选）
     * @return 案件总数
     */
    Long countCases(
            @Param("tenantId") Long tenantId,
            @Param("collectorId") Long collectorId,
            @Param("caseStatus") String caseStatus,
            @Param("queueId") Long queueId,
            @Param("agencyId") Long agencyId,
            @Param("teamId") Long teamId,
            @Param("userId") String userId,
            @Param("searchKeyword") String searchKeyword,
            @Param("dueDateStart") String dueDateStart,
            @Param("dueDateEnd") String dueDateEnd,
            @Param("settlementDateStart") String settlementDateStart,
            @Param("settlementDateEnd") String settlementDateEnd
    );
    
    /**
     * 查询停留案件列表（支持搜索和筛选）
     * 
     * @param tenantId 甲方ID（可选，根据权限自动过滤）
     * @param queueId 队列ID（可选）
     * @param agencyId 机构ID（可选，根据权限自动过滤）
     * @param teamId 小组ID（可选，根据权限自动过滤）
     * @param stayBy 停留操作人ID（可选）
     * @param stayDateStart 停留时间开始（可选）
     * @param stayDateEnd 停留时间结束（可选）
     * @param searchKeyword 搜索关键词（可选，搜索案件编号、客户姓名、客户ID、手机号码）
     * @param sortBy 排序方式（可选）
     * @param skip 跳过数量
     * @param limit 限制数量
     * @return 停留案件列表
     */
    List<Case> selectStayCaseList(
            @Param("tenantId") Long tenantId,
            @Param("queueId") Long queueId,
            @Param("agencyId") Long agencyId,
            @Param("teamId") Long teamId,
            @Param("stayBy") Long stayBy,
            @Param("stayDateStart") String stayDateStart,
            @Param("stayDateEnd") String stayDateEnd,
            @Param("searchKeyword") String searchKeyword,
            @Param("sortBy") String sortBy,
            @Param("skip") Integer skip,
            @Param("limit") Integer limit
    );
    
    /**
     * 统计停留案件总数（支持搜索和筛选）
     * 
     * @param tenantId 甲方ID（可选，根据权限自动过滤）
     * @param queueId 队列ID（可选）
     * @param agencyId 机构ID（可选，根据权限自动过滤）
     * @param teamId 小组ID（可选，根据权限自动过滤）
     * @param stayBy 停留操作人ID（可选）
     * @param stayDateStart 停留时间开始（可选）
     * @param stayDateEnd 停留时间结束（可选）
     * @param searchKeyword 搜索关键词（可选）
     * @return 停留案件总数
     */
    Long countStayCases(
            @Param("tenantId") Long tenantId,
            @Param("queueId") Long queueId,
            @Param("agencyId") Long agencyId,
            @Param("teamId") Long teamId,
            @Param("stayBy") Long stayBy,
            @Param("stayDateStart") String stayDateStart,
            @Param("stayDateEnd") String stayDateEnd,
            @Param("searchKeyword") String searchKeyword
    );
    
    /**
     * 查询需要重新分案的案件列表
     * 根据配置维度（队列/机构/小组）和天数筛选
     * 排除已结清案件和有有效PTP记录的案件
     * 
     * @param tenantId 甲方ID
     * @param configType 配置类型: queue/agency/team
     * @param targetId 目标ID（队列ID/机构ID/小组ID）
     * @param reassignDays 重新分案天数
     * @return 需要重新分案的案件列表
     */
    List<Case> selectCasesToReassign(
            @Param("tenantId") Long tenantId,
            @Param("configType") String configType,
            @Param("targetId") Long targetId,
            @Param("reassignDays") Integer reassignDays
    );
}



