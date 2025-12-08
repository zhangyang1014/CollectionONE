package com.cco.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cco.model.dto.TenantFieldUploadDTO;
import com.cco.model.dto.VersionCompareResponse;
import com.cco.model.entity.TenantFieldUpload;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 甲方字段上传记录Service接口
 *
 * @author CCO Team
 * @since 2025-12-08
 */
public interface TenantFieldUploadService extends IService<TenantFieldUpload> {

    /**
     * 上传JSON文件
     *
     * @param tenantId     甲方ID
     * @param scene        场景
     * @param file         JSON文件
     * @param uploadedBy   上传人ID
     * @param versionNote  版本说明
     * @return 上传结果
     */
    Map<String, Object> uploadJsonFile(String tenantId, String scene, MultipartFile file, 
                                      String uploadedBy, String versionNote);

    /**
     * 获取上传历史
     *
     * @param tenantId   甲方ID
     * @param scene      场景
     * @param page       页码
     * @param pageSize   每页数量
     * @return 上传历史列表
     */
    Map<String, Object> getUploadHistory(String tenantId, String scene, Integer page, Integer pageSize);

    /**
     * 获取特定版本详情
     *
     * @param tenantId  甲方ID
     * @param scene     场景
     * @param version   版本号
     * @return 版本详情
     */
    TenantFieldUploadDTO getVersionDetail(String tenantId, String scene, Integer version);

    /**
     * 版本对比
     *
     * @param tenantId  甲方ID
     * @param scene     场景
     * @param version1  基准版本号
     * @param version2  对比版本号
     * @return 对比结果
     */
    VersionCompareResponse compareVersions(String tenantId, String scene, Integer version1, Integer version2);

    /**
     * 设置当前版本
     *
     * @param tenantId    甲方ID
     * @param scene       场景
     * @param version     版本号
     * @param operatorId  操作人ID
     * @param reason      原因
     * @return 设置结果
     */
    Map<String, Object> activateVersion(String tenantId, String scene, Integer version, 
                                       String operatorId, String reason);

    /**
     * 获取当前生效版本的字段JSON
     *
     * @param tenantId  甲方ID
     * @param scene     场景
     * @return 字段JSON
     */
    Map<String, Object> getCurrentVersionFields(String tenantId, String scene);

    /**
     * 获取JSON模板
     *
     * @param scene  场景
     * @return JSON模板
     */
    Map<String, Object> getJsonTemplate(String scene);

    /**
     * 验证JSON文件
     *
     * @param file  JSON文件
     * @return 验证结果
     */
    Map<String, Object> validateJsonFile(MultipartFile file);
}
