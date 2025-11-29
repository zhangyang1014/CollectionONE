package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 渠道发送限制配置Controller - Mock实现
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/channel-limit-configs")
public class ChannelLimitConfigController {

    // 内存中存储配置数据（Mock数据库）
    private static final Map<String, Map<String, Object>> configStore = new HashMap<>();

    static {
        // 初始化Mock数据
        initMockData();
    }

    /**
     * 初始化Mock数据
     */
    private static void initMockData() {
        // 渠道列表
        String[] channels = {"sms", "rcs", "whatsapp", "call"};
        // 队列列表 - 使用标准队列编码：C、S0、S1、L1、M1
        Long[] queueIds = {1L, 2L, 3L, 4L, 5L};
        String[] queueCodes = {"C", "S0", "S1", "L1", "M1"};
        String[] queueNames = {"C队列", "S0队列", "S1队列", "L1队列", "M1队列"};

        long configId = 1L;
        for (String channel : channels) {
            for (int i = 0; i < queueIds.length; i++) {
                String key = 1L + "_" + channel + "_" + queueCodes[i];
                Map<String, Object> config = new HashMap<>();
                config.put("id", configId++);
                config.put("tenant_id", 1L);
                config.put("channel", channel);
                config.put("queue_id", queueIds[i]);
                config.put("queue_code", queueCodes[i]);
                config.put("queue_name", queueNames[i]);
                
                // 根据渠道设置不同的默认限制值
                if ("sms".equals(channel)) {
                    config.put("daily_limit_per_case", 100);
                    config.put("daily_limit_per_case_unlimited", false);
                    config.put("daily_limit_per_contact", 50);
                    config.put("daily_limit_per_contact_unlimited", false);
                    config.put("send_interval", 60);
                    config.put("send_interval_unlimited", false);
                } else if ("rcs".equals(channel)) {
                    config.put("daily_limit_per_case", 150);
                    config.put("daily_limit_per_case_unlimited", false);
                    config.put("daily_limit_per_contact", 80);
                    config.put("daily_limit_per_contact_unlimited", false);
                    config.put("send_interval", 30);
                    config.put("send_interval_unlimited", false);
                } else if ("whatsapp".equals(channel)) {
                    config.put("daily_limit_per_case", 200);
                    config.put("daily_limit_per_case_unlimited", false);
                    config.put("daily_limit_per_contact", 100);
                    config.put("daily_limit_per_contact_unlimited", false);
                    config.put("send_interval", 20);
                    config.put("send_interval_unlimited", false);
                } else if ("call".equals(channel)) {
                    config.put("daily_limit_per_case", 50);
                    config.put("daily_limit_per_case_unlimited", false);
                    config.put("daily_limit_per_contact", 30);
                    config.put("daily_limit_per_contact_unlimited", false);
                    config.put("send_interval", 120);
                    config.put("send_interval_unlimited", false);
                }
                
                config.put("enabled", true);
                config.put("created_at", "2025-01-01T00:00:00");
                config.put("updated_at", "2025-11-25T00:00:00");
                
                configStore.put(key, config);
            }
        }
        
        log.info("========== 初始化渠道限制配置Mock数据，数量={} ==========", configStore.size());
    }

    /**
     * 获取渠道限制配置列表
     */
    @GetMapping("/tenants/{tenantId}")
    public ResponseData<List<Map<String, Object>>> getChannelLimitConfigs(
            @PathVariable Long tenantId,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String queue) {
        log.info("========== 获取渠道限制配置列表，tenantId={}, channel={}, queue={} ==========", 
                tenantId, channel, queue);
        
        List<Map<String, Object>> configs = new ArrayList<>();
        
        // 从Mock数据中筛选，如果没有该tenantId的数据，则动态生成
        boolean hasTenantData = false;
        for (Map<String, Object> config : configStore.values()) {
            if (tenantId.equals(config.get("tenant_id"))) {
                hasTenantData = true;
                break;
            }
        }
        
        // 如果该tenantId没有数据，动态生成
        if (!hasTenantData) {
            generateConfigsForTenant(tenantId);
        }
        
        // 从Mock数据中筛选
        for (Map<String, Object> config : configStore.values()) {
            // 筛选tenant_id
            if (!tenantId.equals(config.get("tenant_id"))) {
                continue;
            }
            
            // 筛选channel
            if (channel != null && !channel.isEmpty() && !channel.equals(config.get("channel"))) {
                continue;
            }
            
            // 筛选queue
            if (queue != null && !queue.isEmpty() && !queue.equals(config.get("queue_code"))) {
                continue;
            }
            
            // 创建副本返回
            Map<String, Object> configCopy = new HashMap<>(config);
            configs.add(configCopy);
        }
        
        // 按channel和queue排序
        configs.sort((a, b) -> {
            int channelCompare = a.get("channel").toString().compareTo(b.get("channel").toString());
            if (channelCompare != 0) {
                return channelCompare;
            }
            return a.get("queue_code").toString().compareTo(b.get("queue_code").toString());
        });
        
        log.info("========== 返回渠道限制配置列表，数量={} ==========", configs.size());
        return ResponseData.success(configs);
    }
    
