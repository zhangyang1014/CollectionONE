package com.cco.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cco.mapper.CaseReassignConfigMapper;
import com.cco.model.entity.CaseReassignConfig;
import com.cco.service.CaseReassignConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * 案件重新分案配置服务实现
 * 
 * @author CCO Team
 * @since 2025-01-22
 */
@Slf4j
@Service
public class CaseReassignConfigServiceImpl extends ServiceImpl<CaseReassignConfigMapper, CaseReassignConfig> 
        implements CaseReassignConfigService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CaseReassignConfig createConfig(CaseReassignConfig config) {
        log.info("创建重新分案配置，tenantId={}, configType={}, targetId={}, reassignDays={}, teamIds={}", 
                config.getTenantId(), config.getConfigType(), config.getTargetId(), 
                config.getReassignDays(), config.getTeamIds());
        
        // 设置生效日期为T+1
        config.setEffectiveDate(LocalDate.now().plusDays(1));
        
        // 如果未设置isActive，默认为true
        if (config.getIsActive() == null) {
            config.setIsActive(true);
        }
        
        baseMapper.insert(config);
        log.info("创建重新分案配置成功，id={}, effectiveDate={}", config.getId(), config.getEffectiveDate());
        
        return config;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CaseReassignConfig updateConfig(CaseReassignConfig config) {
        log.info("更新重新分案配置，id={}", config.getId());
        
        CaseReassignConfig existing = baseMapper.selectById(config.getId());
        if (existing == null) {
            throw new RuntimeException("配置不存在");
        }
        
        // 如果修改了关键字段，需要重新设置生效日期为T+1
        if (!existing.getReassignDays().equals(config.getReassignDays()) 
                || !existing.getConfigType().equals(config.getConfigType())
                || !existing.getTargetId().equals(config.getTargetId())) {
            config.setEffectiveDate(LocalDate.now().plusDays(1));
            log.info("配置关键字段已修改，重新设置生效日期为T+1，effectiveDate={}", config.getEffectiveDate());
        }
        
        baseMapper.updateById(config);
        log.info("更新重新分案配置成功，id={}", config.getId());
        
        return config;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long id) {
        log.info("删除重新分案配置，id={}", id);
        
        CaseReassignConfig config = baseMapper.selectById(id);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }
        
        baseMapper.deleteById(id);
        log.info("删除重新分案配置成功，id={}", id);
    }
    
    @Override
    public List<CaseReassignConfig> listConfigs(Long tenantId, String configType) {
        log.info("查询重新分案配置列表，tenantId={}, configType={}", tenantId, configType);
        
        QueryWrapper<CaseReassignConfig> wrapper = new QueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq("tenant_id", tenantId);
        }
        if (configType != null && !configType.isEmpty()) {
            wrapper.eq("config_type", configType);
        }
        wrapper.orderByAsc("config_type", "target_id");
        
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    public List<CaseReassignConfig> getEffectiveConfigs(Long tenantId) {
        log.info("查询已生效的重新分案配置，tenantId={}", tenantId);
        
        LocalDate today = LocalDate.now();
        return baseMapper.selectEffectiveConfigs(tenantId, today);
    }
    
    @Override
    public List<CaseReassignConfig> checkConflictConfigs(Long tenantId, Long queueId, List<Long> teamIds) {
        log.info("检查队列-小组维度重复配置，tenantId={}, queueId={}, teamIds={}", tenantId, queueId, teamIds);
        
        // 查询该队列下的所有配置
        QueryWrapper<CaseReassignConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("tenant_id", tenantId)
               .eq("config_type", "queue")
               .eq("target_id", queueId);
        
        List<CaseReassignConfig> queueConfigs = baseMapper.selectList(wrapper);
        
        if (queueConfigs.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 如果teamIds为空或null，表示针对该队列下所有小组，与任何配置都冲突
        if (teamIds == null || teamIds.isEmpty()) {
            return queueConfigs;
        }
        
        // 检查是否有配置与当前选择的小组冲突
        Set<Long> newTeamIds = new HashSet<>(teamIds);
        List<CaseReassignConfig> conflicts = new ArrayList<>();
        
        for (CaseReassignConfig config : queueConfigs) {
            String configTeamIdsStr = config.getTeamIds();
            
            // 如果配置的teamIds为空，表示针对该队列下所有小组，与任何选择都冲突
            if (configTeamIdsStr == null || configTeamIdsStr.isEmpty()) {
                conflicts.add(config);
                continue;
            }
            
            // 解析配置的teamIds
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                List<Long> configTeamIds = objectMapper.readValue(configTeamIdsStr, new TypeReference<List<Long>>() {});
                
                // 检查是否有交集
                Set<Long> configTeamIdsSet = new HashSet<>(configTeamIds);
                configTeamIdsSet.retainAll(newTeamIds);
                
                if (!configTeamIdsSet.isEmpty()) {
                    conflicts.add(config);
                }
            } catch (Exception e) {
                log.error("解析配置的teamIds失败，configId={}, teamIds={}", config.getId(), configTeamIdsStr, e);
            }
        }
        
        log.info("检查结果：找到{}个冲突配置", conflicts.size());
        return conflicts;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConflictConfigs(Long tenantId, Long queueId, List<Long> teamIds) {
        log.info("删除冲突配置，tenantId={}, queueId={}, teamIds={}", tenantId, queueId, teamIds);
        
        List<CaseReassignConfig> conflicts = checkConflictConfigs(tenantId, queueId, teamIds);
        
        for (CaseReassignConfig conflict : conflicts) {
            baseMapper.deleteById(conflict.getId());
            log.info("删除冲突配置，id={}", conflict.getId());
        }
        
        log.info("删除冲突配置完成，共删除{}个配置", conflicts.size());
    }
}

