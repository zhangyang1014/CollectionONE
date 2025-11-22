package com.cco.common.constant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色代码常量
 * 对应Python: app/models/permission.py - RoleCode
 */
public class RoleCode {

    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String TENANT_ADMIN = "TENANT_ADMIN";
    public static final String AGENCY_ADMIN = "AGENCY_ADMIN";
    public static final String TEAM_LEADER = "TEAM_LEADER";
    public static final String QUALITY_INSPECTOR = "QUALITY_INSPECTOR";
    public static final String DATA_SOURCE = "DATA_SOURCE";
    public static final String COLLECTOR = "COLLECTOR";

    /**
     * 获取所有角色
     */
    public static List<String> getAllRoles() {
        return Arrays.asList(
                SUPER_ADMIN,
                TENANT_ADMIN,
                AGENCY_ADMIN,
                TEAM_LEADER,
                QUALITY_INSPECTOR,
                DATA_SOURCE,
                COLLECTOR
        );
    }

    /**
     * 获取角色名称映射
     */
    public static Map<String, String> getRoleNames() {
        Map<String, String> roleNames = new HashMap<>();
        roleNames.put(SUPER_ADMIN, "超级管理员");
        roleNames.put(TENANT_ADMIN, "甲方管理员");
        roleNames.put(AGENCY_ADMIN, "机构管理员");
        roleNames.put(TEAM_LEADER, "小组长");
        roleNames.put(QUALITY_INSPECTOR, "质检员");
        roleNames.put(DATA_SOURCE, "数据源");
        roleNames.put(COLLECTOR, "催员");
        return roleNames;
    }

    /**
     * 获取当前角色可以配置的角色列表
     *
     * @param currentRole 当前用户角色
     * @return 可配置的角色列表
     */
    public static List<String> getConfigurableRoles(String currentRole) {
        Map<String, List<String>> roleHierarchy = new HashMap<>();
        
        // SUPER_ADMIN可以配置所有角色
        roleHierarchy.put(SUPER_ADMIN, getAllRoles());
        
        // TENANT_ADMIN可以配置除了SUPER_ADMIN之外的所有角色
        roleHierarchy.put(TENANT_ADMIN, Arrays.asList(
                TENANT_ADMIN,
                AGENCY_ADMIN,
                TEAM_LEADER,
                QUALITY_INSPECTOR,
                DATA_SOURCE,
                COLLECTOR
        ));
        
        // AGENCY_ADMIN可以配置小组长、质检员、数据源、催员
        roleHierarchy.put(AGENCY_ADMIN, Arrays.asList(
                TEAM_LEADER,
                QUALITY_INSPECTOR,
                DATA_SOURCE,
                COLLECTOR
        ));
        
        // TEAM_LEADER可以配置质检员、数据源、催员
        roleHierarchy.put(TEAM_LEADER, Arrays.asList(
                QUALITY_INSPECTOR,
                DATA_SOURCE,
                COLLECTOR
        ));
        
        return roleHierarchy.getOrDefault(currentRole, Arrays.asList());
    }

}