    /**
     * 为指定tenantId生成配置数据
     */
    private void generateConfigsForTenant(Long tenantId) {
        // 先获取队列列表（这里使用Mock数据）- 使用标准队列编码：C、S0、S1、L1、M1
        Long[] queueIds = {1L, 2L, 3L, 4L, 5L};
        String[] queueCodes = {"C", "S0", "S1", "L1", "M1"};
        String[] queueNames = {"C队列", "S0队列", "S1队列", "L1队列", "M1队列"};
        
        // 渠道列表
        String[] channels = {"sms", "rcs", "whatsapp", "call"};
        
        long maxConfigId = 0;
        for (Map<String, Object> config : configStore.values()) {
            Long id = ((Number) config.get("id")).longValue();
            if (id > maxConfigId) {
                maxConfigId = id;
            }
        }
        
        long configId = maxConfigId + 1;
        for (String channel : channels) {
            for (int i = 0; i < queueIds.length; i++) {
                String key = tenantId + "_" + channel + "_" + queueCodes[i];
                
                // 检查是否已存在
                if (configStore.containsKey(key)) {
                    continue;
                }
                
                Map<String, Object> config = new HashMap<>();
                config.put("id", configId++);
                config.put("tenant_id", tenantId);
                config.put("channel", channel);
                config.put("queue_id", queueIds[i]);
                config.put("queue_code", queueCodes[i]);
                config.put("queue_name", queueNames[i]);
                
                // 根据渠道设置不同的默认限制值
                if ("sms".equals(channel)) {
                    config.put("daily_limit_per_case", 100);
                    config.put("daily_limit_per_case_unlimited", false);
                    config.put("daily_limit_per_contact", 50);
                    config.put("daily_limit_per_contact_unlimited", false);
                    config.put("send_interval", 60);
                    config.put("send_interval_unlimited", false);
                } else if ("rcs".equals(channel)) {
                    config.put("daily_limit_per_case", 150);
                    config.put("daily_limit_per_case_unlimited", false);
                    config.put("daily_limit_per_contact", 80);
                    config.put("daily_limit_per_contact_unlimited", false);
                    config.put("send_interval", 30);
                    config.put("send_interval_unlimited", false);
                } else if ("whatsapp".equals(channel)) {
                    config.put("daily_limit_per_case", 200);
                    config.put("daily_limit_per_case_unlimited", false);
                    config.put("daily_limit_per_contact", 100);
                    config.put("daily_limit_per_contact_unlimited", false);
                    config.put("send_interval", 20);
                    config.put("send_interval_unlimited", false);
                } else if ("call".equals(channel)) {
                    config.put("daily_limit_per_case", 50);
                    config.put("daily_limit_per_case_unlimited", false);
                    config.put("daily_limit_per_contact", 30);
                    config.put("daily_limit_per_contact_unlimited", false);
                    config.put("send_interval", 120);
                    config.put("send_interval_unlimited", false);
                }
                
                config.put("enabled", true);
                config.put("created_at", "2025-01-01T00:00:00");
                config.put("updated_at", "2025-11-25T00:00:00");
                
                configStore.put(key, config);
            }
        }
        
        log.info("========== 为tenantId={}生成渠道限制配置，数量={} ==========", tenantId, channels.length * queueIds.length);
    }

