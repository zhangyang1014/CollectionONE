package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 公共通知表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("public_notifications")
public class PublicNotification extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 甲方ID（NULL表示全局通知）
     */
    private Long tenantId;

    /**
     * 机构ID（NULL表示甲方级别通知）
     */
    private Long agencyId;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知正文内容
     */
    private String content;

    /**
     * H5链接地址（可选）
     */
    private String h5Content;

    /**
     * 轮播间隔（秒）
     */
    private Integer carouselIntervalSeconds;

    /**
     * 是否强制阅读
     */
    private Boolean isForcedRead;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 重复提醒时间间隔（分钟）
     */
    private Integer repeatIntervalMinutes;

    /**
     * 最大提醒次数
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
     * 生效开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveStartTime;

    /**
     * 生效结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveEndTime;

    /**
     * 通知对象角色列表（JSON字符串）
     */
    private String notifyRoles;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 创建人ID
     */
    private Long createdBy;

}

