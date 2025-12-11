package com.cco.model.dto;

import lombok.Data;

/**
 * 版本对比请求DTO
 *
 * @author CCO Team
 * @since 2025-12-08
 */
@Data
public class VersionCompareRequest {

    /**
     * 甲方ID
     */
    private String tenantId;

    /**
     * 场景
     */
    private String scene;

    /**
     * 基准版本号
     */
    private Integer version1;

    /**
     * 对比版本号
     */
    private Integer version2;
}

