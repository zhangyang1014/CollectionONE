package com.cco.service;

/**
 * 案件重新分案服务接口
 * 
 * @author CCO Team
 * @since 2025-01-22
 */
public interface CaseReassignService {
    
    /**
     * 执行重新分案任务
     * 根据已生效的配置，查找符合条件的案件并重新分配
     * 
     * @return 重新分案结果统计
     */
    ReassignResult executeReassign();
    
    /**
     * 重新分案结果
     */
    class ReassignResult {
        private int totalConfigs;      // 处理的配置数量
        private int totalCases;         // 处理的案件总数
        private int reassignedCases;    // 成功重新分案的案件数
        private int failedCases;        // 重新分案失败的案件数
        
        // Getters and Setters
        public int getTotalConfigs() {
            return totalConfigs;
        }
        
        public void setTotalConfigs(int totalConfigs) {
            this.totalConfigs = totalConfigs;
        }
        
        public int getTotalCases() {
            return totalCases;
        }
        
        public void setTotalCases(int totalCases) {
            this.totalCases = totalCases;
        }
        
        public int getReassignedCases() {
            return reassignedCases;
        }
        
        public void setReassignedCases(int reassignedCases) {
            this.reassignedCases = reassignedCases;
        }
        
        public int getFailedCases() {
            return failedCases;
        }
        
        public void setFailedCases(int failedCases) {
            this.failedCases = failedCases;
        }
    }
}

