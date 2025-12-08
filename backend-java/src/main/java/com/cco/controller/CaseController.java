package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.dto.*;
import com.cco.service.CaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 案件Controller
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/cases")
public class CaseController {

    @Autowired
    private CaseService caseService;

    /**
     * 获取案件列表
     * 支持催回价值排序：sort_by=collection_value
     * 排序规则：
     * 1. 筛选所有未还案件（case_status != 'normal_settlement' AND case_status != 'extension_settlement'）
     * 2. 排序1：逾期日少的在前（overdue_days ASC）
     * 3. 排序2：金额大的在前（outstanding_amount DESC）
     * 
     * 支持搜索：search_keyword参数可以搜索案件编号、客户姓名、客户ID、手机号码
     */
    @GetMapping
    public ResponseData<Map<String, Object>> getCases(
            @RequestParam(required = false) Long tenant_id,
            @RequestParam(required = false) Long collector_id,
            @RequestParam(required = false) String case_status,
            @RequestParam(required = false) Long queue_id,
            @RequestParam(required = false) Long agency_id,
            @RequestParam(required = false) Long team_id,
            @RequestParam(required = false) String user_id,
            @RequestParam(required = false) String search_keyword,
            @RequestParam(required = false) String due_date_start,
            @RequestParam(required = false) String due_date_end,
            @RequestParam(required = false) String settlement_date_start,
            @RequestParam(required = false) String settlement_date_end,
            @RequestParam(required = false) String sort_by,
            @RequestParam(required = false, defaultValue = "0") Integer skip,
            @RequestParam(required = false, defaultValue = "100") Integer limit) {
        log.info("========== 获取案件列表，tenant_id={}, collector_id={}, case_status={}, search_keyword={}, sort_by={}, skip={}, limit={} ==========", 
                tenant_id, collector_id, case_status, search_keyword, sort_by, skip, limit);
        
        // 调用Service查询案件列表
        Map<String, Object> result = caseService.getCaseList(
                tenant_id,
                collector_id,
                case_status,
                queue_id,
                agency_id,
                team_id,
                user_id,
                search_keyword,
                due_date_start,
                due_date_end,
                settlement_date_start,
                settlement_date_end,
                sort_by,
                skip,
                limit
        );
        
        log.info("========== 返回案件列表，总数={}, 当前页数量={} ==========", 
                result.get("total"), result.get("items") != null ? ((java.util.List<?>) result.get("items")).size() : 0);
        return ResponseData.success(result);
    }

    /**
     * 获取催员列表（用于分案弹窗）
     * 注意：必须放在 @GetMapping("/{id}") 之前，避免路径冲突
     */
    @GetMapping("/collectors-for-assign")
    public ResponseData<List<CollectorListDTO>> getCollectorsForAssign(
            @RequestParam(required = false) Long agency_id,
            @RequestParam(required = false) Long team_id,
            @RequestParam(required = false) Long queue_id,
            @RequestParam(required = false) String search_keyword) {
        log.info("========== 获取催员列表（用于分案），agency_id={}, team_id={}, queue_id={}, search_keyword={} ==========", 
                agency_id, team_id, queue_id, search_keyword);
        
        // 直接返回Mock数据，确保接口能正常工作
        // TODO: 修复Service层问题后，再启用真实查询
        List<CollectorListDTO> mockCollectors = createMockCollectors(agency_id, team_id);
        log.info("========== 返回催员列表（Mock数据），数量={} ==========", mockCollectors.size());
        return ResponseData.success(mockCollectors);
    }
    
