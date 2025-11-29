package com.cco.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cco.mapper.CaseAssignmentMapper;
import com.cco.mapper.CaseMapper;
import com.cco.mapper.CaseReassignConfigMapper;
import com.cco.mapper.CollectorMapper;
import com.cco.model.entity.Case;
import com.cco.model.entity.CaseAssignment;
import com.cco.model.entity.CaseReassignConfig;
import com.cco.model.entity.Collector;
import com.cco.service.CaseReassignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 案件重新分案服务实现
 * 
 * @author CCO Team
 * @since 2025-01-22
 */
@Slf4j
@Service
public class CaseReassignServiceImpl implements CaseReassignService {
    
    @Autowired
    private CaseReassignConfigMapper configMapper;
    
    @Autowired
    private CaseMapper caseMapper;
    
    @Autowired
    private CollectorMapper collectorMapper;
    
    @Autowired
    private CaseAssignmentMapper caseAssignmentMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReassignResult executeReassign() {
        log.info("========== 开始执行案件重新分案任务 ==========");
        
        ReassignResult result = new ReassignResult();
        LocalDateTime now = LocalDateTime.now();
        
        try {
            // 1. 获取所有已生效的配置
            List<CaseReassignConfig> configs = configMapper.selectEffectiveConfigs(null, java.time.LocalDate.now());
            result.setTotalConfigs(configs.size());
            
            log.info("找到 {} 个已生效的重新分案配置", configs.size());
            
            // 2. 按配置分组处理案件
            for (CaseReassignConfig config : configs) {
                try {
                    log.info("处理配置：tenantId={}, configType={}, targetId={}, reassignDays={}", 
                            config.getTenantId(), config.getConfigType(), config.getTargetId(), config.getReassignDays());
                    
                    // 2.1 查找需要重新分案的案件
                    List<Case> casesToReassign = caseMapper.selectCasesToReassign(
                            config.getTenantId(),
                            config.getConfigType(),
                            config.getTargetId(),
                            config.getReassignDays()
                    );
                    
                    if (casesToReassign.isEmpty()) {
                        log.info("配置下没有需要重新分案的案件");
                        continue;
                    }
                    
                    log.info("找到 {} 个需要重新分案的案件", casesToReassign.size());
                    result.setTotalCases(result.getTotalCases() + casesToReassign.size());
                    
                    // 2.2 按配置维度获取可用的催员列表（排除当前催员）
                    List<Collector> availableCollectors = getAvailableCollectors(config, casesToReassign);
                    
                    if (availableCollectors.isEmpty()) {
                        log.warn("配置下没有可用的催员，跳过重新分案");
                        result.setFailedCases(result.getFailedCases() + casesToReassign.size());
                        continue;
                    }
                    
                    // 2.3 执行重新分案
                    int reassigned = reassignCases(casesToReassign, availableCollectors, now);
                    result.setReassignedCases(result.getReassignedCases() + reassigned);
                    result.setFailedCases(result.getFailedCases() + (casesToReassign.size() - reassigned));
                    
                } catch (Exception e) {
                    log.error("处理配置失败：configId={}", config.getId(), e);
                }
            }
            
            log.info("========== 案件重新分案任务完成，处理配置={}, 处理案件={}, 成功={}, 失败={} ==========",
                    result.getTotalConfigs(), result.getTotalCases(), 
                    result.getReassignedCases(), result.getFailedCases());
            
        } catch (Exception e) {
            log.error("执行案件重新分案任务失败", e);
            throw new RuntimeException("执行案件重新分案任务失败：" + e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * 获取可用的催员列表（排除当前催员）
     */
    private List<Collector> getAvailableCollectors(CaseReassignConfig config, List<Case> cases) {
        // 收集所有当前催员ID
        Set<Long> currentCollectorIds = cases.stream()
                .map(Case::getCollectorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        QueryWrapper<Collector> wrapper = new QueryWrapper<>();
        wrapper.eq("is_active", true)
               .eq("tenant_id", config.getTenantId())
               .notIn(!currentCollectorIds.isEmpty(), "id", currentCollectorIds);
        
        // 只支持队列配置：通过小组关联队列
        wrapper.inSql("team_id", 
                "SELECT id FROM collection_teams WHERE queue_id = " + config.getTargetId() + " AND is_active = 1");
        
        return collectorMapper.selectList(wrapper);
    }
    
    /**
     * 重新分案（平均分配算法）
     */
    private int reassignCases(List<Case> cases, List<Collector> collectors, LocalDateTime now) {
        if (cases.isEmpty() || collectors.isEmpty()) {
            return 0;
        }
        
        int caseCount = cases.size();
        int collectorCount = collectors.size();
        int baseCount = caseCount / collectorCount;
        int remainder = caseCount % collectorCount;
        
        // 随机打乱案件顺序
        List<Case> shuffledCases = new ArrayList<>(cases);
        Collections.shuffle(shuffledCases);
        
        // 分配案件
        int caseIndex = 0;
        int successCount = 0;
        List<CaseAssignment> assignments = new ArrayList<>();
        
        for (int i = 0; i < collectorCount; i++) {
            Collector collector = collectors.get(i);
            int assignCount = baseCount + (i < remainder ? 1 : 0);
            
            for (int j = 0; j < assignCount && caseIndex < caseCount; j++) {
                Case caseInfo = shuffledCases.get(caseIndex++);
                
                try {
                    // 更新案件的催员信息
                    Case updateCase = new Case();
                    updateCase.setId(caseInfo.getId());
                    updateCase.setCollectorId(collector.getId());
                    updateCase.setTeamId(collector.getTeamId());
                    updateCase.setAgencyId(collector.getAgencyId());
                    updateCase.setAssignedAt(now);
                    caseMapper.updateById(updateCase);
                    
                    // 记录分配历史
                    CaseAssignment assignment = new CaseAssignment();
                    assignment.setCaseId(caseInfo.getId());
                    assignment.setCollectorId(collector.getId());
                    assignment.setAssignedBy(null); // 系统自动分配
                    assignment.setAssignedAt(now);
                    assignment.setIgnoreQueueLimit(false);
                    assignments.add(assignment);
                    
                    successCount++;
                    log.debug("案件重新分案成功：caseId={}, oldCollectorId={}, newCollectorId={}", 
                            caseInfo.getId(), caseInfo.getCollectorId(), collector.getId());
                    
                } catch (Exception e) {
                    log.error("案件重新分案失败：caseId={}, collectorId={}", caseInfo.getId(), collector.getId(), e);
                }
            }
        }
        
        // 批量插入分配记录
        if (!assignments.isEmpty()) {
            caseAssignmentMapper.batchInsert(assignments);
        }
        
        log.info("重新分案完成：案件数={}, 催员数={}, 成功={}", caseCount, collectorCount, successCount);
        return successCount;
    }
}

