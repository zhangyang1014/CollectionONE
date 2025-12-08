package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 催员绩效Controller - Mock实现
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/performance")
public class PerformanceController {

    /**
     * 获取催员个人绩效看板数据
     */
    @GetMapping("/collector/{collectorId}")
    public ResponseData<Map<String, Object>> getCollectorPerformance(
            @PathVariable Long collectorId,
            @RequestParam(required = false) String start_date,
            @RequestParam(required = false) String end_date,
            @RequestParam(required = false) String period) {
        log.info("========== 获取催员绩效，collectorId={}, start_date={}, end_date={}, period={} ==========", 
                collectorId, start_date, end_date, period);
        
        Map<String, Object> performance = new HashMap<>();
        
        // 基础统计
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCases", 150);
        summary.put("activeCases", 120);
        summary.put("closedCases", 30);
        summary.put("totalAmount", 500000.00);
        summary.put("collectedAmount", 300000.00);
        summary.put("collectionRate", 60.0);
        summary.put("avgCallDuration", 180); // 秒
        summary.put("totalCalls", 450);
        summary.put("successfulCalls", 320);
        summary.put("callSuccessRate", 71.1);
        performance.put("summary", summary);
        
        // 时间段数据
        List<Map<String, Object>> periodData = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", "2025-11-" + String.format("%02d", 18 + i));
            dayData.put("cases", 20 + (int)(Math.random() * 10));
            dayData.put("amount", 50000 + (int)(Math.random() * 20000));
            dayData.put("calls", 60 + (int)(Math.random() * 20));
            dayData.put("collectionRate", 50 + Math.random() * 20);
            periodData.add(dayData);
        }
        performance.put("periodData", periodData);
        
        // 排名信息
        Map<String, Object> ranking = new HashMap<>();
        ranking.put("rank", 5);
        ranking.put("totalCollectors", 50);
        ranking.put("percentile", 90.0);
        performance.put("ranking", ranking);
        