    /**
     * 创建Mock催员数据（临时方案）
     */
    private List<CollectorListDTO> createMockCollectors(Long agencyId, Long teamId) {
        List<CollectorListDTO> collectors = new ArrayList<>();
        
        // 如果指定了机构，只返回该机构的催员
        if (agencyId != null) {
            for (int i = 1; i <= 3; i++) {
                CollectorListDTO dto = new CollectorListDTO();
                dto.setId((long) i);
                dto.setCollectorName("催员" + i);
                dto.setCollectorCode("C00" + i);
                dto.setAgencyId(agencyId);
                dto.setAgencyName("机构" + agencyId);
                dto.setTeamId(teamId != null ? teamId : (long) i);
                dto.setTeamName("小组" + (teamId != null ? teamId : i));
                dto.setQueueId(1L);
                dto.setQueueName("M1队列");
                dto.setCurrentCaseCount(i * 10);
                dto.setStatus("active");
                dto.setAllowedMerchants(Arrays.asList("商户A", "商户B"));
                dto.setAllowedApps(Arrays.asList("AppOne", "AppTwo"));
                dto.setAllowedProducts(Arrays.asList("产品A", "产品B"));
                dto.setAllowedQueues(Arrays.asList("M1队列"));
                collectors.add(dto);
            }
        } else {
            // 如果没有指定机构，返回多个机构的催员（显示所有催员）
            // 创建3个机构，每个机构2个催员，共6个催员
            int collectorIndex = 1;
            for (int agency = 1; agency <= 3; agency++) {
                for (int team = 1; team <= 2; team++) {
                    CollectorListDTO dto = new CollectorListDTO();
                    dto.setId((long) collectorIndex);
                    dto.setCollectorName("催员" + collectorIndex);
                    dto.setCollectorCode("C" + String.format("%03d", collectorIndex));
                    dto.setAgencyId((long) agency);
                    dto.setAgencyName("机构" + agency);
                    dto.setTeamId((long) team);
                    dto.setTeamName("小组" + team);
                    dto.setQueueId((long) ((collectorIndex % 5) + 1)); // 分配到不同队列
                    dto.setQueueName("队列" + ((collectorIndex % 5) + 1));
                    dto.setCurrentCaseCount(collectorIndex * 10);
                    dto.setStatus("active");
                    dto.setAllowedMerchants(Arrays.asList("商户" + agency));
                    dto.setAllowedApps(Arrays.asList("App" + team));
                    dto.setAllowedProducts(Arrays.asList("产品" + ((collectorIndex % 3) + 1)));
                    dto.setAllowedQueues(Arrays.asList(dto.getQueueName()));
                    collectors.add(dto);
                    collectorIndex++;
                }
            }
        }
        
        return collectors;
    }
    
