package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 案件联系人实体类
 * 对应数据库表: case_contacts
 * 对应Python模型: app/models/case_contact.py
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("case_contacts")
public class CaseContact extends BaseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 案件ID
     */
    private Long caseId;
    
    /**
     * 联系人姓名
     */
    private String contactName;
    
    /**
     * 联系电话
     */
    private String phoneNumber;
    
    /**
     * 关系（本人/配偶/朋友/同事/家人等）
     */
    private String relation;
    
    /**
     * 是否本人
     */
    private Boolean isPrimary;
    
    /**
     * 可用通信渠道（JSON格式）
     */
    private String availableChannels;
    
    /**
     * 备注
     */
    private String remark;
}












































