package com.cco.common.config;

import com.cco.mapper.CaseMapper;
import com.cco.mapper.CaseQueueMapper;
import com.cco.mapper.CollectorMapper;
import com.cco.mapper.CaseAssignmentMapper;
import com.cco.mapper.TenantFieldsJsonMapper;
import com.cco.mapper.FieldGroupMapper;
import com.cco.mapper.TenantMapper;
import com.cco.mapper.TenantAdminMapper;
import com.cco.mapper.TeamAdminAccountMapper;
import com.cco.mapper.AgencyWorkingHoursMapper;
import com.cco.mapper.CaseReassignConfigMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mapper手动配置
 * 用于解决MyBatis Plus自动扫描的兼容性问题
 */
@Configuration
public class MapperConfig {
    
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    
    /**
     * 手动注册CaseQueueMapper
     */
    @Bean
    public MapperFactoryBean<CaseQueueMapper> caseQueueMapper() {
        MapperFactoryBean<CaseQueueMapper> factoryBean = new MapperFactoryBean<>();
        factoryBean.setMapperInterface(CaseQueueMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    
    /**
     * 手动注册CaseMapper
     */
    @Bean
    public MapperFactoryBean<CaseMapper> caseMapper() {
        MapperFactoryBean<CaseMapper> factoryBean = new MapperFactoryBean<>();
        factoryBean.setMapperInterface(CaseMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    
    /**
     * 手动注册CollectorMapper
     */
    @Bean
    public MapperFactoryBean<CollectorMapper> collectorMapper() {
        MapperFactoryBean<CollectorMapper> factoryBean = new MapperFactoryBean<>();
        factoryBean.setMapperInterface(CollectorMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    
    /**
     * 手动注册CaseAssignmentMapper
     */
    @Bean
    public MapperFactoryBean<CaseAssignmentMapper> caseAssignmentMapper() {
        MapperFactoryBean<CaseAssignmentMapper> factoryBean = new MapperFactoryBean<>();
        factoryBean.setMapperInterface(CaseAssignmentMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    
    /**
     * 手动注册TenantFieldsJsonMapper
     */
    @Bean
    public MapperFactoryBean<TenantFieldsJsonMapper> tenantFieldsJsonMapper() {
        MapperFactoryBean<TenantFieldsJsonMapper> factoryBean = new MapperFactoryBean<>();
        factoryBean.setMapperInterface(TenantFieldsJsonMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    
    /**
     * 手动注册FieldGroupMapper
     */
    @Bean
    public MapperFactoryBean<FieldGroupMapper> fieldGroupMapper() {
        MapperFactoryBean<FieldGroupMapper> factoryBean = new MapperFactoryBean<>();
        factoryBean.setMapperInterface(FieldGroupMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    
    /**
     * 手动注册TenantMapper
     */
    @Bean
    public MapperFactoryBean<TenantMapper> tenantMapper() {
        MapperFactoryBean<TenantMapper> factoryBean = new MapperFactoryBean<>();
        factoryBean.setMapperInterface(TenantMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    
    /**
     * 手动注册TenantAdminMapper
     */
    @Bean
    public MapperFactoryBean<TenantAdminMapper> tenantAdminMapper() {
        MapperFactoryBean<TenantAdminMapper> factoryBean = new MapperFactoryBean<>();
        factoryBean.setMapperInterface(TenantAdminMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    
    /**
     * 手动注册TeamAdminAccountMapper
     */
    @Bean
    public MapperFactoryBean<TeamAdminAccountMapper> teamAdminAccountMapper() {
        MapperFactoryBean<TeamAdminAccountMapper> factoryBean = new MapperFactoryBean<>();
        factoryBean.setMapperInterface(TeamAdminAccountMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    
    /**
     * 手动注册AgencyWorkingHoursMapper
     */
    @Bean
    public MapperFactoryBean<AgencyWorkingHoursMapper> agencyWorkingHoursMapper() {
        MapperFactoryBean<AgencyWorkingHoursMapper> factoryBean = new MapperFactoryBean<>();
        factoryBean.setMapperInterface(AgencyWorkingHoursMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
    
    /**
     * 手动注册CaseReassignConfigMapper
     */
    @Bean
    public MapperFactoryBean<CaseReassignConfigMapper> caseReassignConfigMapper() {
        MapperFactoryBean<CaseReassignConfigMapper> factoryBean = new MapperFactoryBean<>();
        factoryBean.setMapperInterface(CaseReassignConfigMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
}




