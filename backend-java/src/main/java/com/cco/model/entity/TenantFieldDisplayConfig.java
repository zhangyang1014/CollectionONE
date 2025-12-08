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
 * 甲方字段展示配置表 - 用于配置不同场景下字段的展示方式
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tenant_field_display_configs", autoResultMap = true)
public class TenantFieldDisplayConfig extends BaseEntity {

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
     * 场景类型：admin_case_list/collector_case_list/collector_case_detail
     */
    private String sceneType;

    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * 字段标识
     */
    private String fieldKey;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段数据类型：String/Integer/Boolean/Enum等
     */
    private String fieldDataType;

    /**
     * 字段来源：standard/custom
     * standard: 标准字段（已映射到标准字段）
     * custom: 自定义字段（不映射到标准字段，存储在custom_fields表）
     */
    private String fieldSource;
    
    /**
     * 枚举选项（仅Enum类型字段有值）
     * 非数据库字段，仅用于前端展示
     */
    @TableField(exist = false)
    private Object enumOptions;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 显示宽度（像素），0表示自动
     */
    private Integer displayWidth;

    /**
     * 颜色类型：normal/red/yellow/green
     */
    private String colorType;

    /**
     * 颜色规则（条件表达式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> colorRule;

    /**
     * 隐藏规则：基于案件所属队列配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> hideRule;

    /**
     * 对哪些队列隐藏（队列ID数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> hideForQueues;

    /**
     * 对哪些机构隐藏（机构ID数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> hideForAgencies;

    /**
     * 对哪些小组隐藏（小组ID数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> hideForTeams;

    /**
     * 格式化规则
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> formatRule;

    /**
     * 是否可搜索（针对文本字段）
     */
    private Boolean isSearchable;

    /**
     * 是否可筛选（针对枚举字段）
     */
    private Boolean isFilterable;

    /**
     * 是否支持范围检索（针对数字和时间字段）
     */
    private Boolean isRangeSearchable;

    /**
     * 是否必须展示（不可配置隐藏）
     * 控台案件列表的必须展示字段：case_code、user_name、loan_amount、outstanding_amount、overdue_days、case_status、due_date
     */
    @TableField(exist = false)
    private Boolean isRequired;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;

}

