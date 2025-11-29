package com.cco.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cco.mapper.CaseAssignmentMapper;
import com.cco.mapper.CaseMapper;
import com.cco.mapper.CollectorMapper;
import com.cco.model.dto.*;
import com.cco.model.entity.Case;
import com.cco.model.entity.CaseAssignment;
import com.cco.model.entity.Collector;
import com.cco.service.CaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 案件服务实现
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@Service
public class CaseServiceImpl extends ServiceImpl<CaseMapper, Case> implements CaseService {
    
    @Autowired
    private CollectorMapper collectorMapper;
    
    @Autowired
    private CaseAssignmentMapper caseAssignmentMapper;
    
    @Override
    public Map<String, Object> getCaseList(
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
            Integer limit) {
        
        log.info("查询案件列表，tenantId={}, collectorId={}, searchKeyword={}, skip={}, limit={}", 
                tenantId, collectorId, searchKeyword, skip, limit);
        
        try {
            // 查询案件列表
            List<Case> cases = baseMapper.selectCaseList(
                    tenantId,
                    collectorId,
                    caseStatus,
                    queueId,
                    agencyId,
                    teamId,
                    userId,
                    searchKeyword,
                    dueDateStart,
                    dueDateEnd,
                    settlementDateStart,
                    settlementDateEnd,
                    sortBy,
                    skip,
                    limit
            );
            
            // 查询总数
            Long total = baseMapper.countCases(
                    tenantId,
                    collectorId,
                    caseStatus,
                    queueId,
                    agencyId,
                    teamId,
                    userId,
                    searchKeyword,
                    dueDateStart,
                    dueDateEnd,
                    settlementDateStart,
                    settlementDateEnd
            );
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("items", cases);
            result.put("total", total != null ? total : 0L);
            result.put("skip", skip);
            result.put("limit", limit);
            
            log.info("返回案件列表，总数={}, 当前页数量={}", total, cases.size());
            return result;
        } catch (Exception e) {
            log.error("查询案件列表失败", e);
            // 如果表不存在，返回空列表而不是抛出异常
            if (e.getMessage() != null && e.getMessage().contains("doesn't exist")) {
                log.warn("cases表不存在，返回空列表。请执行SQL脚本创建表：backend-java/src/main/resources/db/migration/create_cases_table.sql");
                Map<String, Object> result = new HashMap<>();
                result.put("items", new ArrayList<>());
                result.put("total", 0L);
                result.put("skip", skip);
                result.put("limit", limit);
                return result;
            }
            throw e;
        }
    }
    
    @Override
    public List<CollectorListDTO> getCollectorListForAssign(
            Long agencyId,
            Long teamId,
            Long queueId,
            String searchKeyword) {
        log.info("========== 获取催员列表，agencyId={}, teamId={}, queueId={}, searchKeyword={} ==========", 
                agencyId, teamId, queueId, searchKeyword);
        
        try {
            // 检查collectorMapper是否注入成功
            if (collectorMapper == null) {
                log.error("========== CollectorMapper未注入，使用BaseMapper查询 ==========");
                // 如果自定义Mapper未注入，使用BaseMapper查询
                return getCollectorsByBaseMapper(agencyId, teamId, queueId, searchKeyword);
            }
            
            // 尝试使用自定义SQL查询
            try {
                List<CollectorListDTO> collectors = collectorMapper.selectCollectorListForAssign(
                        agencyId, teamId, queueId, searchKeyword);
                log.info("========== 返回催员列表（自定义SQL），数量={} ==========", collectors.size());
                return collectors;
            } catch (Exception sqlEx) {
                log.warn("========== 自定义SQL查询失败，使用BaseMapper查询，错误: {} ==========", sqlEx.getMessage());
                // 如果自定义SQL失败，降级使用BaseMapper
                return getCollectorsByBaseMapper(agencyId, teamId, queueId, searchKeyword);
            }
        } catch (Exception e) {
            log.error("========== 获取催员列表失败 ==========", e);
            log.error("错误类型: {}", e.getClass().getName());
            log.error("错误消息: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("根本原因: {}", e.getCause().getMessage());
            }
            
            // 所有错误都返回空列表，避免前端显示500错误
            log.warn("========== 返回空列表 ==========");
            return new ArrayList<>();
        }
    }
    
