package com.cco.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cco.mapper.CaseQueueMapper;
import com.cco.model.entity.CaseQueue;
import com.cco.service.QueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 案件队列服务实现
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@Service
public class QueueServiceImpl extends ServiceImpl<CaseQueueMapper, CaseQueue> implements QueueService {
    
    @Override
    public List<CaseQueue> listByTenantId(Long tenantId, Boolean isActive) {
        LambdaQueryWrapper<CaseQueue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CaseQueue::getTenantId, tenantId);
        
        if (isActive != null) {
            wrapper.eq(CaseQueue::getIsActive, isActive);
        }
        
        wrapper.orderByAsc(CaseQueue::getSortOrder);
        
        return this.list(wrapper);
    }
    
    @Override
    public boolean existsByQueueCode(Long tenantId, String queueCode, Long excludeId) {
        LambdaQueryWrapper<CaseQueue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CaseQueue::getTenantId, tenantId)
                .eq(CaseQueue::getQueueCode, queueCode);
        
        if (excludeId != null) {
            wrapper.ne(CaseQueue::getId, excludeId);
        }
        
        return this.count(wrapper) > 0;
    }
    
    @Override
    public boolean hasOverlappingRange(Long tenantId, Integer startDays, Integer endDays, Long excludeId) {
        // 获取该甲方的所有其他队列
        LambdaQueryWrapper<CaseQueue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CaseQueue::getTenantId, tenantId)
                .eq(CaseQueue::getIsActive, true);
        
        if (excludeId != null) {
            wrapper.ne(CaseQueue::getId, excludeId);
        }
        
        List<CaseQueue> existingQueues = this.list(wrapper);
        
        // 检查范围是否重合
        for (CaseQueue queue : existingQueues) {
            if (rangesOverlap(
                    startDays, endDays,
                    queue.getOverdueDaysStart(), queue.getOverdueDaysEnd()
            )) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查两个区间是否重合
     * 
     * @param start1 区间1起始值（null表示负无穷）
     * @param end1 区间1结束值（null表示正无穷）
     * @param start2 区间2起始值（null表示负无穷）
     * @param end2 区间2结束值（null表示正无穷）
     * @return 是否重合
     */
    private boolean rangesOverlap(Integer start1, Integer end1, Integer start2, Integer end2) {
        // 转换为可比较的值
        int s1 = start1 == null ? Integer.MIN_VALUE : start1;
        int e1 = end1 == null ? Integer.MAX_VALUE : end1;
        int s2 = start2 == null ? Integer.MIN_VALUE : start2;
        int e2 = end2 == null ? Integer.MAX_VALUE : end2;
        
        // 两个区间重合的条件：s1 <= e2 && s2 <= e1
        return s1 <= e2 && s2 <= e1;
    }
}














