package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通信记录实体类
 * 对应数据库表: communication_records
 * 对应Python模型: app/models/communication_record.py
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("communication_records")
public class CommunicationRecord extends BaseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 案件ID
     */
    private Long caseId;
    
    /**
     * 催员ID
     */
    private Long collectorId;
    
    /**
     * 联系人ID（本人或其他联系人）
     */
    private Long contactPersonId;
    
    /**
     * 通信渠道: phone, whatsapp, sms, rcs
     */
    private String channel;
    
    /**
     * 通信方向: inbound, outbound
     */
    private String direction;
    
    /**
     * 供应商ID（标识使用的外呼供应商）
     */
    private Long supplierId;
    
    /**
     * Infinity返回的分机号
     */
    private String infinityExtensionNumber;
    
    /**
     * Infinity返回的通话唯一标识
     */
    private String callUuid;
    
    /**
     * 自定义参数（JSON格式）
     */
    private String customParams;
    
    /**
     * 通话时长（秒）- 电话专属
     */
    private Integer callDuration;
    
    /**
     * 是否接通 - 电话专属
     */
    private Boolean isConnected;
    
    /**
     * 录音链接 - 电话专属
     */
    private String callRecordUrl;
    
    /**
     * 是否回复 - 消息专属（WhatsApp/SMS/RCS）
     */
    private Boolean isReplied;
    
    /**
     * 消息内容 - 消息专属
     */
    private String messageContent;
    
    /**
     * 联系结果: contacted(可联), connected(已接通), not_connected(未接通), 
     *           refused(拒绝), invalid_number(无效号码) 等
     */
    private String contactResult;
    
    /**
     * 首次触达时长（秒，从案件分配到首次有效触达）
     */
    private Integer ttfcSeconds;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 触达时间
     */
    private LocalDateTime contactedAt;
}