        log.info("========== 返回催员绩效数据 ==========");
        return ResponseData.success(performance);
    }

    /**
     * 获取催员趋势数据
     */
    @GetMapping("/collector/{collectorId}/trend")
    public ResponseData<Map<String, Object>> getCollectorTrend(
            @PathVariable Long collectorId,
            @RequestParam(required = false) String start_date,
            @RequestParam(required = false) String end_date,
            @RequestParam(required = false) String metric) {
        log.info("========== 获取催员趋势，collectorId={}, metric={} ==========", collectorId, metric);
        
        Map<String, Object> trend = new HashMap<>();
        
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Map<String, Object> point = new HashMap<>();
            point.put("date", "2025-10-" + String.format("%02d", 26 + i));
            point.put("value", 100 + Math.random() * 50);
            data.add(point);
        }
        trend.put("data", data);
        trend.put("metric", metric != null ? metric : "collectionRate");
        
        return ResponseData.success(trend);
    }

    /**
     * 获取催员对比数据
     */
    @GetMapping("/collector/{collectorId}/comparison")
    public ResponseData<Map<String, Object>> getCollectorComparison(
            @PathVariable Long collectorId,
            @RequestParam(required = false) String start_date,
            @RequestParam(required = false) String end_date) {
        log.info("========== 获取催员对比，collectorId={} ==========", collectorId);
        
        Map<String, Object> comparison = new HashMap<>();
        
        Map<String, Object> current = new HashMap<>();
        current.put("collectionRate", 60.0);
        current.put("avgCallDuration", 180);
        current.put("totalCalls", 450);
        comparison.put("current", current);
        
        Map<String, Object> average = new HashMap<>();
        average.put("collectionRate", 55.0);
        average.put("avgCallDuration", 165);
        average.put("totalCalls", 420);
        comparison.put("average", average);
        
        Map<String, Object> top = new HashMap<>();
        top.put("collectionRate", 75.0);
        top.put("avgCallDuration", 200);
        top.put("totalCalls", 500);
        comparison.put("top", top);
        
        return ResponseData.success(comparison);
    }

    /**
     * 获取催员排名
     */
    @GetMapping("/collector/{collectorId}/ranking")
    public ResponseData<Map<String, Object>> getCollectorRanking(
            @PathVariable Long collectorId,
            @RequestParam(required = false) String start_date,
            @RequestParam(required = false) String end_date,
            @RequestParam(required = false) String metric) {
        log.info("========== 获取催员排名，collectorId={}, metric={} ==========", collectorId, metric);
        
        Map<String, Object> ranking = new HashMap<>();
        ranking.put("rank", 5);
        ranking.put("totalCollectors", 50);
        ranking.put("percentile", 90.0);
        ranking.put("metric", metric != null ? metric : "collectionRate");
        
        // 排名列表（前后5名）
        List<Map<String, Object>> rankingList = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("rank", i + 1);
            item.put("collectorId", 100 + i);
            item.put("collectorName", "催员" + (100 + i));
            item.put("value", 80.0 - i * 2.0);
            item.put("isCurrent", i == 4);
            rankingList.add(item);
        }
        ranking.put("rankingList", rankingList);
        
        return ResponseData.success(ranking);
    }

    /**
     * 获取催员案件明细
     */
    @GetMapping("/collector/{collectorId}/cases")
    public ResponseData<Map<String, Object>> getCollectorCases(
            @PathVariable Long collectorId,
            @RequestParam(required = false) String start_date,
            @RequestParam(required = false) String end_date,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("========== 获取催员案件明细，collectorId={}, page={}, size={} ==========", collectorId, page, size);
        
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> cases = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Map<String, Object> caseItem = new HashMap<>();
            caseItem.put("id", (page - 1) * size + i + 1);
            caseItem.put("caseNumber", "CASE" + String.format("%06d", (page - 1) * size + i + 1));
            caseItem.put("debtorName", "债务人" + (i + 1));
            caseItem.put("amount", 10000 + Math.random() * 50000);
            caseItem.put("status", i % 3 == 0 ? "active" : "closed");
            caseItem.put("assignedDate", "2025-11-20");
            caseItem.put("lastContactDate", "2025-11-24");
            cases.add(caseItem);
        }
        result.put("cases", cases);
        
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("size", size);
        pagination.put("total", 150);
        pagination.put("totalPages", 15);
        result.put("pagination", pagination);
        
        return ResponseData.success(result);
    }

    /**
     * 获取催员通信记录
     */
    @GetMapping("/collector/{collectorId}/communications")
    public ResponseData<Map<String, Object>> getCollectorCommunications(
            @PathVariable Long collectorId,
            @RequestParam(required = false) String start_date,
            @RequestParam(required = false) String end_date,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("========== 获取催员通信记录，collectorId={}, page={}, size={} ==========", collectorId, page, size);
        
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> communications = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Map<String, Object> comm = new HashMap<>();
            comm.put("id", (page - 1) * size + i + 1);
            comm.put("caseId", (page - 1) * size + i + 1);
            comm.put("caseNumber", "CASE" + String.format("%06d", (page - 1) * size + i + 1));
            comm.put("type", i % 3 == 0 ? "call" : i % 3 == 1 ? "sms" : "email");
            comm.put("direction", i % 2 == 0 ? "outbound" : "inbound");
            comm.put("duration", 60 + (int)(Math.random() * 300));
            comm.put("timestamp", "2025-11-24T" + String.format("%02d", 9 + i) + ":00:00");
            comm.put("result", i % 4 == 0 ? "answered" : "no_answer");
            communications.add(comm);
        }
        result.put("communications", communications);
        
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("size", size);
        pagination.put("total", 450);
        pagination.put("totalPages", 45);
        result.put("pagination", pagination);
        
        return ResponseData.success(result);
    }
}


































