package com.cco.controller;

import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 还款渠道管理Controller - 管理控台专用
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/payment-channels")
public class AdminPaymentChannelController {

    // 内存中存储渠道数据（Mock数据库）
    private static final Map<Long, Map<String, Object>> channelStore = new HashMap<>();

    static {
        // 初始化Mock数据
        initMockData();
    }

    /**
     * 初始化Mock数据
     */
    private static void initMockData() {
        // 为甲方1创建测试渠道数据
        createChannel(1L, 1L, "GCash", "https://example.com/icons/gcash.png", "VA", "Xendit", 
                "菲律宾最流行的电子钱包支付方式", "https://api.example.com/payment/gcash/create", 
                "POST", "API_KEY", "{\"api_key\": \"test_api_key\"}", 
                "{\"loan_id\": \"{loan_id}\", \"case_id\": \"{case_id}\", \"installment_number\": \"{installment_number}\", \"amount\": \"{amount}\", \"customer_name\": \"{customer_name}\", \"customer_phone\": \"{customer_phone}\"}", 
                1, 1);
        
        createChannel(1L, 2L, "BCA Virtual Account", "https://example.com/icons/bca.png", "VA", "Midtrans", 
                "印尼BCA银行虚拟账户", "https://api.example.com/payment/bca-va/create", 
                "POST", "BEARER", "{\"token\": \"test_bearer_token\"}", 
                "{\"loan_id\": \"{loan_id}\", \"case_id\": \"{case_id}\", \"installment_number\": \"{installment_number}\", \"amount\": \"{amount}\", \"customer_id\": \"{customer_id}\"}", 
                1, 2);
        
        createChannel(1L, 3L, "OXXO Pay", "https://example.com/icons/oxxo.png", "H5", "Conekta", 
                "墨西哥便利店现金支付", "https://api.example.com/payment/oxxo/create", 
                "POST", "API_KEY", "{\"api_key\": \"test_api_key\"}", 
                "{\"loan_id\": \"{loan_id}\", \"amount\": \"{amount}\", \"customer_name\": \"{customer_name}\"}", 
                1, 3);
        
        createChannel(1L, 4L, "QRIS", "https://example.com/icons/qris.png", "QR", "Xendit", 
                "印尼统一二维码支付", "https://api.example.com/payment/qris/create", 
                "POST", "API_KEY", "{\"api_key\": \"test_api_key\"}", 
                "{\"loan_id\": \"{loan_id}\", \"case_id\": \"{case_id}\", \"amount\": \"{amount}\"}", 
                1, 4);
        
        log.info("========== 初始化还款渠道Mock数据，数量={} ==========", channelStore.size());
    }

    /**
     * 创建渠道数据
     */
    private static void createChannel(Long partyId, Long id, String channelName, String channelIcon, 
            String channelType, String serviceProvider, String description, String apiUrl, 
            String apiMethod, String authType, String authConfig, String requestParams, 
            Integer isEnabled, Integer sortOrder) {
        Map<String, Object> channel = new HashMap<>();
        channel.put("id", id);
        channel.put("party_id", partyId);
        channel.put("channel_name", channelName);
        channel.put("channel_icon", channelIcon);
        channel.put("channel_type", channelType);
        channel.put("service_provider", serviceProvider);
        channel.put("description", description);
        channel.put("api_url", apiUrl);
        channel.put("api_method", apiMethod);
        channel.put("auth_type", authType);
        channel.put("auth_config", authConfig);
        channel.put("request_params", requestParams);
        channel.put("is_enabled", isEnabled);
        channel.put("sort_order", sortOrder);
        channel.put("created_by", 1L);
        channel.put("updated_by", 1L);
        channel.put("created_at", "2025-01-01T00:00:00");
        channel.put("updated_at", "2025-11-25T00:00:00");
        
        channelStore.put(id, channel);
    }

