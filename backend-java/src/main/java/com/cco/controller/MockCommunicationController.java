package com.cco.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.mapper.CaseContactMapper;
import com.cco.mapper.CommunicationRecordMapper;
import com.cco.model.entity.CaseContact;
import com.cco.model.entity.CommunicationRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 通信记录控制器
 * 提供通信记录相关API
 */
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/communications")
public class MockCommunicationController {
    
    @Autowired(required = false)
    private CommunicationRecordMapper communicationRecordMapper;
    
    @Autowired(required = false)
    private CaseContactMapper caseContactMapper;

    /**
     * 获取案件的所有通信记录
     * GET /api/v1/communications/case/{caseId}
     * 
     * 注意：这个API应该返回所有通信记录，包括：
     * 1. communication_records 表中的记录（电话、WhatsApp等）
     * 2. collection_records 表中的记录（催记，如果有的话）
     * 
     * @param caseId 案件ID
     * @param channel 通信渠道筛选（可选）
     * @return 通信记录列表
     */
    @GetMapping("/case/{caseId}")
    public ResponseData<List<Map<String, Object>>> getCaseCommunications(
            @PathVariable Long caseId,
            @RequestParam(value = "channel", required = false) String channel
    ) {
        System.out.println("===============================================");
        System.out.println("[通信记录API] 接收参数:");
        System.out.println("  caseId = " + caseId);
        System.out.println("  channel = " + channel);
        System.out.println("===============================================");

        List<Map<String, Object>> records = new ArrayList<>();
        
        // 优先从数据库查询真实数据
        if (communicationRecordMapper != null) {
            try {
                // 先检查数据库中是否有任何通信记录（用于调试）
                long totalCount = communicationRecordMapper.selectCount(null);
                System.out.println("[通信记录API] 数据库中总通信记录数: " + totalCount);
                
                // 检查该案件ID是否存在
                QueryWrapper<CommunicationRecord> countWrapper = new QueryWrapper<>();
                countWrapper.eq("case_id", caseId);
                long caseCount = communicationRecordMapper.selectCount(countWrapper);
                System.out.println("[通信记录API] 案件 " + caseId + " 的通信记录数: " + caseCount);
                
                // 如果该案件没有记录，检查是否有其他案件ID（用于调试）
                if (caseCount == 0) {
                    QueryWrapper<CommunicationRecord> sampleWrapper = new QueryWrapper<>();
                    sampleWrapper.last("LIMIT 5");
                    List<CommunicationRecord> sampleRecords = communicationRecordMapper.selectList(sampleWrapper);
                    if (sampleRecords != null && !sampleRecords.isEmpty()) {
                        System.out.println("[通信记录API] 数据库中存在的案件ID示例:");
                        sampleRecords.forEach(r -> System.out.println("  - case_id: " + r.getCaseId() + ", channel: " + r.getChannel() + ", contact_result: " + r.getContactResult()));
                    }
                }
                
                // 使用 MyBatis-Plus 的 QueryWrapper 构建查询条件
                QueryWrapper<CommunicationRecord> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("case_id", caseId);
                
                // 如果指定了渠道，添加渠道筛选条件
                if (channel != null && !channel.trim().isEmpty()) {
                    queryWrapper.eq("channel", channel);
                }
                
                // 按触达时间倒序排列
                queryWrapper.orderByDesc("contacted_at");
                
                // 执行查询
                List<CommunicationRecord> dbRecords = communicationRecordMapper.selectList(queryWrapper);
                
                System.out.println("[通信记录API] 数据库查询结果: caseId=" + caseId + ", 记录数=" + (dbRecords != null ? dbRecords.size() : 0));
                
                if (dbRecords != null && !dbRecords.isEmpty()) {
                    // 转换为Map格式，保持与前端期望的格式一致
                    // 同时查询关联的联系人信息
                    records = dbRecords.stream().map(record -> convertToMap(record, caseContactMapper)).collect(Collectors.toList());
                    System.out.println("[通信记录API] 从数据库查询到 " + records.size() + " 条记录");
                    
                    // 输出每条记录的 contact_result，用于调试
                    for (int i = 0; i < records.size(); i++) {
                        Map<String, Object> record = records.get(i);
                        System.out.println("[通信记录API] 记录 " + (i + 1) + ": id=" + record.get("id") + 
                            ", channel=" + record.get("channel") + 
                            ", contact_result=" + record.get("contact_result") +
                            ", contact_person_id=" + record.get("contact_person_id"));
                    }
                } else {
                    System.out.println("[通信记录API] 数据库中没有找到案件 " + caseId + " 的通信记录");
                    // 如果数据库中没有数据，尝试从Mock数据获取（与collection-records API保持一致）
                    records = generateMockCommunications(caseId);
                    if (records.isEmpty()) {
                        System.out.println("[通信记录API] Mock数据也为空，返回空数组");
                    } else {
                        System.out.println("[通信记录API] 使用Mock数据，返回 " + records.size() + " 条记录");
                    }
                }
            } catch (Exception e) {
                System.err.println("[通信记录API] 查询数据库失败: " + e.getMessage());
                e.printStackTrace();
                // 如果查询失败，尝试使用Mock数据
                records = generateMockCommunications(caseId);
            }
        } else {
            System.out.println("[通信记录API] ⚠️ CommunicationRecordMapper 未注入，使用Mock数据");
            System.out.println("[通信记录API] ⚠️ 这可能是因为：");
            System.out.println("[通信记录API] ⚠️   1. 数据库表不存在");
            System.out.println("[通信记录API] ⚠️   2. MyBatis Mapper 扫描配置问题");
            System.out.println("[通信记录API] ⚠️   3. 数据库连接问题");
            records = generateMockCommunications(caseId);
        }
        
        System.out.println("[通信记录API] 最终返回记录数: " + records.size());
        System.out.println("===============================================");
        
        return ResponseData.success(records);
    }
    