    /**
     * 获取单个渠道限制配置
     */
    @GetMapping("/{configId}")
    public ResponseData<Map<String, Object>> getChannelLimitConfig(@PathVariable Long configId) {
        log.info("========== 获取渠道限制配置详情，configId={} ==========", configId);
        
        // 从Mock数据中查找
        for (Map<String, Object> config : configStore.values()) {
            if (configId.equals(config.get("id"))) {
                Map<String, Object> configCopy = new HashMap<>(config);
                return ResponseData.success(configCopy);
            }
        }
        
        // 如果找不到，返回默认配置
        Map<String, Object> defaultConfig = new HashMap<>();
        defaultConfig.put("id", configId);
        defaultConfig.put("tenant_id", 1L);
        defaultConfig.put("channel", "sms");
        defaultConfig.put("queue_id", 1L);
        defaultConfig.put("queue_code", "C");
        defaultConfig.put("queue_name", "C队列");
        defaultConfig.put("daily_limit_per_case", null);
        defaultConfig.put("daily_limit_per_case_unlimited", true);
        defaultConfig.put("daily_limit_per_contact", null);
        defaultConfig.put("daily_limit_per_contact_unlimited", true);
        defaultConfig.put("send_interval", null);
        defaultConfig.put("send_interval_unlimited", true);
        defaultConfig.put("enabled", true);
        
        return ResponseData.success(defaultConfig);
    }

    /**
     * 创建或更新渠道限制配置
     */
    @PostMapping
    public ResponseData<Map<String, Object>> saveChannelLimitConfig(@RequestBody Map<String, Object> request) {
        log.info("========== 保存渠道限制配置，request={} ==========", request);
        
        Long tenantId = getLongValue(request, "tenant_id", "tenantId");
        String channel = getStringValue(request, "channel");
        String queueCode = getStringValue(request, "queue_code", "queueCode");
        
        if (tenantId == null || channel == null || queueCode == null) {
            return ResponseData.error("参数不完整：tenant_id、channel、queue_code为必填项");
        }
        
        String key = tenantId + "_" + channel + "_" + queueCode;
        Map<String, Object> config = configStore.get(key);
        
        if (config == null) {
            // 创建新配置
            config = new HashMap<>();
            config.put("id", System.currentTimeMillis());
            config.put("tenant_id", tenantId);
            config.put("channel", channel);
            config.put("queue_code", queueCode);
            config.put("created_at", new Date().toString());
        }
        
        // 更新配置
        Long queueId = getLongValue(request, "queue_id", "queueId");
        if (queueId != null) {
            config.put("queue_id", queueId);
        }
        
        String queueName = getStringValue(request, "queue_name", "queueName");
        if (queueName != null) {
            config.put("queue_name", queueName);
        }
        
        // 每日每案件限制数量
        if (request.containsKey("daily_limit_per_case")) {
            config.put("daily_limit_per_case", request.get("daily_limit_per_case"));
        }
        if (request.containsKey("daily_limit_per_case_unlimited")) {
            config.put("daily_limit_per_case_unlimited", request.get("daily_limit_per_case_unlimited"));
        }
        
        // 每日每联系人限制数量
        if (request.containsKey("daily_limit_per_contact")) {
            config.put("daily_limit_per_contact", request.get("daily_limit_per_contact"));
        }
        if (request.containsKey("daily_limit_per_contact_unlimited")) {
            config.put("daily_limit_per_contact_unlimited", request.get("daily_limit_per_contact_unlimited"));
        }
        
        // 发送时间间隔
        if (request.containsKey("send_interval")) {
            config.put("send_interval", request.get("send_interval"));
        }
        if (request.containsKey("send_interval_unlimited")) {
            config.put("send_interval_unlimited", request.get("send_interval_unlimited"));
        }
        
        // 状态
        if (request.containsKey("enabled")) {
            config.put("enabled", request.get("enabled"));
        }
        
        config.put("updated_at", new Date().toString());
        
        // 保存到Mock数据
        configStore.put(key, config);
        
        Map<String, Object> configCopy = new HashMap<>(config);
        log.info("========== 保存渠道限制配置成功，configId={} ==========", config.get("id"));
        return ResponseData.success(configCopy);
    }

