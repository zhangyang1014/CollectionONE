package com.cco.service;

import com.cco.model.dto.*;

import java.util.List;
import java.util.Map;

/**
 * 甲方字段JSON管理服务接口
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
public interface TenantFieldsJsonService {
    
    /**
     * 校验JSON文件格式
     * 
     * @param tenantId 甲方ID
     * @param fieldsJson JSON文件内容
     * @return 校验结果
     */
    TenantFieldsJsonValidateResponse validateJson(Long tenantId, Map<String, Object> fieldsJson);
    
    /**
     * 对比版本差异
     * 
     * @param tenantId 甲方ID
     * @param newFieldsJson 新版本的JSON数据
     * @return 对比结果
     */
    TenantFieldsJsonCompareResponse compareVersions(Long tenantId, Map<String, Object> newFieldsJson);
    
    /**
     * 上传并保存JSON文件
     * 
     * @param tenantId 甲方ID
     * @param request 上传请求
     * @param uploadedBy 上传人
     * @return 保存结果
     */
    Map<String, Object> uploadJson(Long tenantId, TenantFieldsJsonUploadRequest request, String uploadedBy);
    
    /**
     * 获取历史版本列表
     * 
     * @param tenantId 甲方ID
     * @return 历史版本列表
     */
    List<Map<String, Object>> getHistoryVersions(Long tenantId);
    
    /**
     * 获取当前版本的JSON数据
     * 
     * @param tenantId 甲方ID
     * @return 当前版本的JSON数据
     */
    Map<String, Object> getCurrentVersion(Long tenantId);
}

