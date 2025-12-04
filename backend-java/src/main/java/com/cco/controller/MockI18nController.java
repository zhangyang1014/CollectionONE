package com.cco.controller;

import com.cco.common.result.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 多语言配置管理 Mock Controller
 * 提供临时Mock数据，等待真实实现
 */
@RestController
@RequestMapping("/admin")
public class MockI18nController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 语言管理API ====================

    /**
     * 获取语言列表
     */
    @GetMapping("/languages")
    public Result<?> getLanguages(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String direction,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        List<Map<String, Object>> languages = getMockLanguages();
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", languages);
        result.put("total", languages.size());
        result.put("page", page);
        result.put("pageSize", pageSize);
        
        return Result.success(languages);
    }

    /**
     * 获取语言详情
     */
    @GetMapping("/languages/{id}")
    public Result<?> getLanguageDetail(@PathVariable Long id) {
        List<Map<String, Object>> languages = getMockLanguages();
        return languages.stream()
                .filter(lang -> id.equals(lang.get("id")))
                .findFirst()
                .map(Result::success)
                .orElse(Result.error("语言不存在"));
    }

    /**
     * 新增语言
     */
    @PostMapping("/languages")
    public Result<?> createLanguage(@RequestBody Map<String, Object> data) {
        Map<String, Object> newLanguage = new HashMap<>(data);
        newLanguage.put("id", System.currentTimeMillis());
        newLanguage.put("translationProgress", 0);
        newLanguage.put("missingKeysCount", 1200);
        newLanguage.put("version", "v1.0");
        newLanguage.put("createdAt", LocalDateTime.now().format(FORMATTER));
        newLanguage.put("updatedAt", LocalDateTime.now().format(FORMATTER));
        
        return Result.success(newLanguage);
    }

    /**
     * 编辑语言
     */
    @PutMapping("/languages/{id}")
    public Result<?> updateLanguage(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        data.put("id", id);
        data.put("updatedAt", LocalDateTime.now().format(FORMATTER));
        return Result.success(data);
    }

    /**
     * 启用语言
     */
    @PutMapping("/languages/{id}/enable")
    public Result<?> enableLanguage(@PathVariable Long id) {
        return Result.success("语言已启用");
    }

    /**
     * 停用语言
     */
    @PutMapping("/languages/{id}/disable")
    public Result<?> disableLanguage(@PathVariable Long id) {
        return Result.success("语言已停用");
    }

    /**
     * 批量更新语言状态
     */
    @PutMapping("/languages/batch-status")
    public Result<?> batchUpdateStatus(@RequestBody Map<String, Object> data) {
        return Result.success("批量操作成功");
    }

    /**
     * 设置默认语言
     */
    @PutMapping("/languages/default")
    public Result<?> setDefaultLanguage(@RequestBody Map<String, Object> data) {
        return Result.success("默认语言已设置");
    }

    /**
     * 删除语言
     */
    @DeleteMapping("/languages/{id}")
    public Result<?> deleteLanguage(@PathVariable Long id) {
        return Result.success("语言已删除");
    }

    /**
     * 更新语言排序
     */
    @PutMapping("/languages/sort")
    public Result<?> updateSort(@RequestBody List<Map<String, Object>> data) {
        return Result.success("排序已更新");
    }

    // ==================== 语言包管理API ====================

    /**
     * 获取语言包列表
     */
    @GetMapping("/translations/bundles")
    public Result<?> getTranslationBundles(
            @RequestParam(required = false, defaultValue = "zh-CN") String baseLocale
    ) {
        List<Map<String, Object>> bundles = getMockBundles();
        return Result.success(bundles);
    }

    /**
     * 下载语言包
     */
    @GetMapping("/languages/{locale}/bundle")
    public Result<?> downloadBundle(
            @PathVariable String locale,
            @RequestParam(required = false, defaultValue = "full") String type,
            @RequestParam(required = false, defaultValue = "json") String format
    ) {
        // 实际应返回文件流，这里返回提示
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Mock模式：下载功能需要后端完整实现");
        result.put("locale", locale);
        result.put("type", type);
        result.put("format", format);
        return Result.success(result);
    }

    /**
     * 校验上传文件
     */
    @PostMapping("/languages/{locale}/bundle/validate")
    public Result<?> validateBundle(
            @PathVariable String locale,
            @RequestParam("file") MultipartFile file,
            @RequestParam String strategy,
            @RequestParam(required = false) String version
    ) {
        Map<String, Object> validation = new HashMap<>();
        validation.put("isValid", true);
        validation.put("totalKeys", 1200);
        validation.put("newKeys", 44);
        validation.put("updatedKeys", 156);
        validation.put("unknownKeys", Arrays.asList("payment.new.feature.title"));
        validation.put("warnings", Arrays.asList("覆盖 156 个已存在的key"));
        validation.put("errors", new ArrayList<>());
        validation.put("progressBefore", 85);
        validation.put("progressAfter", 92);
        
        return Result.success(validation);
    }

    /**
     * 确认导入
     */
    @PostMapping("/languages/{locale}/bundle/confirm")
    public Result<?> confirmUpload(
            @PathVariable String locale,
            @RequestBody Map<String, Object> data
    ) {
        return Result.success("语言包已导入");
    }

    /**
     * 获取版本历史
     */
    @GetMapping("/languages/{locale}/versions")
    public Result<?> getVersionHistory(
            @PathVariable String locale,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        List<Map<String, Object>> versions = getMockVersions();
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", versions);
        result.put("total", versions.size());
        
        return Result.success(result);
    }

    /**
     * 下载指定版本
     */
    @GetMapping("/languages/{locale}/versions/{version}")
    public Result<?> downloadVersion(
            @PathVariable String locale,
            @PathVariable String version
    ) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Mock模式：下载功能需要后端完整实现");
        result.put("locale", locale);
        result.put("version", version);
        return Result.success(result);
    }

    /**
     * 回滚版本
     */
    @PostMapping("/languages/{locale}/versions/{version}/rollback")
    public Result<?> rollbackVersion(
            @PathVariable String locale,
            @PathVariable String version
    ) {
        return Result.success("版本已回滚到 " + version);
    }

    // ==================== 翻译统计API ====================

    /**
     * 获取翻译统计
     */
    @GetMapping("/translations/statistics")
    public Result<?> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalLanguages", 4);
        statistics.put("totalKeys", 1200);
        statistics.put("averageProgress", 78);
        statistics.put("totalMissing", 1840);
        
        // 各语言进度
        List<Map<String, Object>> languageProgress = new ArrayList<>();
        languageProgress.add(createProgressItem("zh-CN", "中文", "🇨🇳", 1200, 1200, 0, 100));
        languageProgress.add(createProgressItem("en-US", "English", "🇺🇸", 1200, 1020, 180, 85));
        languageProgress.add(createProgressItem("es-MX", "Español", "🇲🇽", 1200, 744, 456, 62));
        languageProgress.add(createProgressItem("id-ID", "Indonesia", "🇮🇩", 1200, 420, 780, 35));
        
        statistics.put("languageProgress", languageProgress);
        
        return Result.success(statistics);
    }

    /**
     * 获取命名空间统计
     */
    @GetMapping("/translations/statistics/namespaces")
    public Result<?> getNamespaceStatistics(@RequestParam(required = false) String baseLocale) {
        List<Map<String, Object>> namespaces = new ArrayList<>();
        namespaces.add(createNamespaceItem("common", 90, 200, 180));
        namespaces.add(createNamespaceItem("auth", 95, 80, 76));
        namespaces.add(createNamespaceItem("dashboard", 78, 150, 117));
        namespaces.add(createNamespaceItem("case", 68, 300, 204));
        namespaces.add(createNamespaceItem("payment", 45, 120, 54));
        namespaces.add(createNamespaceItem("field", 72, 100, 72));
        namespaces.add(createNamespaceItem("tenant", 80, 150, 120));
        namespaces.add(createNamespaceItem("organization", 65, 100, 65));
        
        return Result.success(namespaces);
    }

    /**
     * 获取缺失翻译
     */
    @GetMapping("/translations/missing")
    public Result<?> getMissingTranslations(
            @RequestParam String locale,
            @RequestParam(required = false) String namespace,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        List<Map<String, Object>> missing = new ArrayList<>();
        
        if ("en-US".equals(locale)) {
            missing.add(createMissingItem("payment.method.alipay", "支付宝", "payment", "P1"));
            missing.add(createMissingItem("payment.method.wechat", "微信支付", "payment", "P1"));
            missing.add(createMissingItem("case.status.overdue", "逾期", "case", "P0"));
            missing.add(createMissingItem("dashboard.chart.title", "数据趋势", "dashboard", "P2"));
            missing.add(createMissingItem("common.action.confirm", "确认", "common", "P0"));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", missing);
        result.put("total", missing.size());
        
        return Result.success(result);
    }

    /**
     * 导出缺失翻译
     */
    @GetMapping("/translations/missing/export")
    public Result<?> exportMissing(
            @RequestParam String locale,
            @RequestParam String format
    ) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Mock模式：导出功能需要后端完整实现");
        result.put("locale", locale);
        result.put("format", format);
        return Result.success(result);
    }

    /**
     * 获取质量问题
     */
    @GetMapping("/translations/quality-issues")
    public Result<?> getQualityIssues(
            @RequestParam String locale,
            @RequestParam(required = false) String type
    ) {
        List<Map<String, Object>> issues = new ArrayList<>();
        
        if ("en-US".equals(locale)) {
            issues.add(createQualityIssue("missing_variable", "common.message.welcome", 
                    "欢迎, {username}!", "Welcome!", "Welcome, {username}!"));
            issues.add(createQualityIssue("length_exceeded", "dashboard.description.revenue", 
                    "收入趋势", "The trend of revenue in recent months", null));
            issues.add(createQualityIssue("html_tag", "auth.message.error", 
                    "登录失败", "<b>Login failed", null));
            issues.add(createQualityIssue("plural_missing", "case.count", 
                    "{count} 个案件", "{count} cases", "no cases | one case | {count} cases"));
            issues.add(createQualityIssue("untranslated", "payment.button.submit", 
                    "提交", "提交", null));
        }
        
        return Result.success(issues);
    }

    /**
     * 重新检测质量
     */
    @PostMapping("/translations/quality-issues/recheck")
    public Result<?> recheckQuality(@RequestBody Map<String, Object> data) {
        return Result.success("质量检测完成");
    }

    // ==================== Helper Methods ====================

    private List<Map<String, Object>> getMockLanguages() {
        List<Map<String, Object>> languages = new ArrayList<>();
        
        languages.add(createLanguage(1L, "zh-CN", "中文", "🇨🇳", "ltr", 
                true, true, 1, 100, 0, "v2.3", "2024-12-03 14:30:00"));
        languages.add(createLanguage(2L, "en-US", "English", "🇺🇸", "ltr", 
                true, false, 2, 85, 156, "v2.1", "2024-12-01 10:15:00"));
        languages.add(createLanguage(3L, "es-MX", "Español", "🇲🇽", "ltr", 
                true, false, 3, 62, 398, "v1.8", "2024-11-28 16:45:00"));
        languages.add(createLanguage(4L, "id-ID", "Indonesia", "🇮🇩", "ltr", 
                false, false, 4, 35, 679, "v1.2", "2024-11-20 09:00:00"));
        
        return languages;
    }

    private Map<String, Object> createLanguage(Long id, String locale, String name, 
            String flagIcon, String textDirection, Boolean isEnabled, Boolean isDefault,
            Integer sortOrder, Integer progress, Integer missing, String version, String updatedAt) {
        Map<String, Object> lang = new HashMap<>();
        lang.put("id", id);
        lang.put("locale", locale);
        lang.put("name", name);
        lang.put("flagIcon", flagIcon);
        lang.put("textDirection", textDirection);
        lang.put("isEnabled", isEnabled);
        lang.put("isDefault", isDefault);
        lang.put("sortOrder", sortOrder);
        lang.put("translationProgress", progress);
        lang.put("missingKeysCount", missing);
        lang.put("version", version);
        lang.put("updatedAt", updatedAt);
        lang.put("createdAt", updatedAt);
        return lang;
    }

    private List<Map<String, Object>> getMockBundles() {
        List<Map<String, Object>> bundles = new ArrayList<>();
        
        bundles.add(createBundle("zh-CN", "中文", "🇨🇳", 1200, 1200, 0, 100, 
                "v2.3", "2024-12-03 14:30:00"));
        bundles.add(createBundle("en-US", "English", "🇺🇸", 1200, 1020, 180, 85, 
                "v2.1", "2024-12-01 10:15:00"));
        bundles.add(createBundle("es-MX", "Español", "🇲🇽", 1200, 744, 456, 62, 
                "v1.8", "2024-11-28 16:45:00"));
        bundles.add(createBundle("id-ID", "Indonesia", "🇮🇩", 1200, 420, 780, 35, 
                "v1.2", "2024-11-20 09:00:00"));
        
        return bundles;
    }

    private Map<String, Object> createBundle(String locale, String name, String flagIcon,
            Integer totalKeys, Integer translatedKeys, Integer missingKeys, Integer progress,
            String version, String updatedAt) {
        Map<String, Object> bundle = new HashMap<>();
        bundle.put("locale", locale);
        bundle.put("languageName", name);
        bundle.put("flagIcon", flagIcon);
        bundle.put("totalKeys", totalKeys);
        bundle.put("translatedKeys", translatedKeys);
        bundle.put("missingKeys", missingKeys);
        bundle.put("translationProgress", progress);
        bundle.put("version", version);
        bundle.put("lastUpdatedAt", updatedAt);
        return bundle;
    }

    private List<Map<String, Object>> getMockVersions() {
        List<Map<String, Object>> versions = new ArrayList<>();
        
        versions.add(createVersion(1L, 2L, "v2.1", "修复登录页翻译", 
                1L, "2024-12-03 14:30:00", true));
        versions.add(createVersion(2L, 2L, "v2.0", "大版本更新", 
                2L, "2024-12-01 10:15:00", false));
        versions.add(createVersion(3L, 2L, "v1.9", "补充仪表板翻译", 
                3L, "2024-11-28 16:45:00", false));
        
        return versions;
    }

    private Map<String, Object> createVersion(Long id, Long languageId, String version,
            String changeSummary, Long uploadedBy, String uploadedAt, Boolean isCurrent) {
        Map<String, Object> ver = new HashMap<>();
        ver.put("id", id);
        ver.put("languageId", languageId);
        ver.put("version", version);
        ver.put("changeSummary", changeSummary);
        ver.put("uploadedBy", uploadedBy);
        ver.put("uploadedAt", uploadedAt);
        ver.put("isCurrent", isCurrent);
        return ver;
    }

    private Map<String, Object> createProgressItem(String locale, String name, String flagIcon,
            Integer totalKeys, Integer translatedKeys, Integer missingKeys, Integer progress) {
        Map<String, Object> item = new HashMap<>();
        item.put("locale", locale);
        item.put("languageName", name);
        item.put("flagIcon", flagIcon);
        item.put("totalKeys", totalKeys);
        item.put("translatedKeys", translatedKeys);
        item.put("missingKeys", missingKeys);
        item.put("translationProgress", progress);
        return item;
    }

    private Map<String, Object> createNamespaceItem(String namespace, Integer progress,
            Integer totalKeys, Integer translatedKeys) {
        Map<String, Object> item = new HashMap<>();
        item.put("namespace", namespace);
        item.put("averageProgress", progress);
        item.put("totalKeys", totalKeys);
        item.put("translatedKeys", translatedKeys);
        return item;
    }

    private Map<String, Object> createMissingItem(String keyPath, String baseValue,
            String namespace, String priority) {
        Map<String, Object> item = new HashMap<>();
        item.put("keyPath", keyPath);
        item.put("baseValue", baseValue);
        item.put("namespace", namespace);
        item.put("priority", priority);
        return item;
    }

    private Map<String, Object> createQualityIssue(String type, String keyPath,
            String baseValue, String translatedValue, String suggestion) {
        Map<String, Object> issue = new HashMap<>();
        issue.put("type", type);
        issue.put("keyPath", keyPath);
        issue.put("baseValue", baseValue);
        issue.put("translatedValue", translatedValue);
        if (suggestion != null) {
            issue.put("suggestion", suggestion);
        }
        return issue;
    }
}

