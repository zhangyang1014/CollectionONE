package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Mock权限管理Controller（临时，用于前端开发）
 * 真实的权限管理在数据库配置完成后启用
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/permissions")
public class MockPermissionController {
    
    // 内存存储：保存用户修改的权限配置
    // Key: tenantId_roleCode_permissionItemId, Value: permissionLevel
    private static final Map<String, String> permissionConfigCache = new HashMap<>();
    
    /**
     * 清空权限配置缓存（用于测试或重置）
     */
    @PostMapping("/configs/clear-cache")
    public ResponseData<Map<String, Object>> clearPermissionCache() {
        permissionConfigCache.clear();
        log.info("权限配置缓存已清空");
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "缓存已清空");
        return ResponseData.success(result);
    }

    @GetMapping("/modules")
    public ResponseData<List<Map<String, Object>>> getPermissionModules(
            @RequestParam(required = false) Boolean isActive
    ) {
        log.info("获取权限模块列表（Mock），isActive={}", isActive);
        
        List<Map<String, Object>> modules = new ArrayList<>();
        modules.add(createModule(1L, "工作台", "dashboard", "工作台相关功能", 1, true));
        modules.add(createModule(2L, "数据看板", "data_dashboard", "数据看板相关功能", 2, true));
        modules.add(createModule(3L, "案件管理", "case_management", "案件管理相关功能", 3, true));
        modules.add(createModule(4L, "字段配置", "field_config", "字段配置相关功能", 4, true));
        modules.add(createModule(5L, "人员与机构管理", "organization", "人员与机构管理相关功能", 5, true));
        modules.add(createModule(6L, "渠道配置", "channel_config", "渠道配置相关功能", 6, true));
        modules.add(createModule(7L, "系统管理", "system_management", "系统管理相关功能", 7, true));
        
        return ResponseData.success(modules);
    }

    @GetMapping("/items")
    public ResponseData<List<Map<String, Object>>> getPermissionItems(
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Boolean isActive
    ) {
        log.info("获取权限项列表（Mock），moduleId={}, isActive={}", moduleId, isActive);
        
        // 返回所有权限项（与matrix方法保持一致）
        List<Map<String, Object>> items = buildAllPermissionItems();
        
        // 如果指定了moduleId，进行过滤
        if (moduleId != null) {
            items = items.stream()
                    .filter(item -> moduleId.equals(item.get("module_id")))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        return ResponseData.success(items);
    }
    
    /**
     * 构建所有权限项（供多个方法复用）
     */
    private List<Map<String, Object>> buildAllPermissionItems() {
        List<Map<String, Object>> items = new ArrayList<>();
        int itemId = 1;
        
        // 1. 工作台模块
        items.add(createItem((long) itemId++, 1L, "访问工作台", "dashboard:view", 
            "可以访问工作台菜单，查看并编辑工作台页面内容", 
            "可以访问工作台菜单，查看工作台页面内容，但无法编辑任何数据", 1, true));
        
        // 2. 数据看板模块
        items.add(createItem((long) itemId++, 2L, "单催员业绩看板", "dashboard:performance:collector", 
            "可以访问单催员业绩看板菜单，查看催员业绩数据统计和图表，且可以导出数据", 
            "可以访问单催员业绩看板菜单，查看催员业绩数据统计和图表，但无法编辑或导出数据", 1, true));
        items.add(createItem((long) itemId++, 2L, "空闲催员监控", "dashboard:idle:monitor", 
            "可以访问空闲催员监控菜单，查看空闲催员列表和监控数据，且可以分配任务", 
            "可以访问空闲催员监控菜单，查看空闲催员列表和监控数据，但无法编辑或分配任务", 2, true));
        
        // 3. 案件管理模块
        items.add(createItem((long) itemId++, 3L, "案件列表", "case:list:view", 
            "可以访问案件列表菜单，查看案件列表数据，且可以进行添加、编辑、删除等操作", 
            "可以访问案件列表菜单，查看案件列表数据，但无法进行添加、编辑、删除等操作", 1, true));
        items.add(createItem((long) itemId++, 3L, "案件详情", "case:detail:view", 
            "可以查看案件详情页面，查看案件的所有详细信息，且可以编辑案件信息", 
            "可以查看案件详情页面，查看案件的所有详细信息，但无法编辑案件信息", 2, true));
        items.add(createItem((long) itemId++, 3L, "添加案件", "case:add", 
            "可以访问案件列表菜单，且可以在案件列表页面点击添加案件按钮，创建新的案件记录", 
            "可以访问案件列表菜单，但无法看到或点击添加案件按钮", 3, true));
        items.add(createItem((long) itemId++, 3L, "编辑案件", "case:edit", 
            "可以访问案件详情页面，且可以编辑案件的基本信息、状态、金额等字段", 
            "可以访问案件详情页面，但无法编辑案件信息，只能查看", 4, true));
        items.add(createItem((long) itemId++, 3L, "删除案件", "case:delete", 
            "可以访问案件列表菜单，且可以在案件列表或详情页面删除案件记录", 
            "可以访问案件列表菜单，但无法看到或点击删除案件按钮", 5, true));
        items.add(createItem((long) itemId++, 3L, "分配案件", "case:assign", 
            "可以访问案件列表菜单，且可以将案件分配给指定的催员或小组，修改案件的分配状态", 
            "可以访问案件列表菜单，但无法看到或使用分配案件功能", 6, true));
        items.add(createItem((long) itemId++, 3L, "查看催记", "case:notes:view", 
            "可以访问案件详情页面，且可以查看该案件的所有催收记录历史，并可以编辑或删除催记", 
            "可以访问案件详情页面，且可以查看该案件的所有催收记录历史，但无法编辑或删除催记", 7, true));
        items.add(createItem((long) itemId++, 3L, "添加催记", "case:notes:add", 
            "可以访问案件详情页面，且可以为案件添加新的催收记录，记录催收过程和结果", 
            "可以访问案件详情页面，但无法看到或点击添加催记按钮", 8, true));
        items.add(createItem((long) itemId++, 3L, "筛选器配置", "case:filter:config", 
            "可以访问案件列表菜单，且可以配置案件列表的筛选器选项，自定义筛选条件", 
            "可以访问案件列表菜单，但无法看到或使用筛选器配置功能", 9, true));
        items.add(createItem((long) itemId++, 3L, "导出案件", "case:export", 
            "可以访问案件列表菜单，且可以导出案件列表数据为Excel或其他格式文件", 
            "可以访问案件列表菜单，但无法看到或点击导出案件按钮", 10, true));
        items.add(createItem((long) itemId++, 3L, "自动化分案", "case:auto:assignment", 
            "可以访问自动化分案菜单，查看自动化分案页面，且可以编辑分案策略", 
            "可以访问自动化分案菜单，查看自动化分案页面，但无法编辑分案策略", 11, true));
        items.add(createItem((long) itemId++, 3L, "分案策略管理", "case:auto:strategy", 
            "可以访问自动化分案菜单，且可以创建、编辑、删除分案策略，配置自动分案规则", 
            "可以访问自动化分案菜单，但无法看到或使用分案策略管理功能", 12, true));
        items.add(createItem((long) itemId++, 3L, "甲方案件队列管理", "case:queue:manage", 
            "可以访问甲方案件队列管理菜单，且可以创建、编辑、删除案件队列，配置队列规则", 
            "可以访问甲方案件队列管理菜单，但无法看到或使用队列管理功能", 13, true));
        
        // 4. 字段配置模块
        items.add(createItem((long) itemId++, 4L, "标准字段管理", "field:standard:manage", 
            "可以访问标准字段管理菜单，且可以创建、编辑、删除标准字段，配置字段类型和属性", 
            "可以访问标准字段管理菜单，查看标准字段列表，但无法创建、编辑或删除字段", 1, true));
        items.add(createItem((long) itemId++, 4L, "甲方字段查看", "field:tenant:view", 
            "可以访问甲方字段查看菜单，查看甲方自定义字段列表，且可以编辑或删除字段", 
            "可以访问甲方字段查看菜单，查看甲方自定义字段列表，但无法编辑或删除字段", 2, true));
        items.add(createItem((long) itemId++, 4L, "字段映射配置", "field:mapping:config", 
            "可以访问字段映射配置菜单，且可以配置标准字段与甲方字段之间的映射关系", 
            "可以访问字段映射配置菜单，查看字段映射关系，但无法编辑或删除映射", 3, true));
        items.add(createItem((long) itemId++, 4L, "字段分组管理", "field:group:manage", 
            "可以访问字段分组管理菜单，且可以创建、编辑、删除字段分组，组织字段结构", 
            "可以访问字段分组管理菜单，查看字段分组列表，但无法创建、编辑或删除分组", 4, true));
        items.add(createItem((long) itemId++, 4L, "甲方字段展示配置", "field:display:config", 
            "可以访问甲方字段展示配置菜单，且可以配置字段在案件列表、详情页面的显示方式和顺序", 
            "可以访问甲方字段展示配置菜单，查看字段展示配置，但无法编辑配置", 5, true));
        
        // 5. 人员与机构管理模块
        items.add(createItem((long) itemId++, 5L, "甲方管理", "tenant:manage", 
            "可以访问甲方管理菜单，且可以创建、编辑、删除甲方信息，配置甲方基本信息和权限", 
            "可以访问甲方管理菜单，查看甲方列表和详情，但无法创建、编辑或删除甲方", 1, true));
        items.add(createItem((long) itemId++, 5L, "甲方字段配置", "tenant:field:config", 
            "可以访问甲方字段配置菜单，且可以为指定甲方配置自定义字段，设置字段属性", 
            "可以访问甲方字段配置菜单，查看甲方字段配置，但无法编辑或删除配置", 2, true));
        items.add(createItem((long) itemId++, 5L, "机构管理", "agency:manage", 
            "可以访问机构管理菜单，且可以创建、编辑、删除催收机构信息，管理机构基本信息", 
            "可以访问机构管理菜单，查看机构列表和详情，但无法创建、编辑或删除机构", 3, true));
        items.add(createItem((long) itemId++, 5L, "机构作息时间管理", "agency:working:hours", 
            "可以访问机构作息时间管理菜单，且可以配置机构的上下班时间、休息日等作息规则", 
            "可以访问机构作息时间管理菜单，查看机构作息时间配置，但无法编辑配置", 4, true));
        items.add(createItem((long) itemId++, 5L, "小组群管理", "team:group:manage", 
            "可以访问小组群管理菜单，且可以创建、编辑、删除小组群，组织小组结构", 
            "可以访问小组群管理菜单，查看小组群列表，但无法创建、编辑或删除小组群", 5, true));
        items.add(createItem((long) itemId++, 5L, "小组管理", "team:manage", 
            "可以访问小组管理菜单，且可以创建、编辑、删除催收小组，配置小组信息和成员", 
            "可以访问小组管理菜单，查看小组列表和详情，但无法创建、编辑或删除小组", 6, true));
        items.add(createItem((long) itemId++, 5L, "小组管理员管理", "team:admin:manage", 
            "可以访问小组管理员管理菜单，且可以创建、编辑、删除小组管理员账号，分配管理权限", 
            "可以访问小组管理员管理菜单，查看小组管理员列表，但无法创建、编辑或删除管理员", 7, true));
        items.add(createItem((long) itemId++, 5L, "催员管理", "collector:manage", 
            "可以访问催员管理菜单，且可以创建、编辑、删除催员账号，配置催员信息和权限", 
            "可以访问催员管理菜单，查看催员列表和详情，但无法创建、编辑或删除催员", 8, true));
        
        // 6. 渠道配置模块
        items.add(createItem((long) itemId++, 6L, "渠道发送限制配置", "channel:limit:config", 
            "可以访问渠道发送限制配置菜单，且可以配置各渠道的发送频率、时间限制等规则", 
            "可以访问渠道发送限制配置菜单，查看渠道发送限制配置，但无法编辑配置", 1, true));
        items.add(createItem((long) itemId++, 6L, "甲方渠道管理", "channel:tenant:manage", 
            "可以访问甲方渠道管理菜单，且可以为甲方配置可用的催收渠道，启用或禁用渠道", 
            "可以访问甲方渠道管理菜单，查看甲方渠道配置，但无法编辑或删除配置", 2, true));
        
        // 7. 系统管理模块
        items.add(createItem((long) itemId++, 7L, "权限配置", "system:permission:config", 
            "可以访问权限配置菜单，且可以配置各角色的权限矩阵，设置权限级别（不可见/仅可见/可编辑）", 
            "可以访问权限配置菜单，查看权限配置矩阵，但无法编辑权限设置", 1, true));
        items.add(createItem((long) itemId++, 7L, "权限查看", "system:permission:view", 
            "可以访问权限查看菜单，查看当前系统的权限配置情况，且可以导出权限配置", 
            "可以访问权限查看菜单，查看当前系统的权限配置情况，但无法编辑权限设置", 2, true));
        items.add(createItem((long) itemId++, 7L, "通知配置", "system:notification:config", 
            "可以访问通知配置菜单，且可以配置系统通知模板、通知规则和通知渠道", 
            "可以访问通知配置菜单，查看通知配置信息，但无法编辑或删除配置", 3, true));
        
        return items;
    }

    @GetMapping("/configs")
    public ResponseData<List<Map<String, Object>>> getPermissionConfigs(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String roleCode
    ) {
        log.info("获取权限配置（Mock），tenantId={}, roleCode={}", tenantId, roleCode);
        
        List<Map<String, Object>> configs = new ArrayList<>();
        configs.add(createConfig(1L, tenantId, "SuperAdmin", 1L, "editable"));
        configs.add(createConfig(2L, tenantId, "SuperAdmin", 2L, "editable"));
        configs.add(createConfig(3L, tenantId, "TenantAdmin", 1L, "readonly"));
        
        return ResponseData.success(configs);
    }

    @PutMapping("/configs")
    public ResponseData<Map<String, Object>> updatePermissionConfigs(
            @RequestBody Map<String, Object> request
    ) {
        log.info("批量更新权限配置（Mock），request={}", request);
        
        try {
            Long tenantId = request.get("tenant_id") != null ? 
                Long.valueOf(request.get("tenant_id").toString()) : null;
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> updates = (List<Map<String, Object>>) request.get("updates");
            
            if (updates == null || updates.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "更新列表不能为空");
                return ResponseData.error(400, "更新列表不能为空");
            }
            
            int updatedCount = 0;
            for (Map<String, Object> update : updates) {
                String roleCode = (String) update.get("role_code");
                Long permissionItemId = Long.valueOf(update.get("permission_item_id").toString());
                String permissionLevel = (String) update.get("permission_level");
                
                // 保存到内存缓存
                String key = String.format("%s_%s_%d", 
                    tenantId != null ? tenantId.toString() : "system",
                    roleCode,
                    permissionItemId);
                permissionConfigCache.put(key, permissionLevel);
                updatedCount++;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", String.format("权限配置已更新，共更新 %d 项", updatedCount));
            result.put("updated", updatedCount);
            result.put("created", 0);
            
            log.info("权限配置已保存到内存缓存，共更新 {} 项", updatedCount);
            return ResponseData.success(result);
        } catch (Exception e) {
            log.error("批量更新权限配置失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
            return ResponseData.error(500, "更新失败: " + e.getMessage());
        }
    }

    @GetMapping("/configurable-roles")
    public ResponseData<Map<String, Object>> getConfigurableRoles(
            @RequestParam(name = "current_role") String currentRole
    ) {
        log.info("获取可配置角色列表（Mock），currentRole={}", currentRole);
        
        Map<String, Object> response = new HashMap<>();
        
        List<Map<String, Object>> roles = new ArrayList<>();
        roles.add(createRole("SuperAdmin", "超级管理员"));
        roles.add(createRole("TenantAdmin", "甲方管理员"));
        roles.add(createRole("AgencyAdmin", "机构管理员"));
        roles.add(createRole("TeamAdmin", "小组管理员"));
        roles.add(createRole("Collector", "催员"));
        
        response.put("configurable_roles", roles);
        response.put("current_role", currentRole);
        
        return ResponseData.success(response);
    }

    @GetMapping("/matrix")
    public ResponseData<Map<String, Object>> getPermissionMatrix(
            @RequestParam(required = false) Long tenantId
    ) {
        log.info("获取权限矩阵（Mock），tenantId={}", tenantId);
        
        Map<String, Object> matrix = new HashMap<>();
        
        // 模块列表 - 基于控台实际菜单结构
        List<Map<String, Object>> modules = new ArrayList<>();
        modules.add(createModule(1L, "工作台", "dashboard", "工作台相关功能", 1, true));
        modules.add(createModule(2L, "数据看板", "data_dashboard", "数据看板相关功能", 2, true));
        modules.add(createModule(3L, "案件管理", "case_management", "案件管理相关功能", 3, true));
        modules.add(createModule(4L, "字段配置", "field_config", "字段配置相关功能", 4, true));
        modules.add(createModule(5L, "人员与机构管理", "organization", "人员与机构管理相关功能", 5, true));
        modules.add(createModule(6L, "渠道配置", "channel_config", "渠道配置相关功能", 6, true));
        modules.add(createModule(7L, "系统管理", "system_management", "系统管理相关功能", 7, true));
        
        // 权限项列表 - 复用buildAllPermissionItems方法
        List<Map<String, Object>> items = buildAllPermissionItems();
        
        // 配置列表 - 为所有角色和权限项生成配置
        List<Map<String, Object>> configs = new ArrayList<>();
        List<String> roles = Arrays.asList("SuperAdmin", "TenantAdmin", "AgencyAdmin", "TeamAdmin", "Collector");
        
        // 为每个角色和权限项创建配置
        for (String role : roles) {
            for (Map<String, Object> item : items) {
                Long itemId = (Long) item.get("id");
                String permissionCode = (String) item.get("permission_code");
                
                // 先检查内存缓存中是否有用户修改的配置
                String cacheKey = String.format("%s_%s_%d", 
                    tenantId != null ? tenantId.toString() : "system",
                    role,
                    itemId);
                String permissionLevel = permissionConfigCache.get(cacheKey);
                
                // 如果缓存中没有，使用默认权限级别
                if (permissionLevel == null) {
                    permissionLevel = getPermissionLevel(role, permissionCode);
                }
                
                configs.add(createConfig(
                    (long) configs.size() + 1,
                    tenantId,
                    role,
                    itemId,
                    permissionLevel
                ));
            }
        }
        
        matrix.put("modules", modules);
        matrix.put("items", items);
        matrix.put("configs", configs);
        matrix.put("tenant_id", tenantId);
        
        return ResponseData.success(matrix);
    }

    @DeleteMapping("/configs/{configId}")
    public ResponseData<Map<String, Object>> deletePermissionConfig(
            @PathVariable Long configId
    ) {
        log.info("删除权限配置（Mock），configId={}", configId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "配置已删除（Mock）");
        
        return ResponseData.success(result);
    }

    // Helper methods
    private Map<String, Object> createModule(Long id, String moduleName, String moduleCode, 
                                             String description, Integer sortOrder, Boolean isActive) {
        Map<String, Object> module = new HashMap<>();
        module.put("id", id);
        module.put("module_name", moduleName);
        module.put("module_code", moduleCode);
        module.put("module_key", moduleCode);  // 前端期望的字段名
        module.put("description", description);
        module.put("sort_order", sortOrder);
        module.put("is_active", isActive);
        module.put("created_at", "2025-11-22T10:00:00");
        module.put("updated_at", "2025-11-22T10:00:00");
        return module;
    }

    private Map<String, Object> createItem(Long id, Long moduleId, String itemName, 
                                          String permissionCode, String editableDesc, String readonlyDesc,
                                          Integer sortOrder, Boolean isActive) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", id);
        item.put("module_id", moduleId);
        item.put("item_name", itemName);
        item.put("permission_code", permissionCode);
        item.put("item_key", permissionCode);  // 前端期望的字段名
        // 根据moduleId查找对应的module_code作为module_key
        String moduleKey = getModuleKeyByModuleId(moduleId);
        if (moduleKey != null) {
            item.put("module_key", moduleKey);  // 前端期望的字段名
        }
        // 组合描述：可编辑和仅可见两行说明
        String description = "✏️：" + editableDesc + "\n👁️：" + readonlyDesc;
        item.put("description", description);
        item.put("sort_order", sortOrder);
        item.put("is_active", isActive);
        item.put("created_at", "2025-11-22T10:00:00");
        item.put("updated_at", "2025-11-22T10:00:00");
        return item;
    }
    
    /**
     * 根据模块ID获取模块代码
     */
    private String getModuleKeyByModuleId(Long moduleId) {
        if (moduleId == 1L) return "dashboard";
        if (moduleId == 2L) return "data_dashboard";
        if (moduleId == 3L) return "case_management";
        if (moduleId == 4L) return "field_config";
        if (moduleId == 5L) return "organization";
        if (moduleId == 6L) return "channel_config";
        if (moduleId == 7L) return "system_management";
        return null;
    }

    private Map<String, Object> createConfig(Long id, Long tenantId, String roleCode, 
                                            Long permissionItemId, String permissionLevel) {
        Map<String, Object> config = new HashMap<>();
        config.put("id", id);
        config.put("tenant_id", tenantId);
        config.put("role_code", roleCode);
        config.put("permission_item_id", permissionItemId);
        config.put("permission_level", permissionLevel);  // 前端期望的字段名和格式
        config.put("has_permission", !"none".equals(permissionLevel));  // 保留兼容性
        config.put("created_at", "2025-11-22T10:00:00");
        config.put("updated_at", "2025-11-22T10:00:00");
        return config;
    }
    
    /**
     * 根据角色和权限代码确定权限级别
     * 基于控台实际业务逻辑设计
     */
    private String getPermissionLevel(String roleCode, String permissionCode) {
        // SuperAdmin 拥有所有权限（可编辑）
        if ("SuperAdmin".equals(roleCode)) {
            return "editable";
        }
        
        // TenantAdmin（甲方管理员）权限
        if ("TenantAdmin".equals(roleCode)) {
            // 工作台：可编辑（可以配置工作台布局、小部件等）
            if (permissionCode.equals("dashboard:view")) {
                return "editable";
            }
            // 案件管理：全部可编辑
            if (permissionCode.startsWith("case:")) {
                return "editable";
            }
            // 字段配置：全部可编辑
            if (permissionCode.startsWith("field:")) {
                return "editable";
            }
            // 甲方管理：全部可编辑
            if (permissionCode.startsWith("tenant:")) {
                return "editable";
            }
            // 机构管理：可编辑（可以管理自己甲方下的机构）
            if (permissionCode.startsWith("agency:")) {
                return "editable";
            }
            // 小组群管理：可编辑（可以管理自己甲方下的小组群）
            if (permissionCode.startsWith("team:group:")) {
                return "editable";
            }
            // 小组管理：可编辑（可以管理自己甲方下的小组）
            if (permissionCode.startsWith("team:manage")) {
                return "editable";
            }
            // 小组管理员管理：可编辑（可以管理自己甲方下的小组管理员）
            if (permissionCode.startsWith("team:admin:")) {
                return "editable";
            }
            // 催员管理：可编辑（可以管理自己甲方下的催员）
            if (permissionCode.startsWith("collector:")) {
                return "editable";
            }
            // 渠道配置：全部可编辑
            if (permissionCode.startsWith("channel:")) {
                return "editable";
            }
            // 系统管理：权限查看和通知配置可编辑
            if (permissionCode.startsWith("system:")) {
                if (permissionCode.contains("permission:view") || permissionCode.contains("notification:config")) {
                    return "editable";
                }
                return "readonly";
            }
            // 数据看板：只读
            if (permissionCode.startsWith("dashboard:")) {
                return "readonly";
            }
            // 其他：只读
            return "readonly";
        }
        
        // AgencyAdmin（机构管理员）权限
        if ("AgencyAdmin".equals(roleCode)) {
            // 工作台：可编辑（可以配置工作台布局、小部件等）
            if (permissionCode.equals("dashboard:view")) {
                return "editable";
            }
            // 案件管理：查看和分配可编辑，其他只读
            if (permissionCode.startsWith("case:")) {
                // 案件列表、案件详情、查看催记、添加催记：可编辑
                if (permissionCode.contains(":list:view") || permissionCode.contains(":detail:view") || 
                    permissionCode.contains(":notes:view") || permissionCode.contains(":notes:add") ||
                    permissionCode.contains(":assign")) {
                    return "editable";
                }
                // 其他案件管理权限：只读（添加、编辑、删除、筛选器配置、导出、自动化分案、分案策略管理、队列管理）
                return "readonly";
            }
            // 机构管理：自己的机构可编辑
            if (permissionCode.startsWith("agency:") || permissionCode.startsWith("team:") || 
                permissionCode.startsWith("collector:")) {
                return "editable";
            }
            // 数据看板：只读
            if (permissionCode.startsWith("dashboard:")) {
                return "readonly";
            }
            // 其他：不可见
            return "none";
        }
        
        // TeamAdmin（小组管理员）权限
        if ("TeamAdmin".equals(roleCode)) {
            // 工作台：可编辑（可以配置工作台布局、小部件等）
            if (permissionCode.equals("dashboard:view")) {
                return "editable";
            }
            // 案件管理：查看、分配、催记可编辑，其他只读
            if (permissionCode.startsWith("case:")) {
                // 案件列表、案件详情、查看催记、添加催记、分配案件：可编辑
                if (permissionCode.contains(":list:view") || permissionCode.contains(":detail:view") || 
                    permissionCode.contains(":notes:view") || permissionCode.contains(":notes:add") ||
                    permissionCode.contains(":assign")) {
                    return "editable";
                }
                // 其他案件管理权限：只读
                return "readonly";
            }
            // 小组和催员管理：可编辑
            if (permissionCode.startsWith("team:") || permissionCode.startsWith("collector:")) {
                return "editable";
            }
            // 数据看板：只读
            if (permissionCode.startsWith("dashboard:")) {
                return "readonly";
            }
            // 其他：不可见
            return "none";
        }
        
        // Collector（催员）权限
        if ("Collector".equals(roleCode)) {
            // 工作台：可编辑（可以配置工作台布局、小部件等）
            if (permissionCode.equals("dashboard:view")) {
                return "editable";
            }
            // 案件管理：查看和催记可编辑，其他只读
            if (permissionCode.startsWith("case:")) {
                // 案件列表、案件详情、查看催记、添加催记：可编辑
                if (permissionCode.contains(":list:view") || permissionCode.contains(":detail:view") || 
                    permissionCode.contains(":notes:view") || permissionCode.contains(":notes:add")) {
                    return "editable";
                }
                // 其他案件管理权限：只读
                return "readonly";
            }
            // 数据看板：只读
            if (permissionCode.startsWith("dashboard:")) {
                return "readonly";
            }
            // 其他：不可见
            return "none";
        }
        
        // 默认无权限
        return "none";
    }

    private Map<String, Object> createRole(String code, String name) {
        Map<String, Object> role = new HashMap<>();
        role.put("code", code);
        role.put("name", name);
        return role;
    }
}