    /**
     * 更新渠道限制配置
     */
    @PutMapping("/{configId}")
    public ResponseData<Map<String, Object>> updateChannelLimitConfig(
            @PathVariable Long configId,
            @RequestBody Map<String, Object> request) {
        log.info("========== 更新渠道限制配置，configId={}, request={} ==========", configId, request);
        
        // 从Mock数据中查找
        Map<String, Object> config = null;
        String foundKey = null;
        for (Map.Entry<String, Map<String, Object>> entry : configStore.entrySet()) {
            if (configId.equals(entry.getValue().get("id"))) {
                config = entry.getValue();
                foundKey = entry.getKey();
                break;
            }
        }
        
        if (config == null) {
            return ResponseData.error("配置不存在，configId=" + configId);
        }
        
        // 更新配置
        Long queueId = getLongValue(request, "queue_id", "queueId");
        if (queueId != null) {
            config.put("queue_id", queueId);
        }
        
        String queueCode = getStringValue(request, "queue_code", "queueCode");
        if (queueCode != null) {
            config.put("queue_code", queueCode);
        }
        
        String queueName = getStringValue(request, "queue_name", "queueName");
        if (queueName != null) {
            config.put("queue_name", queueName);
        }
        
        // 每日每案件限制数量
        if (request.containsKey("daily_limit_per_case")) {
            config.put("daily_limit_per_case", request.get("daily_limit_per_case"));
        }
        if (request.containsKey("daily_limit_per_case_unlimited")) {
            config.put("daily_limit_per_case_unlimited", request.get("daily_limit_per_case_unlimited"));
        }
        
        // 每日每联系人限制数量
        if (request.containsKey("daily_limit_per_contact")) {
            config.put("daily_limit_per_contact", request.get("daily_limit_per_contact"));
        }
        if (request.containsKey("daily_limit_per_contact_unlimited")) {
            config.put("daily_limit_per_contact_unlimited", request.get("daily_limit_per_contact_unlimited"));
        }
        
        // 发送时间间隔
        if (request.containsKey("send_interval")) {
            config.put("send_interval", request.get("send_interval"));
        }
        if (request.containsKey("send_interval_unlimited")) {
            config.put("send_interval_unlimited", request.get("send_interval_unlimited"));
        }
        
        // 状态
        if (request.containsKey("enabled")) {
            config.put("enabled", request.get("enabled"));
        }
        
        config.put("updated_at", new Date().toString());
        
        // 如果queue_code改变了，需要更新key
        if (queueCode != null && foundKey != null) {
            String channel = (String) config.get("channel");
            Long tenantId = ((Number) config.get("tenant_id")).longValue();
            String newKey = tenantId + "_" + channel + "_" + queueCode;
            if (!newKey.equals(foundKey)) {
                configStore.remove(foundKey);
                configStore.put(newKey, config);
            }
        }
        
        Map<String, Object> configCopy = new HashMap<>(config);
        log.info("========== 更新渠道限制配置成功，configId={} ==========", configId);
        return ResponseData.success(configCopy);
    }

    /**
     * 删除渠道限制配置
     */
    @DeleteMapping("/{configId}")
    public ResponseData<String> deleteChannelLimitConfig(@PathVariable Long configId) {
        log.info("========== 删除渠道限制配置，configId={} ==========", configId);
        
        // 从Mock数据中查找并删除
        String foundKey = null;
        for (Map.Entry<String, Map<String, Object>> entry : configStore.entrySet()) {
            if (configId.equals(entry.getValue().get("id"))) {
                foundKey = entry.getKey();
                break;
            }
        }
        
        if (foundKey != null) {
            configStore.remove(foundKey);
            log.info("========== 删除渠道限制配置成功，configId={} ==========", configId);
            return ResponseData.success("删除成功");
        } else {
            return ResponseData.error("配置不存在，configId=" + configId);
        }
    }

    /**
     * 批量保存渠道限制配置
     */
    @PostMapping("/batch")
    public ResponseData<String> batchSaveChannelLimitConfigs(@RequestBody Map<String, Object> request) {
        log.info("========== 批量保存渠道限制配置，request={} ==========", request);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> configs = (List<Map<String, Object>>) request.get("configs");
        if (configs == null || configs.isEmpty()) {
            return ResponseData.error("配置列表不能为空");
        }
        
        int successCount = 0;
        for (Map<String, Object> config : configs) {
            try {
                saveChannelLimitConfig(config);
                successCount++;
            } catch (Exception e) {
                log.error("保存配置失败：", e);
            }
        }
        
        log.info("========== 批量保存渠道限制配置完成，成功={}, 总数={} ==========", 
                successCount, configs.size());
        return ResponseData.success("批量保存成功，成功数量：" + successCount);
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

