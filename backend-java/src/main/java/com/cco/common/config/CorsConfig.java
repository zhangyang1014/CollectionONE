package com.cco.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * 跨域配置
 * 确保前端可以正常访问后端API
 */
@Configuration
public class CorsConfig {
    
    // 直接使用硬编码值，避免配置依赖
    private final List<String> allowedOrigins = Arrays.asList(
        "http://localhost:5173",
        "http://localhost:3000",
        "http://localhost:8080"
    );
    
    private final List<String> allowedMethods = Arrays.asList(
        "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
    );
    
    private final List<String> allowedHeaders = List.of("*");
    
    private final List<String> exposedHeaders = List.of("*");
    
    @Value("${app.cors.allow-credentials}")
    private Boolean allowCredentials;
    
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的域名
        config.setAllowedOrigins(allowedOrigins);
        
        // 允许的请求方法
        config.setAllowedMethods(allowedMethods);
        
        // 允许的请求头
        config.setAllowedHeaders(allowedHeaders);
        
        // 暴露的响应头
        config.setExposedHeaders(exposedHeaders);
        
        // 允许携带凭证
        config.setAllowCredentials(allowCredentials);
        
        // 预检请求有效期（秒）
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
    
}

