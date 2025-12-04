package com.cco.common.config;

import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 配置
 * 注意：分页插件暂时注释，避免编译错误
 * 使用MapperConfig手动注册的Mapper，不在此处配置自动扫描
 */
@Configuration
public class MyBatisConfig {
    
    // 暂时注释分页插件配置，确保服务能启动
    // 如果需要分页功能，可以后续启用
    /*
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(1000L);
        paginationInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }
    */
    
}

