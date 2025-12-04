package com.cco.controller;

import com.cco.common.response.ResponseData;
import com.cco.model.entity.MessageTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息模板配置管理Controller（Mock实现）
 * 
 * @author CCO Team
 * @date 2025-12-03
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@Tag(name = "消息模板配置管理", description = "提供消息模板的CRUD操作和查询功能")
public class MockMessageTemplateController {
    
    // Mock数据存储
    private final Map<Long, MessageTemplate> templateStore = new HashMap<>();
    private Long idCounter = 8L; // 从8开始，因为初始化数据有7条
    
    /**
     * 初始化Mock数据
     */
    public MockMessageTemplateController() {
        initMockData();
    }
    
    /**
     * 获取模板列表（控台端）
     */
    @Operation(summary = "获取模板列表", description = "支持分页、筛选、搜索")
    @GetMapping("/console/message-templates")
    public ResponseData<PageResult<MessageTemplateVO>> getTemplateList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "甲方ID") @RequestParam Long tenantId,
            @Parameter(description = "模板类型") @RequestParam(required = false) String templateType,
            @Parameter(description = "机构ID") @RequestParam(required = false) Long agencyId,
            @Parameter(description = "案件阶段") @RequestParam(required = false) String caseStage,
            @Parameter(description = "场景") @RequestParam(required = false) String scene,
            @Parameter(description = "时间点") @RequestParam(required = false) String timeSlot,
            @Parameter(description = "启用状态") @RequestParam(required = false) Boolean isEnabled,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword
    ) {
        log.info("获取模板列表 - tenantId: {}, templateType: {}, keyword: {}", tenantId, templateType, keyword);
        
        // 筛选数据
        List<MessageTemplate> filtered = templateStore.values().stream()
                .filter(t -> t.getTenantId().equals(tenantId))
                .filter(t -> templateType == null || t.getTemplateType().equals(templateType))
                .filter(t -> caseStage == null || t.getCaseStage().equals(caseStage))
                .filter(t -> scene == null || t.getScene().equals(scene))
                .filter(t -> timeSlot == null || t.getTimeSlot().equals(timeSlot))
                .filter(t -> isEnabled == null || t.getIsEnabled().equals(isEnabled))
                .filter(t -> agencyId == null || (t.getAgencyIds() != null && t.getAgencyIds().contains(agencyId)))
                .filter(t -> keyword == null || t.getTemplateName().contains(keyword) || t.getContent().contains(keyword))
                .sorted(Comparator.comparing(MessageTemplate::getSortOrder)
                        .thenComparing(Comparator.comparing(MessageTemplate::getCreatedAt).reversed()))
                .collect(Collectors.toList());
        
        // 分页
        int total = filtered.size();
        int start = (page - 1) * pageSize;
        
        // 边界检查：确保start不超出范围
        List<MessageTemplate> pageData;
        if (start >= total) {
            pageData = new ArrayList<>();
        } else {
            int end = Math.min(start + pageSize, total);
            pageData = filtered.subList(start, end);
        }
        
        // 转换为VO
        List<MessageTemplateVO> voList = pageData.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        PageResult<MessageTemplateVO> result = new PageResult<>();
        result.setTotal(total);
        result.setPage(page);
        result.setPageSize(pageSize);
        result.setList(voList);
        
        return ResponseData.success(result);
    }
    
    /**
     * 创建模板
     */
    @Operation(summary = "创建模板", description = "创建新的消息模板")
    @PostMapping("/console/message-templates")
    public ResponseData<MessageTemplate> createTemplate(@RequestBody MessageTemplateRequest request) {
        log.info("创建模板 - templateName: {}", request.getTemplateName());
        
        // 校验模板名称是否重复
        boolean nameExists = templateStore.values().stream()
                .anyMatch(t -> t.getTenantId().equals(request.getTenantId()) 
                        && t.getTemplateName().equals(request.getTemplateName()));
        if (nameExists) {
            return ResponseData.error(400, "Template name already exists");
        }
        
        // 创建新模板
        MessageTemplate template = new MessageTemplate();
        template.setId(idCounter++);
        template.setTenantId(request.getTenantId());
        template.setTemplateName(request.getTemplateName());
        template.setTemplateType(request.getTemplateType());
        template.setAgencyIds(request.getAgencyIds());
        template.setCaseStage(request.getCaseStage());
        template.setScene(request.getScene());
        template.setTimeSlot(request.getTimeSlot());
        template.setContent(request.getContent());
        template.setVariables(request.getVariables());
        template.setIsEnabled(request.getIsEnabled() != null ? request.getIsEnabled() : true);
        template.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        template.setUsageCount(0);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setCreatedBy(request.getCreatedBy());
        template.setUpdatedBy(request.getCreatedBy());
        
        templateStore.put(template.getId(), template);
        
        return ResponseData.success(template);
    }
    
    /**
     * 更新模板
     */
    @Operation(summary = "更新模板", description = "更新现有模板")
    @PutMapping("/console/message-templates/{id}")
    public ResponseData<MessageTemplate> updateTemplate(
            @PathVariable Long id,
            @RequestBody MessageTemplateRequest request
    ) {
        log.info("更新模板 - id: {}, templateName: {}", id, request.getTemplateName());
        
        MessageTemplate template = templateStore.get(id);
        if (template == null) {
            return ResponseData.error(404, "Template not found");
        }
        
        // 校验模板名称是否与其他模板重复
        boolean nameExists = templateStore.values().stream()
                .anyMatch(t -> !t.getId().equals(id)
                        && t.getTenantId().equals(request.getTenantId()) 
                        && t.getTemplateName().equals(request.getTemplateName()));
        if (nameExists) {
            return ResponseData.error(400, "Template name already exists");
        }
        
        // 更新模板
        template.setTemplateName(request.getTemplateName());
        template.setTemplateType(request.getTemplateType());
        template.setAgencyIds(request.getAgencyIds());
        template.setCaseStage(request.getCaseStage());
        template.setScene(request.getScene());
        template.setTimeSlot(request.getTimeSlot());
        template.setContent(request.getContent());
        template.setVariables(request.getVariables());
        template.setIsEnabled(request.getIsEnabled());
        template.setSortOrder(request.getSortOrder());
        template.setUpdatedAt(LocalDateTime.now());
        template.setUpdatedBy(request.getUpdatedBy());
        
        return ResponseData.success(template);
    }
    
    /**
     * 删除模板
     */
    @Operation(summary = "删除模板", description = "删除指定模板")
    @DeleteMapping("/console/message-templates/{id}")
    public ResponseData<Void> deleteTemplate(
            @PathVariable Long id,
            @RequestParam Long tenantId
    ) {
        log.info("删除模板 - id: {}, tenantId: {}", id, tenantId);
        
        MessageTemplate template = templateStore.get(id);
        if (template == null || !template.getTenantId().equals(tenantId)) {
            return ResponseData.error(404, "Template not found");
        }
        
        templateStore.remove(id);
        
        return ResponseData.success("Template deleted successfully", null);
    }
    
    /**
     * 切换启用状态
     */
    @Operation(summary = "切换启用状态", description = "启用或禁用模板")
    @PatchMapping("/console/message-templates/{id}/toggle")
    public ResponseData<Map<String, Object>> toggleTemplate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        Long tenantId = Long.valueOf(request.get("tenantId").toString());
        Boolean isEnabled = (Boolean) request.get("isEnabled");
        
        log.info("切换模板状态 - id: {}, isEnabled: {}", id, isEnabled);
        
        MessageTemplate template = templateStore.get(id);
        if (template == null || !template.getTenantId().equals(tenantId)) {
            return ResponseData.error(404, "Template not found");
        }
        
        template.setIsEnabled(isEnabled);
        template.setUpdatedAt(LocalDateTime.now());
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("isEnabled", isEnabled);
        
        return ResponseData.success(result);
    }
    
    /**
     * 获取可用模板列表（IM端）
     */
    @Operation(summary = "获取可用模板", description = "IM端获取催员可用的模板列表")
    @GetMapping("/message-templates")
    public ResponseData<List<MessageTemplateSimpleVO>> getAvailableTemplates(
            @RequestParam Long tenantId,
            @RequestParam Long collectorId,
            @RequestParam Long agencyId,
            @RequestParam(required = false) String caseStage,
            @RequestParam(required = false) String templateType,
            @RequestParam(required = false) String scene,
            @RequestParam(required = false) String timeSlot
    ) {
        log.info("获取可用模板 - tenantId: {}, collectorId: {}, agencyId: {}", tenantId, collectorId, agencyId);
        
        List<MessageTemplate> filtered = templateStore.values().stream()
                .filter(t -> t.getTenantId().equals(tenantId))
                .filter(t -> t.getIsEnabled())
                .filter(t -> {
                    // 组织模板：检查机构权限
                    if ("organization".equals(t.getTemplateType())) {
                        return t.getAgencyIds() == null || t.getAgencyIds().contains(agencyId);
                    }
                    // 个人模板：仅创建人可见
                    return collectorId.equals(t.getCreatedBy());
                })
                .filter(t -> caseStage == null || t.getCaseStage().equals(caseStage))
                .filter(t -> templateType == null || t.getTemplateType().equals(templateType))
                .filter(t -> scene == null || t.getScene().equals(scene))
                .filter(t -> timeSlot == null || t.getTimeSlot().equals(timeSlot))
                .sorted(Comparator.comparing(MessageTemplate::getSortOrder)
                        .thenComparing(Comparator.comparing(MessageTemplate::getCreatedAt).reversed()))
                .collect(Collectors.toList());
        
        List<MessageTemplateSimpleVO> voList = filtered.stream()
                .map(this::convertToSimpleVO)
                .collect(Collectors.toList());
        
        return ResponseData.success(voList);
    }
    
    /**
     * 获取可用变量列表
     * 变量来源于"甲方字段展示配置"中定义的标准字段
     */
    @Operation(summary = "获取可用变量", description = "获取系统预置的变量列表，基于甲方字段展示配置")
    @GetMapping("/message-templates/variables")
    public ResponseData<List<VariableVO>> getVariables() {
        List<VariableVO> variables = Arrays.asList(
                new VariableVO("客户名", "{客户名}", "user_name", "string", "张三", "客户真实姓名"),
                new VariableVO("贷款编号", "{贷款编号}", "case_code", "string", "BTSK-200100", "案件编号"),
                new VariableVO("逾期天数", "{逾期天数}", "overdue_days", "integer", "23", "当前逾期天数"),
                new VariableVO("到期日期", "{到期日期}", "due_date", "date", "2025-01-15", "应还款日期"),
                new VariableVO("贷款金额", "{贷款金额}", "loan_amount", "decimal", "50,000", "原始贷款金额"),
                new VariableVO("应还金额", "{应还金额}", "outstanding_amount", "decimal", "10,529", "应还未还金额"),
                new VariableVO("本金", "{本金}", "principal_amount", "decimal", "50,000", "贷款本金"),
                new VariableVO("罚息", "{罚息}", "late_fee", "decimal", "529", "逾期罚息"),
                new VariableVO("产品名称", "{产品名称}", "product_name", "string", "快速贷", "贷款产品名称"),
                new VariableVO("App名称", "{App名称}", "app_name", "string", "MegaPeso", "借款App名称")
        );
        
        return ResponseData.success(variables);
    }
    
    /**
     * 记录模板使用
     */
    @Operation(summary = "记录模板使用", description = "增加模板使用次数统计")
    @PostMapping("/message-templates/{id}/usage")
    public ResponseData<Void> recordUsage(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        MessageTemplate template = templateStore.get(id);
        if (template != null) {
            template.setUsageCount(template.getUsageCount() + 1);
            log.info("记录模板使用 - id: {}, 当前使用次数: {}", id, template.getUsageCount());
        }
        
        return ResponseData.success(null);
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 转换为详细VO
     */
    private MessageTemplateVO convertToVO(MessageTemplate template) {
        MessageTemplateVO vo = new MessageTemplateVO();
        vo.setId(template.getId());
        vo.setTenantId(template.getTenantId());
        vo.setTemplateName(template.getTemplateName());
        vo.setTemplateType(template.getTemplateType());
        vo.setAgencyIds(template.getAgencyIds());
        vo.setAgencyNames(formatAgencyNames(template.getAgencyIds()));
        vo.setCaseStage(template.getCaseStage());
        vo.setScene(template.getScene());
        vo.setTimeSlot(template.getTimeSlot());
        vo.setContent(template.getContent());
        vo.setVariables(template.getVariables());
        vo.setIsEnabled(template.getIsEnabled());
        vo.setSortOrder(template.getSortOrder());
        vo.setUsageCount(template.getUsageCount());
        vo.setCreatedAt(template.getCreatedAt());
        vo.setUpdatedAt(template.getUpdatedAt());
        vo.setCreatedBy(template.getCreatedBy());
        vo.setCreatedByName("管理员"); // Mock数据
        return vo;
    }
    
    /**
     * 转换为简单VO（IM端使用）
     */
    private MessageTemplateSimpleVO convertToSimpleVO(MessageTemplate template) {
        MessageTemplateSimpleVO vo = new MessageTemplateSimpleVO();
        vo.setId(template.getId());
        vo.setTitle(template.getTemplateName());
        vo.setType(template.getTemplateType());
        vo.setStage(template.getCaseStage());
        vo.setScene(template.getScene());
        vo.setTimeSlot(template.getTimeSlot());
        vo.setContent(template.getContent());
        vo.setVariables(template.getVariables());
        return vo;
    }
    
    /**
     * 格式化机构名称显示
     */
    private String formatAgencyNames(List<Long> agencyIds) {
        if (agencyIds == null || agencyIds.isEmpty()) {
            return "全部机构";
        }
        return agencyIds.size() + "个机构";
    }
    
    /**
     * 初始化Mock数据
     */
    private void initMockData() {
        // 模板1：早安问候 + 还款提醒
        MessageTemplate t1 = new MessageTemplate();
        t1.setId(1L);
        t1.setTenantId(1L);
        t1.setTemplateName("早安问候 + 还款提醒");
        t1.setTemplateType("organization");
        t1.setAgencyIds(null);
        t1.setCaseStage("S0");
        t1.setScene("greeting");
        t1.setTimeSlot("morning");
        t1.setContent("您好{客户名}，早上好！您在{App名称}的{产品名称}（贷款编号：{贷款编号}）已逾期{逾期天数}天，应还金额{应还金额}，请在{到期日期}前完成还款。谢谢您的配合！");
        t1.setVariables(Arrays.asList("客户名", "App名称", "产品名称", "贷款编号", "逾期天数", "应还金额", "到期日期"));
        t1.setIsEnabled(true);
        t1.setSortOrder(10);
        t1.setUsageCount(152);
        t1.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        t1.setUpdatedAt(LocalDateTime.of(2025, 1, 10, 15, 30));
        t1.setCreatedBy(1L);
        templateStore.put(1L, t1);
        
        // 模板2：下午催款提醒
        MessageTemplate t2 = new MessageTemplate();
        t2.setId(2L);
        t2.setTenantId(1L);
        t2.setTemplateName("下午催款提醒");
        t2.setTemplateType("organization");
        t2.setAgencyIds(Arrays.asList(1L, 2L, 3L));
        t2.setCaseStage("S1-3");
        t2.setScene("reminder");
        t2.setTimeSlot("afternoon");
        t2.setContent("{客户名}您好，您的{产品名称}（贷款编号：{贷款编号}）逾期已{逾期天数}天，贷款金额{贷款金额}，未还金额{应还金额}（本金{本金}+罚息{罚息}），请今日内完成还款，如有困难请联系我们！");
        t2.setVariables(Arrays.asList("客户名", "产品名称", "贷款编号", "逾期天数", "贷款金额", "应还金额", "本金", "罚息"));
        t2.setIsEnabled(true);
        t2.setSortOrder(20);
        t2.setUsageCount(89);
        t2.setCreatedAt(LocalDateTime.of(2025, 1, 2, 14, 0));
        t2.setUpdatedAt(LocalDateTime.of(2025, 1, 12, 9, 15));
        t2.setCreatedBy(1L);
        templateStore.put(2L, t2);
        
        // 模板3：晚间强度提醒
        MessageTemplate t3 = new MessageTemplate();
        t3.setId(3L);
        t3.setTenantId(1L);
        t3.setTemplateName("晚间强度提醒");
        t3.setTemplateType("organization");
        t3.setAgencyIds(Arrays.asList(1L, 2L));
        t3.setCaseStage("S3+");
        t3.setScene("strong");
        t3.setTimeSlot("evening");
        t3.setContent("{客户名}，您在{App名称}的贷款（{贷款编号}）已严重逾期{逾期天数}天，未还金额{应还金额}，如不立即还款我们将采取法律措施！");
        t3.setVariables(Arrays.asList("客户名", "App名称", "贷款编号", "逾期天数", "应还金额"));
        t3.setIsEnabled(true);
        t3.setSortOrder(30);
        t3.setUsageCount(34);
        t3.setCreatedAt(LocalDateTime.of(2025, 1, 3, 16, 0));
        t3.setUpdatedAt(LocalDateTime.of(2025, 1, 14, 11, 20));
        t3.setCreatedBy(1L);
        templateStore.put(3L, t3);
        
        // 模板4：承诺还款确认
        MessageTemplate t4 = new MessageTemplate();
        t4.setId(4L);
        t4.setTenantId(1L);
        t4.setTemplateName("承诺还款确认");
        t4.setTemplateType("organization");
        t4.setAgencyIds(null);
        t4.setCaseStage("C");
        t4.setScene("greeting");
        t4.setTimeSlot("afternoon");
        t4.setContent("{客户名}您好，感谢您承诺在{到期日期}前还款{应还金额}。我们会持续关注，期待您的履约。");
        t4.setVariables(Arrays.asList("客户名", "到期日期", "应还金额"));
        t4.setIsEnabled(true);
        t4.setSortOrder(40);
        t4.setUsageCount(67);
        t4.setCreatedAt(LocalDateTime.of(2025, 1, 4, 10, 30));
        t4.setUpdatedAt(LocalDateTime.of(2025, 1, 15, 14, 45));
        t4.setCreatedBy(1L);
        templateStore.put(4L, t4);
        
        // 模板5：逾期初期温和提醒
        MessageTemplate t5 = new MessageTemplate();
        t5.setId(5L);
        t5.setTenantId(1L);
        t5.setTemplateName("逾期初期温和提醒");
        t5.setTemplateType("organization");
        t5.setAgencyIds(null);
        t5.setCaseStage("S1-3");
        t5.setScene("reminder");
        t5.setTimeSlot("morning");
        t5.setContent("尊敬的{客户名}，您好！您在{App名称}的{产品名称}（贷款编号：{贷款编号}）已逾期{逾期天数}天，应还金额{应还金额}。为避免影响个人信用，请尽快安排还款。");
        t5.setVariables(Arrays.asList("客户名", "App名称", "产品名称", "贷款编号", "逾期天数", "应还金额"));
        t5.setIsEnabled(true);
        t5.setSortOrder(50);
        t5.setUsageCount(123);
        t5.setCreatedAt(LocalDateTime.of(2025, 1, 5, 9, 0));
        t5.setUpdatedAt(LocalDateTime.of(2025, 1, 16, 16, 10));
        t5.setCreatedBy(1L);
        templateStore.put(5L, t5);
        
        // 模板6：贷款金额明细提醒
        MessageTemplate t6 = new MessageTemplate();
        t6.setId(6L);
        t6.setTenantId(1L);
        t6.setTemplateName("贷款金额明细提醒");
        t6.setTemplateType("organization");
        t6.setAgencyIds(null);
        t6.setCaseStage("S1-3");
        t6.setScene("reminder");
        t6.setTimeSlot("afternoon");
        t6.setContent("{客户名}您好，您的贷款详情：贷款金额{贷款金额}，本金{本金}，罚息{罚息}，应还总额{应还金额}，请在{到期日期}前还款。");
        t6.setVariables(Arrays.asList("客户名", "贷款金额", "本金", "罚息", "应还金额", "到期日期"));
        t6.setIsEnabled(true);
        t6.setSortOrder(60);
        t6.setUsageCount(67);
        t6.setCreatedAt(LocalDateTime.of(2025, 1, 6, 8, 30));
        t6.setUpdatedAt(LocalDateTime.of(2025, 1, 17, 12, 0));
        t6.setCreatedBy(1L);
        templateStore.put(6L, t6);
        
        // 模板7：个人问候（上午）
        MessageTemplate t7 = new MessageTemplate();
        t7.setId(7L);
        t7.setTenantId(1L);
        t7.setTemplateName("个人问候（上午）");
        t7.setTemplateType("personal");
        t7.setAgencyIds(null);
        t7.setCaseStage("S0");
        t7.setScene("greeting");
        t7.setTimeSlot("morning");
        t7.setContent("您好{客户名}，早上好！关于您在{App名称}的{产品名称}还款事宜想跟您沟通一下，现在方便吗？");
        t7.setVariables(Arrays.asList("客户名", "App名称", "产品名称"));
        t7.setIsEnabled(true);
        t7.setSortOrder(100);
        t7.setUsageCount(45);
        t7.setCreatedAt(LocalDateTime.of(2025, 1, 7, 8, 30));
        t7.setUpdatedAt(LocalDateTime.of(2025, 1, 18, 12, 0));
        t7.setCreatedBy(1001L);
        templateStore.put(7L, t7);
        
        // 模板8：还款确认模板
        MessageTemplate t8 = new MessageTemplate();
        t8.setId(8L);
        t8.setTenantId(1L);
        t8.setTemplateName("还款确认模板");
        t8.setTemplateType("personal");
        t8.setAgencyIds(null);
        t8.setCaseStage("C");
        t8.setScene("greeting");
        t8.setTimeSlot("afternoon");
        t8.setContent("{客户名}您好，我们已收到您的还款{应还金额}，感谢您的配合！");
        t8.setVariables(Arrays.asList("客户名", "应还金额"));
        t8.setIsEnabled(true);
        t8.setSortOrder(110);
        t8.setUsageCount(78);
        t8.setCreatedAt(LocalDateTime.of(2025, 1, 8, 15, 0));
        t8.setUpdatedAt(LocalDateTime.of(2025, 1, 19, 10, 30));
        t8.setCreatedBy(1001L);
        templateStore.put(8L, t8);
    }
    
    // ==================== 内部类 ====================
    
    @Data
    public static class PageResult<T> {
        private Integer total;
        private Integer page;
        private Integer pageSize;
        private List<T> list;
    }
    
    @Data
    public static class MessageTemplateRequest {
        private Long tenantId;
        private String templateName;
        private String templateType;
        private List<Long> agencyIds;
        private String caseStage;
        private String scene;
        private String timeSlot;
        private String content;
        private List<String> variables;
        private Boolean isEnabled;
        private Integer sortOrder;
        private Long createdBy;
        private Long updatedBy;
    }
    
    @Data
    public static class MessageTemplateVO {
        private Long id;
        private Long tenantId;
        private String templateName;
        private String templateType;
        private List<Long> agencyIds;
        private String agencyNames;
        private String caseStage;
        private String scene;
        private String timeSlot;
        private String content;
        private List<String> variables;
        private Boolean isEnabled;
        private Integer sortOrder;
        private Integer usageCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Long createdBy;
        private String createdByName;
    }
    
    @Data
    public static class MessageTemplateSimpleVO {
        private Long id;
        private String title;
        private String type;
        private String stage;
        private String scene;
        private String timeSlot;
        private String content;
        private List<String> variables;
    }
    
    @Data
    public static class VariableVO {
        private String name;
        private String key;
        private String fieldKey; // 字段标识，对应甲方字段展示配置中的field_key
        private String type;
        private String example;
        private String description;
        
        public VariableVO(String name, String key, String fieldKey, String type, String example, String description) {
            this.name = name;
            this.key = key;
            this.fieldKey = fieldKey;
            this.type = type;
            this.example = example;
            this.description = description;
        }
    }
}

