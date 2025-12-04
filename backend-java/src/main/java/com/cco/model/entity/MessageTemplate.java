package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息模板配置实体类
 * 
 * @author CCO Team
 * @date 2025-12-03
 */
@Data
@TableName("message_templates")
public class MessageTemplate {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 甲方ID
     */
    private Long tenantId;
    
    /**
     * 模板名称
     */
    private String templateName;
    
    /**
     * 模板类型：organization-组织模板，personal-个人模板
     */
    private String templateType;
    
    /**
     * 适用机构ID列表（JSON格式）
     * NULL表示全部机构，[1,2,3]表示指定机构
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<Long> agencyIds;
    
    /**
     * 案件阶段：C/S0/S1-3/S3+
     */
    private String caseStage;
    
    /**
     * 场景：greeting/reminder/strong
     */
    private String scene;
    
    /**
     * 时间点：morning/afternoon/evening
     */
    private String timeSlot;
    
    /**
     * 模板内容，支持变量占位符
     */
    private String content;
    
    /**
     * 可用变量列表（JSON格式）
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> variables;
    
    /**
     * 是否启用：true-启用，false-禁用
     */
    private Boolean isEnabled;
    
    /**
     * 排序权重，越小越靠前
     */
    private Integer sortOrder;
    
    /**
     * 使用次数统计
     */
    private Integer usageCount;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    /**
     * 创建人ID
     */
    private Long createdBy;
    
    /**
     * 更新人ID
     */
    private Long updatedBy;
}

