package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 通信记录Controller - Mock实现
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/communications")
public class CommunicationController {

    /**
     * 获取案件的通信记录
     */
    @GetMapping("/case/{caseId}")
    public ResponseData<List<Map<String, Object>>> getCaseCommunications(@PathVariable Long caseId) {
        log.info("========== 获取案件通信记录，caseId={} ==========", caseId);
        
        List<Map<String, Object>> communications = new ArrayList<>();
        
        // 生成Mock通信记录数据
        String[] channels = {"phone", "whatsapp", "sms", "rcs"};
        String[] directions = {"outbound", "inbound"};
        String[] contactResults = {"connected", "not_connected", "replied", "not_replied"};
        
        Random random = new Random();
        int recordCount = 3 + random.nextInt(5); // 3-7条记录
        
        for (int i = 0; i < recordCount; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", (long) (caseId * 100 + i));
            record.put("case_id", caseId);
            record.put("collector_id", 1L);
            record.put("contact_person_id", 1L + i);
            record.put("channel", channels[random.nextInt(channels.length)]);
            record.put("direction", directions[random.nextInt(directions.length)]);
            record.put("contact_result", contactResults[random.nextInt(contactResults.length)]);
            record.put("contacted_at", LocalDateTime.now().minusHours(i).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            // 根据渠道类型设置不同的字段
            String channel = (String) record.get("channel");
            if ("phone".equals(channel)) {
                record.put("call_duration", 60 + random.nextInt(300));
                record.put("is_connected", random.nextBoolean());
                record.put("call_record_url", "https://record.example.com/call_" + record.get("id") + ".mp3");
            } else {
                record.put("message_content", "消息内容 " + i);
                record.put("is_replied", random.nextBoolean());
            }
            
            record.put("remark", "备注信息 " + i);
            record.put("created_at", LocalDateTime.now().minusHours(i).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            record.put("updated_at", LocalDateTime.now().minusHours(i).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            communications.add(record);
        }
        
        // 按时间倒序排列
        communications.sort((a, b) -> {
            String timeA = (String) a.get("contacted_at");
            String timeB = (String) b.get("contacted_at");
            return timeB.compareTo(timeA);
        });
        
        log.info("========== 返回通信记录，数量={} ==========", communications.size());
        return ResponseData.success(communications);
    }
}

