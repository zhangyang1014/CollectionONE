package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 通知配置表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "notification_configs", autoResultMap = true)
public class NotificationConfig extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 甲方ID（NULL表示全局配置）
     */
    private Long tenantId;

    /**
     * 通知类型：unreplied/nudge/case_update/performance/timeout
     */
    private String notificationType;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 配置数据（JSON格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> configData;

}