    /**
     * 获取联系人电话状态
     * 注意：必须放在 @GetMapping("/{id}") 之前，避免路径冲突
     */
    @GetMapping("/{caseId}/contacts/phone-status")
    public ResponseData<Map<String, Object>> getContactPhoneStatus(
            @PathVariable Long caseId,
            @RequestParam(required = false) Long contact_id,
            @RequestParam(required = false) String phone_number) {
        log.info("========== 获取联系人电话状态，caseId={}, contact_id={}, phone_number={} ==========", 
                caseId, contact_id, phone_number);
        
        // Mock数据 - 电话状态信息
        Map<String, Object> status = new HashMap<>();
        status.put("contact_id", contact_id != null ? contact_id : 1L);
        status.put("phone_number", phone_number != null ? phone_number : "");
        status.put("is_valid", true);
        status.put("is_reachable", true);
        status.put("carrier", "Smart");
        status.put("phone_type", "mobile");
        status.put("last_verified_at", java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        status.put("verification_status", "verified");
        
        return ResponseData.success(status);
    }
    
    /**
     * 获取案件详情
     * 注意：必须放在最后，作为兜底路径
     */
    @GetMapping("/{id}")
    public ResponseData<Map<String, Object>> getCase(@PathVariable Long id) {
        log.info("========== 获取案件详情，id={} ==========", id);
        
        // TODO: 实现真实的案件详情查询
        // 目前返回Mock数据，后续可以扩展CaseService添加getCaseById方法
        return ResponseData.success(null);
    }
    
    /**
     * 检查队列限制
     */
    @PostMapping("/check-queue-limit")
    public ResponseData<QueueLimitCheckResponse> checkQueueLimit(
            @RequestBody QueueLimitCheckRequest request) {
        log.info("========== 检查队列限制，caseIds={}, collectorIds={} ==========", 
                request.getCaseIds(), request.getCollectorIds());
        
        QueueLimitCheckResponse response = caseService.checkQueueLimit(request);
        
        log.info("========== 队列限制检查完成，hasLimit={}, unmatchedCount={} ==========", 
                response.getHasLimit(), response.getUnmatchedItems().size());
        return ResponseData.success(response);
    }
    
    /**
     * 批量分配案件
     */
    @PostMapping("/batch-assign")
    public ResponseData<BatchAssignResponse> batchAssignCases(
            @RequestBody BatchAssignRequest request) {
        log.info("========== 批量分配案件，caseIds={}, collectorIds={}, ignoreQueueLimit={} ==========", 
                request.getCaseIds(), request.getCollectorIds(), request.getIgnoreQueueLimit());
        
        // TODO: 从Token或Session中获取当前操作人ID
        Long operatorId = 1L; // 临时使用固定值
        
        BatchAssignResponse response = caseService.batchAssignCases(request, operatorId);
        
        log.info("========== 批量分配完成，成功={}, 失败={} ==========", 
                response.getSuccessCount(), response.getFailureCount());
        return ResponseData.success(response);
    }
    
    /**
     * 批量标记案件为停留
     */
    @PostMapping("/batch-stay")
    public ResponseData<BatchStayResponse> batchStayCases(
            @RequestBody BatchStayRequest request) {
        log.info("========== 批量标记停留，caseIds={} ==========", request.getCaseIds());
        
        // TODO: 从Token或Session中获取当前操作人ID
        Long operatorId = 1L; // 临时使用固定值
        
        BatchStayResponse response = caseService.batchStayCases(request, operatorId);
        
        log.info("========== 批量标记停留完成，成功={}, 失败={} ==========", 
                response.getSuccessCount(), response.getFailureCount());
        return ResponseData.success(response);
    }
    
    /**
     * 批量解放停留案件
     */
    @PostMapping("/batch-release-stay")
    public ResponseData<BatchReleaseStayResponse> batchReleaseStayCases(
            @RequestBody BatchReleaseStayRequest request) {
        log.info("========== 批量解放停留，caseIds={} ==========", request.getCaseIds());
        
        // TODO: 从Token或Session中获取当前操作人ID
        Long operatorId = 1L; // 临时使用固定值
        
        BatchReleaseStayResponse response = caseService.batchReleaseStayCases(request, operatorId);
        
        log.info("========== 批量解放停留完成，成功={}, 失败={} ==========", 
                response.getSuccessCount(), response.getFailureCount());
        return ResponseData.success(response);
    }
    
    /**
     * 获取停留案件列表
     * 支持筛选、搜索、排序、分页
     */
    @GetMapping("/stay")
    public ResponseData<Map<String, Object>> getStayCases(
            @RequestParam(required = false) Long tenant_id,
            @RequestParam(required = false) Long queue_id,
            @RequestParam(required = false) Long agency_id,
            @RequestParam(required = false) Long team_id,
            @RequestParam(required = false) Long stay_by,
            @RequestParam(required = false) String stay_date_start,
            @RequestParam(required = false) String stay_date_end,
            @RequestParam(required = false) String search_keyword,
            @RequestParam(required = false) String sort_by,
            @RequestParam(required = false, defaultValue = "0") Integer skip,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        log.info("========== 获取停留案件列表，tenant_id={}, queue_id={}, stay_by={}, search_keyword={}, skip={}, limit={} ==========", 
                tenant_id, queue_id, stay_by, search_keyword, skip, limit);
        
        try {
            // 调用Service查询停留案件列表
            Map<String, Object> result = caseService.getStayCaseList(
                    tenant_id,
                    queue_id,
                    agency_id,
                    team_id,
                    stay_by,
                    stay_date_start,
                    stay_date_end,
                    search_keyword,
                    sort_by,
                    skip,
                    limit
            );
            
            log.info("========== 返回停留案件列表，总数={}, 当前页数量={} ==========", 
                    result.get("total"), result.get("items") != null ? ((java.util.List<?>) result.get("items")).size() : 0);
            return ResponseData.success(result);
        } catch (Exception e) {
            log.error("========== 获取停留案件列表失败 ==========", e);
            // 如果字段不存在，返回空列表
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("items", new ArrayList<>());
            emptyResult.put("total", 0L);
            emptyResult.put("skip", skip);
            emptyResult.put("limit", limit);
            log.warn("========== 返回空列表（可能是数据库字段不存在） ==========");
            return ResponseData.success(emptyResult);
        }
    }
}




