package com.cco.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cco.common.constant.PermissionLevel;
import com.cco.common.constant.RoleCode;
import com.cco.common.exception.BusinessException;
import com.cco.mapper.PermissionItemMapper;
import com.cco.mapper.PermissionModuleMapper;
import com.cco.mapper.RolePermissionConfigMapper;
import com.cco.model.dto.permission.*;
import com.cco.model.entity.PermissionItem;
import com.cco.model.entity.PermissionModule;
import com.cco.model.entity.RolePermissionConfig;
import com.cco.service.IPermissionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限管理Service实现类
 * 对应Python: app/api/permissions.py
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<RolePermissionConfigMapper, RolePermissionConfig>
        implements IPermissionService {

    @Autowired
    private PermissionModuleMapper permissionModuleMapper;

    @Autowired
    private PermissionItemMapper permissionItemMapper;

    @Autowired
    private RolePermissionConfigMapper rolePermissionConfigMapper;

    @Override
    public List<PermissionModuleDTO> getPermissionModules(Boolean isActive) {
        LambdaQueryWrapper<PermissionModule> wrapper = new LambdaQueryWrapper<>();
        if (isActive != null) {
            wrapper.eq(PermissionModule::getIsActive, isActive);
        }
        wrapper.orderByAsc(PermissionModule::getSortOrder);

        List<PermissionModule> modules = permissionModuleMapper.selectList(wrapper);
        return modules.stream().map(module -> {
            PermissionModuleDTO dto = new PermissionModuleDTO();
            BeanUtils.copyProperties(module, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<PermissionItemDTO> getPermissionItems(Long moduleId, Boolean isActive) {
        LambdaQueryWrapper<PermissionItem> wrapper = new LambdaQueryWrapper<>();
        if (moduleId != null) {
            wrapper.eq(PermissionItem::getModuleId, moduleId);
        }
        if (isActive != null) {
            wrapper.eq(PermissionItem::getIsActive, isActive);
        }
        wrapper.orderByAsc(PermissionItem::getSortOrder);

        List<PermissionItem> items = permissionItemMapper.selectList(wrapper);

        // 获取所有模块（用于填充moduleKey）
        Map<Long, PermissionModule> moduleMap = permissionModuleMapper.selectList(null)
                .stream()
                .collect(Collectors.toMap(PermissionModule::getId, m -> m));

        return items.stream().map(item -> {
            PermissionItemDTO dto = new PermissionItemDTO();
            BeanUtils.copyProperties(item, dto);
            // 添加moduleKey
            PermissionModule module = moduleMap.get(item.getModuleId());
            if (module != null) {
                dto.setModuleKey(module.getModuleKey());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<RolePermissionConfigDTO> getPermissionConfigs(Long tenantId, String roleCode) {
        LambdaQueryWrapper<RolePermissionConfig> wrapper = new LambdaQueryWrapper<>();

        // tenant_id 筛选（支持查询 NULL）
        if (tenantId != null) {
            wrapper.eq(RolePermissionConfig::getTenantId, tenantId);
        } else {
            // 不传 tenant_id 时，只返回系统默认配置
            wrapper.isNull(RolePermissionConfig::getTenantId);
        }

        // role_code 筛选
        if (roleCode != null && !roleCode.isEmpty()) {
            wrapper.eq(RolePermissionConfig::getRoleCode, roleCode);
        }

        List<RolePermissionConfig> configs = rolePermissionConfigMapper.selectList(wrapper);
        return configs.stream().map(config -> {
            RolePermissionConfigDTO dto = new RolePermissionConfigDTO();
            BeanUtils.copyProperties(config, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchUpdatePermissionConfigs(BatchUpdatePermissionRequest request) {
        Long tenantId = request.getTenantId();
        List<PermissionConfigUpdateDTO> updates = request.getUpdates();

        if (updates == null || updates.isEmpty()) {
            throw new BusinessException("更新列表不能为空");
        }

        int updatedCount = 0;
        int createdCount = 0;
        List<String> errors = new ArrayList<>();

        for (PermissionConfigUpdateDTO update : updates) {
            try {
                // 验证权限级别
                if (!PermissionLevel.isValid(update.getPermissionLevel())) {
                    errors.add("无效的权限级别: " + update.getPermissionLevel());
                    continue;
                }

                // 查询是否已存在配置
                LambdaQueryWrapper<RolePermissionConfig> wrapper = new LambdaQueryWrapper<>();
                if (tenantId != null) {
                    wrapper.eq(RolePermissionConfig::getTenantId, tenantId);
                } else {
                    wrapper.isNull(RolePermissionConfig::getTenantId);
                }
                wrapper.eq(RolePermissionConfig::getRoleCode, update.getRoleCode())
                        .eq(RolePermissionConfig::getPermissionItemId, update.getPermissionItemId());

                RolePermissionConfig existingConfig = rolePermissionConfigMapper.selectOne(wrapper);

                if (existingConfig != null) {
                    // 更新现有配置
                    existingConfig.setPermissionLevel(update.getPermissionLevel());
                    rolePermissionConfigMapper.updateById(existingConfig);
                    updatedCount++;
                } else {
                    // 创建新配置
                    RolePermissionConfig newConfig = new RolePermissionConfig();
                    newConfig.setTenantId(tenantId);
                    newConfig.setRoleCode(update.getRoleCode());
                    newConfig.setPermissionItemId(update.getPermissionItemId());
                    newConfig.setPermissionLevel(update.getPermissionLevel());
                    rolePermissionConfigMapper.insert(newConfig);
                    createdCount++;
                }
            } catch (Exception e) {
                errors.add("处理权限项 " + update.getPermissionItemId() + " 失败: " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", String.format("成功更新 %d 条，创建 %d 条配置", updatedCount, createdCount));
        result.put("updated", updatedCount);
        result.put("created", createdCount);
        if (!errors.isEmpty()) {
            result.put("errors", errors);
        }

        return result;
    }

    @Override
    public ConfigurableRolesResponse getConfigurableRoles(String currentRole) {
        List<String> configurableRoleCodes = RoleCode.getConfigurableRoles(currentRole);
        Map<String, String> roleNames = RoleCode.getRoleNames();

        List<Map<String, String>> configurableRoles = configurableRoleCodes.stream()
                .map(roleCode -> {
                    Map<String, String> roleInfo = new HashMap<>();
                    roleInfo.put("code", roleCode);
                    roleInfo.put("name", roleNames.getOrDefault(roleCode, roleCode));
                    return roleInfo;
                })
                .collect(Collectors.toList());

        return new ConfigurableRolesResponse(currentRole, configurableRoles);
    }

    @Override
    public PermissionMatrixResponse getPermissionMatrix(Long tenantId) {
        // 获取所有启用的模块
        List<PermissionModuleDTO> modules = getPermissionModules(true);

        // 获取所有启用的权限项
        List<PermissionItemDTO> items = getPermissionItems(null, true);

        // 获取权限配置
        LambdaQueryWrapper<RolePermissionConfig> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            // 优先使用甲方配置，如果不存在则使用系统默认配置
            // 注意：这里需要先获取所有相关配置，然后在内存中去重
            wrapper.and(w -> w.eq(RolePermissionConfig::getTenantId, tenantId)
                    .or()
                    .isNull(RolePermissionConfig::getTenantId));
        } else {
            wrapper.isNull(RolePermissionConfig::getTenantId);
        }

        List<RolePermissionConfig> configs = rolePermissionConfigMapper.selectList(wrapper);

        // 构建配置映射 (roleCode, permissionItemId) -> config
        // 如果已存在（即甲方配置），优先使用甲方配置
        Map<String, RolePermissionConfig> configMap = new HashMap<>();
        for (RolePermissionConfig config : configs) {
            String key = config.getRoleCode() + "_" + config.getPermissionItemId();
            RolePermissionConfig existing = configMap.get(key);
            // 如果不存在，或者当前是甲方配置，则更新
            if (existing == null || (config.getTenantId() != null && config.getTenantId().equals(tenantId))) {
                configMap.put(key, config);
            }
        }

        // 转换为DTO
        List<RolePermissionConfigDTO> configDTOs = configMap.values().stream()
                .map(config -> {
                    RolePermissionConfigDTO dto = new RolePermissionConfigDTO();
                    BeanUtils.copyProperties(config, dto);
                    return dto;
                })
                .collect(Collectors.toList());

        return new PermissionMatrixResponse(modules, items, configDTOs, tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermissionConfig(Long configId) {
        RolePermissionConfig config = rolePermissionConfigMapper.selectById(configId);

        if (config == null) {
            throw new BusinessException("配置不存在");
        }

        // 不允许删除系统默认配置
        if (config.getTenantId() == null) {
            throw new BusinessException("不能删除系统默认配置");
        }

        rolePermissionConfigMapper.deleteById(configId);
    }

}

