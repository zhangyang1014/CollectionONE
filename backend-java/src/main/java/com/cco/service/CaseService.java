package com.cco.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cco.model.dto.BatchAssignRequest;
import com.cco.model.dto.BatchAssignResponse;
import com.cco.model.dto.BatchStayRequest;
import com.cco.model.dto.BatchStayResponse;
import com.cco.model.dto.BatchReleaseStayRequest;
import com.cco.model.dto.BatchReleaseStayResponse;
import com.cco.model.dto.CollectorListDTO;
import com.cco.model.dto.QueueLimitCheckRequest;
import com.cco.model.dto.QueueLimitCheckResponse;
import com.cco.model.entity.Case;

import java.util.List;
import java.util.Map;

/**
 * 案件服务接口
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
public interface CaseService extends IService<Case> {
    
    /**
     * 查询案件列表（支持搜索和分页）
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
     * @return 包含items和total的Map
     */
    Map<String, Object> getCaseList(
            Long tenantId,
            Long collectorId,
            String caseStatus,
            Long queueId,
            Long agencyId,
            Long teamId,
            String userId,
            String searchKeyword,
            String dueDateStart,
            String dueDateEnd,
            String settlementDateStart,
            String settlementDateEnd,
            String sortBy,
            Integer skip,
            Integer limit
    );
    
    /**
     * 获取催员列表（用于分案弹窗）
     */
    List<CollectorListDTO> getCollectorListForAssign(
            Long agencyId,
            Long teamId,
            Long queueId,
            String searchKeyword
    );
    
    /**
     * 检查队列限制
     */
    QueueLimitCheckResponse checkQueueLimit(QueueLimitCheckRequest request);
    
    /**
     * 批量分配案件
     */
    BatchAssignResponse batchAssignCases(BatchAssignRequest request, Long operatorId);
    
    /**
     * 批量标记案件为停留
     */
    BatchStayResponse batchStayCases(BatchStayRequest request, Long operatorId);
    
    /**
     * 批量解放停留案件
     */
    BatchReleaseStayResponse batchReleaseStayCases(BatchReleaseStayRequest request, Long operatorId);
    
    /**
     * 查询停留案件列表（支持搜索和分页）
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
     * @return 包含items和total的Map
     */
    Map<String, Object> getStayCaseList(
            Long tenantId,
            Long queueId,
            Long agencyId,
            Long teamId,
            Long stayBy,
            String stayDateStart,
            String stayDateEnd,
            String searchKeyword,
            String sortBy,
            Integer skip,
            Integer limit
    );
}



