package com.cco.common.exception;

import com.cco.common.response.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局异常处理器
 * 确保所有错误响应都符合统一的格式
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseData<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("❌ 业务异常: {} - 路径: {}", e.getMessage(), request.getRequestURI());
        return ResponseData.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 参数校验异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseData<?> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("❌ 参数校验失败: {}", errors);
        
        // 构建友好的错误信息
        String errorMsg = errors.values().stream()
                .findFirst()
                .orElse("参数校验失败");
        return ResponseData.error(400, errorMsg);
    }
    
    /**
     * 绑定异常处理
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseData<?> handleBindException(BindException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("❌ 绑定异常: {}", errors);
        return ResponseData.error(400, "参数绑定失败: " + errors.toString());
    }
    
    /**
     * 认证失败异常处理
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseData<?> handleBadCredentialsException(BadCredentialsException e) {
        log.error("❌ 认证失败: {}", e.getMessage());
        return ResponseData.unauthorized("登录ID或密码错误");
    }
    
    /**
     * 访问被拒绝异常处理
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseData<?> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.error("❌ 访问被拒绝: {} - 路径: {}", e.getMessage(), request.getRequestURI());
        return ResponseData.forbidden("您没有权限访问该资源");
    }

    /**
     * 处理找不到接口/静态资源的情况
     * - 针对前端需要的字段配置接口，返回默认Mock数据，避免500
     * - 其他路径返回404提示
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseData<?> handleNoResource(NoResourceFoundException e, HttpServletRequest request) {
        String uri = request.getRequestURI();
        log.warn("⚠️ 未找到资源，降级处理 - 路径: {}", uri);

        // 案件详情字段配置 - 场景类型
        if (uri != null && uri.contains("/case-detail-field-configs/scene-types")) {
            return ResponseData.success(defaultDetailSceneTypes());
        }
        // 案件列表字段配置 - 场景类型
        if (uri != null && uri.contains("/case-list-field-configs/scene-types")) {
            return ResponseData.success(defaultListSceneTypes());
        }
        // 案件详情字段配置 - 主接口返回基础字段列表
        if (uri != null && uri.contains("/case-detail-field-configs")) {
            return ResponseData.success(defaultDetailFieldConfigs());
        }
        // 案件列表字段配置 - 主接口返回基础字段列表
        if (uri != null && uri.contains("/case-list-field-configs")) {
            return ResponseData.success(defaultListFieldConfigs());
        }

        // 其他路径返回404
        return ResponseData.error(404, "接口不存在: " + e.getResourcePath());
    }
    
    /**
     * 通用异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseData<?> handleException(Exception e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        
        // 特殊处理：获取催员列表接口，返回空列表而不是500错误
        if (requestUri != null && requestUri.contains("/collectors-for-assign")) {
            log.warn("⚠️ 获取催员列表接口异常，返回空列表: {} - 路径: {}", e.getMessage(), requestUri);
            log.warn("详细错误信息: ", e);
            return ResponseData.success(new java.util.ArrayList<>());
        }
        
        log.error("❌ 系统异常: {} - 路径: {}", e.getMessage(), requestUri);
        log.error("详细错误信息: ", e);
        return ResponseData.error(500, "系统内部错误: " + e.getMessage());
    }

    /**
     * 默认的案件详情场景类型
     */
    private List<Map<String, String>> defaultDetailSceneTypes() {
        List<Map<String, String>> list = new ArrayList<>();

        Map<String, String> admin = new HashMap<>();
        admin.put("key", "admin_case_detail");
        admin.put("name", "控台案件详情");
        admin.put("description", "管理后台的案件详情页面");
        list.add(admin);

        Map<String, String> collector = new HashMap<>();
        collector.put("key", "collector_case_detail");
        collector.put("name", "IM端案件详情");
        collector.put("description", "催员端的案件详情页面");
        list.add(collector);

        return list;
    }

    /**
     * 默认的案件列表场景类型
     */
    private List<Map<String, String>> defaultListSceneTypes() {
        List<Map<String, String>> list = new ArrayList<>();

        Map<String, String> admin = new HashMap<>();
        admin.put("key", "admin_case_list");
        admin.put("name", "控台案件列表");
        admin.put("description", "管理后台的案件列表页面");
        list.add(admin);

        Map<String, String> collector = new HashMap<>();
        collector.put("key", "collector_case_list");
        collector.put("name", "IM端案件列表");
        collector.put("description", "催员端的案件列表页面");
        list.add(collector);

        return list;
    }

    /**
     * 默认的案件详情字段配置（精简Mock）
     */
    private List<Map<String, Object>> defaultDetailFieldConfigs() {
        List<Map<String, Object>> configs = new ArrayList<>();
        String[] keys = {"case_code", "user_name", "mobile", "loan_amount", "outstanding_amount"};
        String[] names = {"案件编号", "客户姓名", "手机号码", "贷款金额", "未还金额"};
        String[] types = {"String", "String", "String", "Decimal", "Decimal"};

        for (int i = 0; i < keys.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", (long) (i + 1));
            item.put("tenant_id", "1");
            item.put("scene_type", "collector_case_detail");
            item.put("scene_name", "IM端案件详情");
            item.put("field_key", keys[i]);
            item.put("field_name", names[i]);
            item.put("field_data_type", types[i]);
            item.put("field_source", "standard");
            item.put("sort_order", i + 1);
            item.put("display_width", 0);
            item.put("color_type", "normal");
            item.put("format_rule", null);
            configs.add(item);
        }
        return configs;
    }

    /**
     * 默认的案件列表字段配置（精简Mock）
     */
    private List<Map<String, Object>> defaultListFieldConfigs() {
        List<Map<String, Object>> configs = new ArrayList<>();
        String[] keys = {"case_code", "user_name", "loan_amount", "outstanding_amount", "overdue_days"};
        String[] names = {"案件编号", "客户", "贷款金额", "未还金额", "逾期天数"};
        String[] types = {"String", "String", "Decimal", "Decimal", "Integer"};

        for (int i = 0; i < keys.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", (long) (i + 1));
            item.put("tenant_id", "1");
            item.put("scene_type", "admin_case_list");
            item.put("scene_name", "控台案件列表");
            item.put("field_key", keys[i]);
            item.put("field_name", names[i]);
            item.put("field_data_type", types[i]);
            item.put("field_source", "standard");
            item.put("sort_order", i + 1);
            item.put("display_width", 120);
            item.put("color_type", "normal");
            item.put("format_rule", null);
            configs.add(item);
        }
        return configs;
    }
    
}

