package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

/**
 * 机构作息时间表
 * 根据PRD要求，机构作息时间用于质检、通知等环节的时间控制
 * PRD要求：day_of_week 1-7（1=周一），start_time和end_time为Time类型
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("agency_working_hours")
public class AgencyWorkingHours extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 机构ID
     */
    private Long agencyId;

    /**
     * 星期几（1-7，1=周一，7=周日）
     */
    private Integer dayOfWeek;

    /**
     * 开始时间（HH:MM格式，NULL表示不工作）
     */
    private LocalTime startTime;

    /**
     * 结束时间（HH:MM格式，NULL表示不工作）
     */
    private LocalTime endTime;

    /**
     * 是否启用
     */
    private Boolean isActive;
}

