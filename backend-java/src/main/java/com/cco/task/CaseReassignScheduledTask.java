package com.cco.task;

import com.cco.service.CaseReassignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 案件重新分案定时任务
 * 每天02:00执行
 * 
 * @author CCO Team
 * @since 2025-01-22
 */
@Slf4j
@Component
public class CaseReassignScheduledTask {
    
    @Autowired
    private CaseReassignService caseReassignService;
    
    /**
     * 执行案件重新分案任务
     * 每天02:00执行
     * cron表达式：秒 分 时 日 月 周
     * 0 0 2 * * ? 表示每天02:00:00执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void executeReassignTask() {
        log.info("========== 定时任务触发：开始执行案件重新分案任务 ==========");
        
        try {
            CaseReassignService.ReassignResult result = caseReassignService.executeReassign();
            
            log.info("========== 定时任务完成：处理配置={}, 处理案件={}, 成功={}, 失败={} ==========",
                    result.getTotalConfigs(), result.getTotalCases(),
                    result.getReassignedCases(), result.getFailedCases());
            
        } catch (Exception e) {
            log.error("========== 定时任务执行失败 ==========", e);
        }
    }
}


