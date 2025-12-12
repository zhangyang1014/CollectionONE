package com.cco.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cco.model.entity.CaseQueue;

import java.util.List;

/**
 * 案件队列服务接口
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
public interface QueueService extends IService<CaseQueue> {
    
    /**
     * 根据甲方ID获取队列列表
     * 
     * @param tenantId 甲方ID
     * @param isActive 是否只获取启用的队列（可选）
     * @return 队列列表
     */
    List<CaseQueue> listByTenantId(Long tenantId, Boolean isActive);
    
    /**
     * 检查队列编码是否已存在
     * 
     * @param tenantId 甲方ID
     * @param queueCode 队列编码
     * @param excludeId 排除的队列ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByQueueCode(Long tenantId, String queueCode, Long excludeId);
    
    /**
     * 检查逾期天数范围是否与其他队列重合
     * 
     * @param tenantId 甲方ID
     * @param startDays 起始天数（null表示负无穷）
     * @param endDays 结束天数（null表示正无穷）
     * @param excludeId 排除的队列ID（用于更新时检查）
     * @return 是否重合
     */
    boolean hasOverlappingRange(Long tenantId, Integer startDays, Integer endDays, Long excludeId);
}






