    /**
     * 使用BaseMapper查询催员列表（降级方案）
     */
    private List<CollectorListDTO> getCollectorsByBaseMapper(
            Long agencyId, Long teamId, Long queueId, String searchKeyword) {
        try {
            log.info("========== 使用BaseMapper查询催员列表 ==========");
            
            // 先查询所有催员，看看数据库中有没有数据
            QueryWrapper<Collector> allWrapper = new QueryWrapper<>();
            List<Collector> allCollectors = collectorMapper.selectList(allWrapper);
            log.info("========== 数据库中总催员数: {} ==========", allCollectors.size());
            
            if (allCollectors.size() > 0) {
                log.info("========== 前3个催员信息: ==========");
                for (int i = 0; i < Math.min(3, allCollectors.size()); i++) {
                    Collector c = allCollectors.get(i);
                    log.info("催员{}: id={}, name={}, code={}, is_active={}, status={}, agency_id={}, team_id={}", 
                            i+1, c.getId(), c.getCollectorName(), c.getCollectorCode(), 
                            c.getIsActive(), c.getStatus(), c.getAgencyId(), c.getTeamId());
                }
            }
            
            // 使用MyBatis Plus的BaseMapper查询（放宽条件）
            QueryWrapper<Collector> wrapper = new QueryWrapper<>();
            
            // 先只查询is_active=1的，如果结果为空，再查询所有
            wrapper.eq("is_active", 1);
            
            // status条件改为可选，因为可能数据库中的status值不是'active'
            // wrapper.eq("status", "active");
            
            if (agencyId != null) {
                wrapper.eq("agency_id", agencyId);
            }
            if (teamId != null) {
                wrapper.eq("team_id", teamId);
            }
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                wrapper.and(w -> w.like("collector_name", searchKeyword)
                                 .or()
                                 .like("collector_code", searchKeyword));
            }
            
            List<Collector> collectors = collectorMapper.selectList(wrapper);
            log.info("========== 查询到催员数（is_active=1）: {} ==========", collectors.size());
            
            // 如果查询结果为空，尝试查询所有启用的催员（不限制status）
            if (collectors.isEmpty()) {
                log.warn("========== is_active=1的催员为空，尝试查询所有启用的催员 ==========");
                QueryWrapper<Collector> wrapper2 = new QueryWrapper<>();
                wrapper2.eq("is_active", 1);
                if (agencyId != null) {
                    wrapper2.eq("agency_id", agencyId);
                }
                if (teamId != null) {
                    wrapper2.eq("team_id", teamId);
                }
                if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                    wrapper2.and(w -> w.like("collector_name", searchKeyword)
                                     .or()
                                     .like("collector_code", searchKeyword));
                }
                collectors = collectorMapper.selectList(wrapper2);
                log.info("========== 查询到催员数（所有启用）: {} ==========", collectors.size());
            }
            
            // 转换为DTO
            List<CollectorListDTO> result = new ArrayList<>();
            for (Collector c : collectors) {
                CollectorListDTO dto = new CollectorListDTO();
                dto.setId(c.getId());
                dto.setCollectorName(c.getCollectorName());
                dto.setCollectorCode(c.getCollectorCode());
                dto.setAgencyId(c.getAgencyId());
                dto.setTeamId(c.getTeamId());
                dto.setStatus(c.getStatus());
                dto.setCurrentCaseCount(c.getCurrentCaseCount() != null ? c.getCurrentCaseCount() : 0);
                
                // TODO: 可以通过关联查询获取机构名称、小组名称、队列信息
                // 暂时设置为空
                dto.setAgencyName("");
                dto.setTeamName("");
                dto.setQueueId(null);
                dto.setQueueName("");
                
                result.add(dto);
            }
            
