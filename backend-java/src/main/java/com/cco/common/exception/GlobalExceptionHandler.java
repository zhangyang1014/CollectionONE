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

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
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
        return ResponseData.error(400, "参数校验失败: " + errors.toString());
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
    
}

