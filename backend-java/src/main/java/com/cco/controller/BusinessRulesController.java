package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.service.BusinessRulesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 业务规则Controller
 * 根据PRD要求，提供业务规则相关的接口
 * 
 * @author CCO Team
 * @since 2025-01-11
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/business-rules")
public class BusinessRulesController {
    
    @Autowired
    private BusinessRulesService businessRulesService;

    /**
     * 检查是否在营业时间内
     * 根据PRD要求：GET /api/v1/business-rules/working-hours/check
     * 
     * @param agency_id 机构ID
     * @param datetime 要检查的日期时间（ISO格式，如：2025-01-11T14:30:00）
     * @return 是否在营业时间内
     */
    @GetMapping("/working-hours/check")
    public ResponseData<Map<String, Object>> checkWorkingHours(
            @RequestParam Long agency_id,
            @RequestParam(required = false) String datetime) {
        log.info("========== 检查是否在营业时间内，agency_id={}, datetime={} ==========", agency_id, datetime);
        
        // 如果没有提供datetime，使用当前时间
        LocalDateTime checkTime;
        if (datetime != null && !datetime.isEmpty()) {
            try {
                checkTime = LocalDateTime.parse(datetime.replace("Z", ""), 
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e) {
                checkTime = LocalDateTime.now();
            }
        } else {
            checkTime = LocalDateTime.now();
        }
        
        // 使用Service检查是否在营业时间内
        boolean isWorkingHours = businessRulesService.checkWorkingHours(agency_id, checkTime);
        
        // 获取星期几（PRD要求：1-7，1=周一）
        int dayOfWeek = checkTime.getDayOfWeek().getValue(); // Java的DayOfWeek：1=周一，7=周日
        
        // 获取时间（HH:MM格式）
        String timeStr = checkTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        
        Map<String, Object> result = new HashMap<>();
        result.put("agency_id", agency_id);
        result.put("datetime", checkTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        result.put("day_of_week", dayOfWeek); // PRD要求：1-7，1=周一
        result.put("time", timeStr);
        result.put("is_working_hours", isWorkingHours);
        result.put("message", isWorkingHours ? "在营业时间内" : "不在营业时间内");
        
        log.info("========== 检查结果：is_working_hours={} ==========", isWorkingHours);
        return ResponseData.success(result);
    }
}

