package com.cco.common.constant;

/**
 * 系统常量
 */
public class Constants {
    
    /**
     * API 版本前缀
     */
    public static final String API_V1_PREFIX = "/api/v1";
    
    /**
     * JWT Token 前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * JWT Token Header
     */
    public static final String TOKEN_HEADER = "Authorization";
    
    /**
     * 用户角色
     */
    public static class Role {
        public static final String SUPER_ADMIN = "SuperAdmin";
        public static final String TENANT_ADMIN = "TenantAdmin";
        public static final String AGENCY_ADMIN = "AgencyAdmin";
        public static final String TEAM_ADMIN = "TeamAdmin";
        public static final String COLLECTOR = "Collector";
    }
    
    /**
     * 默认密码
     */
    public static final String DEFAULT_PASSWORD = "123456";
    
    /**
     * 分页默认值
     */
    public static class Page {
        public static final int DEFAULT_PAGE = 1;
        public static final int DEFAULT_SIZE = 10;
        public static final int MAX_SIZE = 100;
    }
    
}

