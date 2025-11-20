package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 通知模板表 - 用于接收甲方核心系统推送的通知
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "notification_templates", autoResultMap = true)
public class NotificationTemplate extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 甲方ID（NULL表示全局模板）
     */
    private Long tenantId;

    /**
     * 模板ID（唯一标识）
     */
    private String templateId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板类型：case_tag_change/case_payment/user_app_visit/user_payment_page_visit等
     */
    private String templateType;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 通知正文模板，支持变量如：{case_id}、{amount}、{tag_name}等
     */
    private String contentTemplate;

    /**
     * 点击后跳转的URL模板，支持变量
     */
    private String jumpUrlTemplate;

    /**
     * 发送对象类型：agency/team/collector
     */
    private String targetType;

    /**
     * 目标机构ID列表（JSON数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> targetAgencies;

    /**
     * 目标小组ID列表（JSON数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> targetTeams;

    /**
     * 目标催员ID列表（JSON数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> targetCollectors;

    /**
     * 是否强制阅读
     */
    private Boolean isForcedRead;

    /**
     * 非强制阅读时的重复提醒间隔（分钟）
     */
    private Integer repeatIntervalMinutes;

    /**
     * 非强制阅读时的最大提醒次数
     */
    private Integer maxRemindCount;

    /**
     * 通知时间范围开始（HH:MM）
     */
    private String notifyTimeStart;

    /**
     * 通知时间范围结束（HH:MM）
     */
    private String notifyTimeEnd;

    /**
     * 优先级：high/medium/low
     */
    private String priority;

    /**
     * 展示时长（秒）
     */
    private Integer displayDurationSeconds;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 可用变量列表及说明（JSON）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> availableVariables;

    /**
     * 累计发送次数
     */
    private Integer totalSent;

    /**
     * 累计阅读次数
     */
    private Integer totalRead;

    /**
     * 创建人ID
     */
    private Long createdBy;

}