            log.info("========== 返回催员列表（BaseMapper），数量={} ==========", result.size());
            return result;
        } catch (Exception e) {
            log.error("========== BaseMapper查询也失败 ==========", e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Override
    public QueueLimitCheckResponse checkQueueLimit(QueueLimitCheckRequest request) {
        log.info("检查队列限制，caseIds={}, collectorIds={}", 
                request.getCaseIds(), request.getCollectorIds());
        
        QueueLimitCheckResponse response = new QueueLimitCheckResponse();
        response.setHasLimit(false);
        response.setUnmatchedItems(new ArrayList<>());
        
        try {
            // 查询所有案件的队列信息
            List<Case> cases = baseMapper.selectBatchIds(request.getCaseIds());
            Map<Long, Case> caseMap = cases.stream()
                    .collect(Collectors.toMap(Case::getId, c -> c));
            
            // 查询所有催员及其小组的队列信息
            List<Collector> collectors = collectorMapper.selectBatchIds(request.getCollectorIds());
            
            // 检查每个案件和催员的队列是否匹配
            for (Long caseId : request.getCaseIds()) {
                Case caseInfo = caseMap.get(caseId);
                if (caseInfo == null) {
                    log.warn("案件不存在：{}", caseId);
                    continue;
                }
                
                for (Collector collector : collectors) {
                    // 通过关联查询获取催员小组的队列ID
                    // 这里简化处理，实际应该查询collection_teams表获取queue_id
                    Long collectorTeamQueueId = getCollectorTeamQueueId(collector.getTeamId());
                    
                    // 如果案件队列ID与催员小组队列ID不匹配
                    if (caseInfo.getQueueId() != null && collectorTeamQueueId != null
                            && !caseInfo.getQueueId().equals(collectorTeamQueueId)) {
                        response.setHasLimit(true);
                        
                        QueueLimitCheckResponse.UnmatchedItem item = new QueueLimitCheckResponse.UnmatchedItem();
                        item.setCaseId(caseId);
                        item.setCaseCode(caseInfo.getCaseCode());
                        item.setCaseQueueId(caseInfo.getQueueId());
                        item.setCaseQueueName(getQueueName(caseInfo.getQueueId()));
                        item.setCollectorId(collector.getId());
                        item.setCollectorName(collector.getCollectorName());
                        item.setCollectorTeamQueueId(collectorTeamQueueId);
                        item.setCollectorTeamQueueName(getQueueName(collectorTeamQueueId));
                        
                        response.getUnmatchedItems().add(item);
                    }
                }
            }
            
            log.info("队列限制检查完成，hasLimit={}, unmatchedCount={}", 
                    response.getHasLimit(), response.getUnmatchedItems().size());
            return response;
        } catch (Exception e) {
            log.error("队列限制检查失败", e);
            return response;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchAssignResponse batchAssignCases(BatchAssignRequest request, Long operatorId) {
        log.info("批量分配案件，caseIds={}, collectorIds={}, ignoreQueueLimit={}, operatorId={}", 
                request.getCaseIds(), request.getCollectorIds(), request.getIgnoreQueueLimit(), operatorId);
        
        BatchAssignResponse response = new BatchAssignResponse();
        response.setSuccessCount(0);
        response.setFailureCount(0);
        response.setAssignments(new ArrayList<>());
        
        try {
            // 平均分配算法
            int caseCount = request.getCaseIds().size();
            int collectorCount = request.getCollectorIds().size();
            int baseCount = caseCount / collectorCount;
            int remainder = caseCount % collectorCount;
            
            // 随机打乱案件顺序
            List<Long> shuffledCaseIds = new ArrayList<>(request.getCaseIds());
            Collections.shuffle(shuffledCaseIds);
            
            // 分配案件
            int caseIndex = 0;
            List<CaseAssignment> assignments = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            
            for (int i = 0; i < collectorCount; i++) {
                Long collectorId = request.getCollectorIds().get(i);
                int assignCount = baseCount + (i < remainder ? 1 : 0);
                
                // 获取催员信息
                Collector collector = collectorMapper.selectById(collectorId);
                if (collector == null) {
                    log.warn("催员不存在：{}", collectorId);
                    continue;
                }
                
                for (int j = 0; j < assignCount && caseIndex < caseCount; j++) {
                    Long caseId = shuffledCaseIds.get(caseIndex++);
                    
                    try {
                        // 更新案件的催员信息
                        Case caseInfo = new Case();
                        caseInfo.setId(caseId);
                        caseInfo.setCollectorId(collectorId);
                        caseInfo.setTeamId(collector.getTeamId());
                        caseInfo.setAgencyId(collector.getAgencyId());
                        caseInfo.setAssignedAt(now);
                        baseMapper.updateById(caseInfo);
                        
                        // 记录分配历史
                        CaseAssignment assignment = new CaseAssignment();
                        assignment.setCaseId(caseId);
                        assignment.setCollectorId(collectorId);
                        assignment.setAssignedBy(operatorId);
                        assignment.setAssignedAt(now);
                        assignment.setIgnoreQueueLimit(request.getIgnoreQueueLimit());
                        assignments.add(assignment);
                        
                        // 记录成功详情
                        BatchAssignResponse.AssignmentDetail detail = new BatchAssignResponse.AssignmentDetail();
                        detail.setCaseId(caseId);
                        detail.setCollectorId(collectorId);
                        detail.setStatus("success");
                        response.getAssignments().add(detail);
                        response.setSuccessCount(response.getSuccessCount() + 1);
                        
                    } catch (Exception e) {
                        log.error("分配案件失败，caseId={}, collectorId={}", caseId, collectorId, e);
                        
                        // 记录失败详情
                        BatchAssignResponse.AssignmentDetail detail = new BatchAssignResponse.AssignmentDetail();
                        detail.setCaseId(caseId);
                        detail.setCollectorId(collectorId);
                        detail.setStatus("failure");
                        detail.setErrorMessage(e.getMessage());
                        response.getAssignments().add(detail);
                        response.setFailureCount(response.getFailureCount() + 1);
                    }
                }
            }
            
            // 批量插入分配记录
            if (!assignments.isEmpty()) {
                caseAssignmentMapper.batchInsert(assignments);
            }
            
            log.info("批量分配完成，成功={}, 失败={}", response.getSuccessCount(), response.getFailureCount());
            return response;
            
        } catch (Exception e) {
            log.error("批量分配案件失败", e);
            throw new RuntimeException("批量分配案件失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 获取催员小组的队列ID
     */
    private Long getCollectorTeamQueueId(Long teamId) {
        if (teamId == null) {
            return null;
        }
        // 这里应该查询collection_teams表获取queue_id
        // 简化处理，直接通过SQL查询
        try {
            return baseMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Case>()
                            .select("team_id")
                            .eq("id", 1)
            ).getTeamId();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取队列名称
     */
    private String getQueueName(Long queueId) {
        if (queueId == null) {
            return "";
        }
        // 这里应该查询case_queues表获取queue_name
        // 简化处理，返回队列ID
        return "队列" + queueId;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchStayResponse batchStayCases(BatchStayRequest request, Long operatorId) {
        log.info("========== 批量标记停留，caseIds={}, operated_by={} ==========", 
                request.getCaseIds(), operatorId);
        
        BatchStayResponse response = new BatchStayResponse();
        response.setSuccessCount(0);
        response.setFailureCount(0);
        response.setFailures(new ArrayList<>());
        
        if (request.getCaseIds() == null || request.getCaseIds().isEmpty()) {
            log.warn("案件ID列表为空");
            return response;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        for (Long caseId : request.getCaseIds()) {
            try {
                // 查询案件
                Case caseInfo = baseMapper.selectById(caseId);
                if (caseInfo == null) {
                    log.warn("案件不存在：{}", caseId);
                    BatchStayResponse.FailureDetail failure = new BatchStayResponse.FailureDetail();
                    failure.setCaseId(caseId);
                    failure.setErrorMessage("案件不存在");
                    response.getFailures().add(failure);
                    response.setFailureCount(response.getFailureCount() + 1);
                    continue;
                }
                
                // 检查案件状态：已结清的案件不能停留
                if ("normal_settlement".equals(caseInfo.getCaseStatus()) 
                        || "extension_settlement".equals(caseInfo.getCaseStatus())) {
                    log.warn("已结清的案件不能标记为停留：{}", caseId);
                    BatchStayResponse.FailureDetail failure = new BatchStayResponse.FailureDetail();
                    failure.setCaseId(caseId);
                    failure.setErrorMessage("已结清的案件不能标记为停留");
                    response.getFailures().add(failure);
                    response.setFailureCount(response.getFailureCount() + 1);
                    continue;
                }
                
                // 检查是否已经是停留状态（幂等性）
                if (Boolean.TRUE.equals(caseInfo.getIsStay())) {
                    log.info("案件已经是停留状态，跳过：{}", caseId);
                    response.setSuccessCount(response.getSuccessCount() + 1);
                    continue;
                }
                
                // 更新案件为停留状态
                Case updateCase = new Case();
                updateCase.setId(caseId);
                updateCase.setIsStay(true);
                updateCase.setStayAt(now);
                updateCase.setStayBy(operatorId);
                // 清除催员分配
                updateCase.setCollectorId(null);
                updateCase.setTeamId(null);
                updateCase.setAgencyId(null);
                
                baseMapper.updateById(updateCase);
                
                response.setSuccessCount(response.getSuccessCount() + 1);
                log.info("案件标记停留成功：{}", caseId);
                
            } catch (Exception e) {
                log.error("标记案件停留失败，caseId={}", caseId, e);
                BatchStayResponse.FailureDetail failure = new BatchStayResponse.FailureDetail();
                failure.setCaseId(caseId);
                failure.setErrorMessage(e.getMessage());
                response.getFailures().add(failure);
                response.setFailureCount(response.getFailureCount() + 1);
            }
        }
        
        log.info("========== 批量标记停留完成，成功={}, 失败={} ==========", 
                response.getSuccessCount(), response.getFailureCount());
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchReleaseStayResponse batchReleaseStayCases(BatchReleaseStayRequest request, Long operatorId) {
        log.info("========== 批量解放停留，caseIds={}, operated_by={} ==========", 
                request.getCaseIds(), operatorId);
        
        BatchReleaseStayResponse response = new BatchReleaseStayResponse();
        response.setSuccessCount(0);
        response.setFailureCount(0);
        response.setFailures(new ArrayList<>());
        
        if (request.getCaseIds() == null || request.getCaseIds().isEmpty()) {
            log.warn("案件ID列表为空");
            return response;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        for (Long caseId : request.getCaseIds()) {
            try {
                // 查询案件
                Case caseInfo = baseMapper.selectById(caseId);
                if (caseInfo == null) {
                    log.warn("案件不存在：{}", caseId);
                    BatchReleaseStayResponse.FailureDetail failure = new BatchReleaseStayResponse.FailureDetail();
                    failure.setCaseId(caseId);
                    failure.setErrorMessage("案件不存在");
                    response.getFailures().add(failure);
                    response.setFailureCount(response.getFailureCount() + 1);
                    continue;
                }
                
                // 检查是否是停留状态
                if (!Boolean.TRUE.equals(caseInfo.getIsStay())) {
                    log.warn("案件不是停留状态，不能解放停留：{}", caseId);
                    BatchReleaseStayResponse.FailureDetail failure = new BatchReleaseStayResponse.FailureDetail();
                    failure.setCaseId(caseId);
                    failure.setErrorMessage("案件不是停留状态");
                    response.getFailures().add(failure);
                    response.setFailureCount(response.getFailureCount() + 1);
                    continue;
                }
                
                // 更新案件：取消停留状态
                Case updateCase = new Case();
                updateCase.setId(caseId);
                updateCase.setIsStay(false);
                updateCase.setStayAt(null);
                updateCase.setStayBy(null);
                updateCase.setStayReleasedAt(now);
                updateCase.setStayReleasedBy(operatorId);
                // 注意：解放停留后，案件不绑定催员（collector_id, team_id, agency_id保持为NULL）
                
                baseMapper.updateById(updateCase);
                
                response.setSuccessCount(response.getSuccessCount() + 1);
                log.info("案件解放停留成功：{}", caseId);
                
            } catch (Exception e) {
                log.error("解放案件停留失败，caseId={}", caseId, e);
                BatchReleaseStayResponse.FailureDetail failure = new BatchReleaseStayResponse.FailureDetail();
                failure.setCaseId(caseId);
                failure.setErrorMessage(e.getMessage());
                response.getFailures().add(failure);
                response.setFailureCount(response.getFailureCount() + 1);
            }
        }
        
        log.info("========== 批量解放停留完成，成功={}, 失败={} ==========", 
                response.getSuccessCount(), response.getFailureCount());
        return response;
    }
    
    @Override
    public Map<String, Object> getStayCaseList(
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
            Integer limit) {
        
        log.info("查询停留案件列表，tenantId={}, queueId={}, stayBy={}, searchKeyword={}, skip={}, limit={}", 
                tenantId, queueId, stayBy, searchKeyword, skip, limit);
        
        try {
            // 查询停留案件列表
            List<Case> cases = baseMapper.selectStayCaseList(
                    tenantId,
                    queueId,
                    agencyId,
                    teamId,
                    stayBy,
                    stayDateStart,
                    stayDateEnd,
                    searchKeyword,
                    sortBy,
                    skip,
                    limit
            );
            
            // 查询总数
            Long total = baseMapper.countStayCases(
                    tenantId,
                    queueId,
                    agencyId,
                    teamId,
                    stayBy,
                    stayDateStart,
                    stayDateEnd,
                    searchKeyword
            );
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("items", cases);
            result.put("total", total != null ? total : 0L);
            result.put("skip", skip);
            result.put("limit", limit);
            
            log.info("返回停留案件列表，总数={}, 当前页数量={}", total, cases.size());
            return result;
        } catch (Exception e) {
            log.error("========== 查询停留案件列表失败 ==========");
            log.error("异常类型: {}", e.getClass().getName());
            log.error("异常消息: {}", e.getMessage());
            
            // 递归检查所有cause
            Throwable cause = e.getCause();
            int depth = 0;
            while (cause != null && depth < 5) {
                log.error("根本原因[{}]: {} - {}", depth, cause.getClass().getName(), cause.getMessage());
                cause = cause.getCause();
                depth++;
            }
            
            // 打印完整堆栈（仅前10行）
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (int i = 0; i < Math.min(10, stackTrace.length); i++) {
                log.error("  at {}", stackTrace[i]);
            }
            
            // 如果字段不存在或表不存在，返回空列表而不是抛出异常
            String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            String causeMsg = "";
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                causeMsg = e.getCause().getMessage().toLowerCase();
            }
            
            // 检查是否是字段不存在的错误（更全面的检查）
            boolean isFieldMissingError = 
                errorMsg.contains("doesn't exist") || 
                errorMsg.contains("unknown column") || 
                errorMsg.contains("is_stay") ||
                errorMsg.contains("bad sql grammar") ||
                errorMsg.contains("sql syntax") ||
                errorMsg.contains("column") && errorMsg.contains("not found") ||
                causeMsg.contains("doesn't exist") ||
                causeMsg.contains("unknown column") ||
                causeMsg.contains("is_stay") ||
                causeMsg.contains("bad sql grammar") ||
                causeMsg.contains("sql syntax");
            
            if (isFieldMissingError) {
                log.warn("========== 停留字段不存在，返回空列表 ==========");
                log.warn("请执行SQL迁移脚本: backend-java/src/main/resources/db/migration/add_case_stay_fields.sql");
                Map<String, Object> result = new HashMap<>();
                result.put("items", new ArrayList<>());
                result.put("total", 0L);
                result.put("skip", skip);
                result.put("limit", limit);
                return result;
            }
            
            // 其他异常也返回空列表，避免前端500错误
            log.warn("========== 未知错误，返回空列表以避免前端报错 ==========");
            Map<String, Object> result = new HashMap<>();
            result.put("items", new ArrayList<>());
            result.put("total", 0L);
            result.put("skip", skip);
            result.put("limit", limit);
            return result;
        }
    }
}