    /**
     * 获取渠道列表
     */
    @GetMapping
    public ResponseData<Map<String, Object>> getPaymentChannels(
            @RequestParam Long party_id,
            @RequestParam(required = false) Boolean is_enabled,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "100") Integer page_size) {
        log.info("========== 获取还款渠道列表，party_id={}, is_enabled={}, page={}, page_size={} ==========", 
                party_id, is_enabled, page, page_size);
        
        List<Map<String, Object>> channels = new ArrayList<>();
        
        // 从Mock数据中筛选
        for (Map<String, Object> channel : channelStore.values()) {
            Long channelPartyId = ((Number) channel.get("party_id")).longValue();
            
            // 筛选party_id
            if (!party_id.equals(channelPartyId)) {
                continue;
            }
            
            // 筛选is_enabled
            if (is_enabled != null) {
                Integer enabled = (Integer) channel.get("is_enabled");
                if (!is_enabled.equals(enabled == 1)) {
                    continue;
                }
            }
            
            // 创建副本返回（不包含敏感信息）
            Map<String, Object> channelCopy = new HashMap<>(channel);
            // auth_config是敏感信息，不返回给前端
            channelCopy.remove("auth_config");
            channels.add(channelCopy);
        }
        
        // 按sort_order和created_at排序
        channels.sort((a, b) -> {
            Integer sortOrderA = (Integer) a.get("sort_order");
            Integer sortOrderB = (Integer) b.get("sort_order");
            int compare = sortOrderA.compareTo(sortOrderB);
            if (compare != 0) {
                return compare;
            }
            String createdAtA = (String) a.get("created_at");
            String createdAtB = (String) b.get("created_at");
            return createdAtA.compareTo(createdAtB);
        });
        
        // 分页
        int total = channels.size();
        int offset = (page - 1) * page_size;
        int end = Math.min(offset + page_size, total);
        List<Map<String, Object>> pagedChannels = offset < total ? channels.subList(offset, end) : new ArrayList<>();
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("page_size", page_size);
        result.put("list", pagedChannels);
        
        log.info("========== 返回还款渠道列表，总数={}, 当前页={} ==========", total, pagedChannels.size());
        return ResponseData.success(result);
    }

    /**
     * 获取渠道详情
     */
    @GetMapping("/{channelId}")
    public ResponseData<Map<String, Object>> getPaymentChannel(@PathVariable Long channelId) {
        log.info("========== 获取还款渠道详情，channelId={} ==========", channelId);
        
        Map<String, Object> channel = channelStore.get(channelId);
        if (channel == null) {
            return ResponseData.error("渠道不存在，channelId=" + channelId);
        }
        
        // 创建副本返回（不包含敏感信息）
        Map<String, Object> channelCopy = new HashMap<>(channel);
        // auth_config是敏感信息，不返回给前端
        channelCopy.remove("auth_config");
        
        return ResponseData.success(channelCopy);
    }

    /**
     * 创建渠道
     */
    @PostMapping
    public ResponseData<Map<String, Object>> createPaymentChannel(@RequestBody Map<String, Object> request) {
        log.info("========== 创建还款渠道，request={} ==========", request);
        
        Long partyId = getLongValue(request, "party_id");
        String channelName = getStringValue(request, "channel_name");
        String channelType = getStringValue(request, "channel_type");
        String apiUrl = getStringValue(request, "api_url");
        String authType = getStringValue(request, "auth_type");
        
        if (partyId == null || channelName == null || channelType == null || apiUrl == null || authType == null) {
            return ResponseData.error("参数不完整：party_id、channel_name、channel_type、api_url、auth_type为必填项");
        }
        
        // 生成新ID
        long newId = System.currentTimeMillis();
        
        Map<String, Object> channel = new HashMap<>();
        channel.put("id", newId);
        channel.put("party_id", partyId);
        channel.put("channel_name", channelName);
        channel.put("channel_icon", getStringValue(request, "channel_icon"));
        channel.put("channel_type", channelType);
        channel.put("service_provider", getStringValue(request, "service_provider"));
        channel.put("description", getStringValue(request, "description"));
        channel.put("api_url", apiUrl);
        channel.put("api_method", getStringValue(request, "api_method", "POST"));
        channel.put("auth_type", authType);
        channel.put("auth_config", getStringValue(request, "auth_config"));
        channel.put("request_params", getStringValue(request, "request_params"));
        channel.put("is_enabled", request.getOrDefault("is_enabled", 1));
        channel.put("sort_order", request.getOrDefault("sort_order", 0));
        channel.put("created_by", 1L);
        channel.put("updated_by", 1L);
        channel.put("created_at", new Date().toString());
        channel.put("updated_at", new Date().toString());
        
        channelStore.put(newId, channel);
        
        // 创建副本返回（不包含敏感信息）
        Map<String, Object> channelCopy = new HashMap<>(channel);
        channelCopy.remove("auth_config");
        
        log.info("========== 创建还款渠道成功，channelId={} ==========", newId);
        return ResponseData.success(channelCopy);
    }

    /**
     * 更新渠道
     */
    @PutMapping("/{channelId}")
    public ResponseData<Map<String, Object>> updatePaymentChannel(
            @PathVariable Long channelId,
            @RequestBody Map<String, Object> request) {
        log.info("========== 更新还款渠道，channelId={}, request={} ==========", channelId, request);
        
        Map<String, Object> channel = channelStore.get(channelId);
        if (channel == null) {
            return ResponseData.error("渠道不存在，channelId=" + channelId);
        }
        
        // 更新字段
        if (request.containsKey("channel_name")) {
            channel.put("channel_name", request.get("channel_name"));
        }
        if (request.containsKey("channel_icon")) {
            channel.put("channel_icon", request.get("channel_icon"));
        }
        if (request.containsKey("channel_type")) {
            channel.put("channel_type", request.get("channel_type"));
        }
        if (request.containsKey("service_provider")) {
            channel.put("service_provider", request.get("service_provider"));
        }
        if (request.containsKey("description")) {
            channel.put("description", request.get("description"));
        }
        if (request.containsKey("api_url")) {
            channel.put("api_url", request.get("api_url"));
        }
        if (request.containsKey("api_method")) {
            channel.put("api_method", request.get("api_method"));
        }
        if (request.containsKey("auth_type")) {
            channel.put("auth_type", request.get("auth_type"));
        }
        if (request.containsKey("auth_config")) {
            channel.put("auth_config", request.get("auth_config"));
        }
        if (request.containsKey("request_params")) {
            channel.put("request_params", request.get("request_params"));
        }
        if (request.containsKey("is_enabled")) {
            channel.put("is_enabled", request.get("is_enabled"));
        }
        if (request.containsKey("sort_order")) {
            channel.put("sort_order", request.get("sort_order"));
        }
        channel.put("updated_by", 1L);
        channel.put("updated_at", new Date().toString());
        
        // 创建副本返回（不包含敏感信息）
        Map<String, Object> channelCopy = new HashMap<>(channel);
        channelCopy.remove("auth_config");
        
        log.info("========== 更新还款渠道成功，channelId={} ==========", channelId);
        return ResponseData.success(channelCopy);
    }

    /**
     * 删除渠道
     */
    @DeleteMapping("/{channelId}")
    public ResponseData<String> deletePaymentChannel(@PathVariable Long channelId) {
        log.info("========== 删除还款渠道，channelId={} ==========", channelId);
        
        Map<String, Object> channel = channelStore.remove(channelId);
        if (channel == null) {
            return ResponseData.error("渠道不存在，channelId=" + channelId);
        }
        
        log.info("========== 删除还款渠道成功，channelId={} ==========", channelId);
        return ResponseData.success("删除成功");
    }

    /**
     * 切换启用状态
     */
    @PostMapping("/{channelId}/toggle")
    public ResponseData<Map<String, Object>> togglePaymentChannel(@PathVariable Long channelId) {
        log.info("========== 切换还款渠道状态，channelId={} ==========", channelId);
        
        Map<String, Object> channel = channelStore.get(channelId);
        if (channel == null) {
            return ResponseData.error("渠道不存在，channelId=" + channelId);
        }
        
        Integer isEnabled = (Integer) channel.get("is_enabled");
        channel.put("is_enabled", isEnabled == 1 ? 0 : 1);
        channel.put("updated_by", 1L);
        channel.put("updated_at", new Date().toString());
        
        // 创建副本返回（不包含敏感信息）
        Map<String, Object> channelCopy = new HashMap<>(channel);
        channelCopy.remove("auth_config");
        
        log.info("========== 切换还款渠道状态成功，channelId={}, is_enabled={} ==========", 
                channelId, channel.get("is_enabled"));
        return ResponseData.success(channelCopy);
    }

    /**
     * 更新排序
     */
    @PostMapping("/sort")
    public ResponseData<String> updateSort(@RequestBody Map<String, Object> request) {
        log.info("========== 更新还款渠道排序，request={} ==========", request);
        
        Long partyId = getLongValue(request, "party_id");
        @SuppressWarnings("unchecked")
        List<Object> channelIdsObj = (List<Object>) request.get("channel_ids");
        
        if (partyId == null || channelIdsObj == null || channelIdsObj.isEmpty()) {
            return ResponseData.error("参数不完整：party_id、channel_ids为必填项");
        }
        
        // 转换channelIds为Long列表（兼容Integer和Long）
        List<Long> channelIds = new ArrayList<>();
        for (Object id : channelIdsObj) {
            if (id instanceof Long) {
                channelIds.add((Long) id);
            } else if (id instanceof Integer) {
                channelIds.add(((Integer) id).longValue());
            } else if (id instanceof Number) {
                channelIds.add(((Number) id).longValue());
            } else if (id instanceof String) {
                try {
                    channelIds.add(Long.parseLong((String) id));
                } catch (NumberFormatException e) {
                    log.error("无法解析channel_id: {}", id);
                }
            }
        }
        
        if (channelIds.isEmpty()) {
            return ResponseData.error("channel_ids格式错误");
        }
        
        // 更新排序
        for (int i = 0; i < channelIds.size(); i++) {
            Long channelId = channelIds.get(i);
            Map<String, Object> channel = channelStore.get(channelId);
            if (channel != null && partyId.equals(channel.get("party_id"))) {
                channel.put("sort_order", i + 1);
                channel.put("updated_at", new Date().toString());
            }
        }
        
        log.info("========== 更新还款渠道排序成功，partyId={}, channelIds={} ==========", 
                partyId, channelIds);
        return ResponseData.success("排序更新成功");
    }

    /**
     * 辅助方法：获取Long值
     */
    private Long getLongValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                if (value instanceof Long) {
                    return (Long) value;
                } else if (value instanceof Integer) {
                    return ((Integer) value).longValue();
                } else if (value instanceof Number) {
                    return ((Number) value).longValue();
                } else if (value instanceof String) {
                    try {
                        return Long.parseLong((String) value);
                    } catch (NumberFormatException e) {
                        // 忽略
                    }
                }
            }
        }
        return null;
    }

    /**
     * 辅助方法：获取String值
     */
    private String getStringValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }
}

