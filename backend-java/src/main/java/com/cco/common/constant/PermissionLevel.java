package com.cco.common.constant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限级别常量
 * 对应Python: app/models/permission.py - PermissionLevel
 */
public class PermissionLevel {

    public static final String NONE = "none";           // 不可见
    public static final String READONLY = "readonly";   // 仅可见
    public static final String EDITABLE = "editable";   // 可编辑

    /**
     * 获取所有权限级别
     */
    public static List<String> getAllLevels() {
        return Arrays.asList(NONE, READONLY, EDITABLE);
    }

    /**
     * 获取权限级别的显示名称
     */
    public static Map<String, String> getDisplayNames() {
        Map<String, String> displayNames = new HashMap<>();
        displayNames.put(NONE, "不可见");
        displayNames.put(READONLY, "仅可见");
        displayNames.put(EDITABLE, "可编辑");
        return displayNames;
    }

    /**
     * 获取权限级别的显示名称
     *
     * @param level 权限级别
     * @return 显示名称
     */
    public static String getDisplayName(String level) {
        return getDisplayNames().getOrDefault(level, level);
    }

    /**
     * 验证权限级别是否有效
     *
     * @param level 权限级别
     * @return 是否有效
     */
    public static boolean isValid(String level) {
        return getAllLevels().contains(level);
    }

}

