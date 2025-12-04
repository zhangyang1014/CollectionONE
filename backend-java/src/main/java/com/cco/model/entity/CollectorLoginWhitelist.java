package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 催员登录白名单IP配置实体
 * 
 * @author CCO Team
 * @since 2025-01-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("collector_login_whitelist")
public class CollectorLoginWhitelist extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属甲方ID
     */
    private Long tenantId;

    /**
     * 是否启用白名单IP登录管理（false-否，true-是）
     * 注意：这个字段在表中存储，但实际控制逻辑是：如果该甲方有任何一条记录的isEnabled=true，则启用白名单
     */
    private Boolean isEnabled;

    /**
     * 白名单IP地址（支持IPv4和IPv6，支持CIDR格式，如：192.168.1.0/24）
     */
    private String ipAddress;

    /**
     * IP地址描述/备注
     */
    private String description;

    /**
     * 是否启用（false-否，true-是）
     */
    private Boolean isActive;

}



