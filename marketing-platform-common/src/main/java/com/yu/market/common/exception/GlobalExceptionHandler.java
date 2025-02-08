package com.yu.market.common.exception;

import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.common.result.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

/**
 * @author yu
 * @description 全局异常处理器｜拦截指定异常并通过优雅构建方式返回前端信息
 * @date 2024-12-19
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseResult<?> handleValidationException(HttpServletRequest request, MethodArgumentNotValidException ex) {
        // 获取第一个字段错误信息
        BindingResult bindingResult = ex.getBindingResult();
        String errorMessage = Optional.ofNullable(bindingResult.getFieldError())
                .map(FieldError::getDefaultMessage)
                .orElse("Invalid request parameters");

        // 打印错误日志
        logError(request, ex, errorMessage);

        // 返回统一错误结果
        return ResponseResult.error(BaseErrorCode.CLIENT_ERROR, errorMessage);
    }

    /**
     * 处理自定义异常
     */
    @ExceptionHandler(AbstractException.class)
    public ResponseResult<?> handleCustomException(HttpServletRequest request, AbstractException ex) {
        // 打印错误日志
        logError(request, ex, ex.getErrorMessage());

        // 返回统一错误结果
        return ResponseResult.error(ex.getErrorCode(), ex.getErrorMessage());
    }

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Throwable.class)
    public ResponseResult<?> handleGlobalException(HttpServletRequest request, Throwable throwable) {
        // 打印错误日志
        logError(request, throwable, "Internal server error");

        // 返回统一错误结果
        return ResponseResult.error(BaseErrorCode.SERVICE_ERROR, "Internal server error");
    }

    /**
     * 提取日志打印的公共逻辑
     * @param request 请求对象
     * @param ex 异常对象
     * @param errorMessage 错误信息
     */
    private void logError(HttpServletRequest request, Throwable ex, String errorMessage) {
        String method = request.getMethod();
        String url = getRequestUrl(request);
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String sessionId = request.getSession().getId();

        // 打印堆栈信息（输出堆栈的全部信息或者根据需求输出更多行）
        StringBuilder stackTraceBuilder = new StringBuilder();
        stackTraceBuilder.append(ex.getClass().getName()).append(": ").append(errorMessage).append("\n");
        StackTraceElement[] stackTrace = ex.getStackTrace();
        for (int i = 0; i < Math.min(10, stackTrace.length); i++) {  // 控制显示堆栈的最大行数
            stackTraceBuilder.append("\tat ").append(stackTrace[i]).append("\n");
        }

        // 打印详细日志，包含请求方法、URL、客户端信息、堆栈等
        log.error("[{}] {} [Exception] {}\nClient IP: {}\nUser-Agent: {}\nSession ID: {}\n{}",
                method, url, errorMessage, clientIp, userAgent, sessionId, stackTraceBuilder);
    }

    /**
     * 获取完整的请求 URL，包括查询参数
     * @param request 请求对象
     * @return 完整的 URL
     */
    private String getRequestUrl(HttpServletRequest request) {
        String queryString = request.getQueryString();
        return queryString == null ? request.getRequestURL().toString() : request.getRequestURL().toString() + "?" + queryString;
    }

    /**
     * 获取客户端 IP 地址
     * @param request 请求对象
     * @return 客户端 IP 地址
     */
    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }
}