    /**
     * 生成Mock通信记录数据（与collection-records API保持一致）
     * 用于在数据库中没有数据时提供测试数据
     */
    private List<Map<String, Object>> generateMockCommunications(Long caseId) {
        List<Map<String, Object>> mockRecords = new ArrayList<>();
        
        // 根据caseId生成不同的Mock数据，模拟不同的状态
        int mod = (int) (caseId % 5);
        
        // 为 BT0001_CASE_001 (caseId=91) 生成有"可联"催记的数据
        if (caseId == 91 || mod == 1) {
            // 电话记录 - 可联
            Map<String, Object> phoneRecord = new HashMap<>();
            phoneRecord.put("id", 1000L + caseId);
            phoneRecord.put("case_id", caseId);
            phoneRecord.put("collector_id", 1L);
            phoneRecord.put("contact_person_id", null); // 本人
            phoneRecord.put("channel", "phone");
            phoneRecord.put("direction", "outbound");
            phoneRecord.put("contact_result", "contacted"); // 可联
            phoneRecord.put("is_connected", true);
            phoneRecord.put("call_duration", 120);
            phoneRecord.put("contacted_at", java.time.LocalDateTime.now().minusDays(1).toString());
            phoneRecord.put("created_at", java.time.LocalDateTime.now().minusDays(1).toString());
            phoneRecord.put("contact_person", null);
            mockRecords.add(phoneRecord);
            
            // WhatsApp记录 - 有回复
            Map<String, Object> waRecord = new HashMap<>();
            waRecord.put("id", 2000L + caseId);
            waRecord.put("case_id", caseId);
            waRecord.put("collector_id", 1L);
            waRecord.put("contact_person_id", null); // 本人
            waRecord.put("channel", "whatsapp");
            waRecord.put("direction", "inbound"); // 客户回复
            waRecord.put("is_replied", true);
            waRecord.put("message_content", "我这两天就还");
            waRecord.put("contacted_at", java.time.LocalDateTime.now().minusHours(2).toString());
            waRecord.put("created_at", java.time.LocalDateTime.now().minusHours(2).toString());
            waRecord.put("contact_person", null);
            mockRecords.add(waRecord);
        } else if (mod == 2) {
            // 电话记录 - 已拨打但不可联
            Map<String, Object> phoneRecord = new HashMap<>();
            phoneRecord.put("id", 1000L + caseId);
            phoneRecord.put("case_id", caseId);
            phoneRecord.put("collector_id", 1L);
            phoneRecord.put("contact_person_id", null);
            phoneRecord.put("channel", "phone");
            phoneRecord.put("direction", "outbound");
            phoneRecord.put("contact_result", "not_connected"); // 未接通
            phoneRecord.put("is_connected", false);
            phoneRecord.put("contacted_at", java.time.LocalDateTime.now().minusDays(2).toString());
            phoneRecord.put("created_at", java.time.LocalDateTime.now().minusDays(2).toString());
            phoneRecord.put("contact_person", null);
            mockRecords.add(phoneRecord);
        }
        // 其他情况返回空数组（表示没有通信记录）
        
        return mockRecords;
    }
    
    /**
     * 将 CommunicationRecord 实体转换为 Map
     * 保持与前端期望的格式一致
     * 
     * @param record 通信记录实体
     * @param caseContactMapper 联系人Mapper（用于查询关联的联系人信息）
     */
    private Map<String, Object> convertToMap(CommunicationRecord record, CaseContactMapper caseContactMapper) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", record.getId());
        map.put("case_id", record.getCaseId());
        map.put("collector_id", record.getCollectorId());
        map.put("contact_person_id", record.getContactPersonId());
        map.put("channel", record.getChannel());
        map.put("direction", record.getDirection());
        map.put("supplier_id", record.getSupplierId());
        map.put("infinity_extension_number", record.getInfinityExtensionNumber());
        map.put("call_uuid", record.getCallUuid());
        map.put("custom_params", record.getCustomParams());
        map.put("call_duration", record.getCallDuration());
        map.put("is_connected", record.getIsConnected());
        map.put("call_record_url", record.getCallRecordUrl());
        map.put("is_replied", record.getIsReplied());
        map.put("message_content", record.getMessageContent());
        map.put("contact_result", record.getContactResult());
        map.put("ttfc_seconds", record.getTtfcSeconds());
        map.put("remark", record.getRemark());
        map.put("contacted_at", record.getContactedAt() != null ? record.getContactedAt().toString() : null);
        map.put("created_at", record.getCreatedAt() != null ? record.getCreatedAt().toString() : null);
        map.put("updated_at", record.getUpdatedAt() != null ? record.getUpdatedAt().toString() : null);
        
        // 如果有关联的联系人ID，查询联系人信息
        if (record.getContactPersonId() != null && caseContactMapper != null) {
            try {
                CaseContact contact = caseContactMapper.selectById(record.getContactPersonId());
                if (contact != null) {
                    Map<String, Object> contactMap = new HashMap<>();
                    contactMap.put("id", contact.getId());
                    contactMap.put("case_id", contact.getCaseId());
                    contactMap.put("contact_name", contact.getContactName());
                    contactMap.put("phone_number", contact.getPhoneNumber());
                    contactMap.put("relation", contact.getRelation());
                    contactMap.put("is_primary", contact.getIsPrimary());
                    contactMap.put("available_channels", contact.getAvailableChannels());
                    contactMap.put("remark", contact.getRemark());
                    map.put("contact_person", contactMap);
                }
            } catch (Exception e) {
                System.err.println("[通信记录API] 查询联系人信息失败: " + e.getMessage());
                // 如果查询失败，不添加 contact_person，前端会使用 contact_person_id 判断
            }
        }
        
        return map;
    }
}

