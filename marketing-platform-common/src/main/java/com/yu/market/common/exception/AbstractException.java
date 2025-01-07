package com.yu.market.common.exception;

import com.yu.market.common.exception.errorCode.IErrorCode;
import lombok.Getter;
import org.springframework.util.StringUtils;

/**
 * @author yu
 * @description 抽象项目中三类异常体系，客户端异常、服务端异常以及远程服务调用异常
 * @date 2024-12-19
 */
@Getter
public abstract class AbstractException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;

    public AbstractException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable);
        this.errorCode = errorCode.code();
        this.errorMessage = resolveErrorMessage(message, errorCode);
    }

    /**
     * 如果 message 不为空，则使用 message，否则使用 errorCode 提供的默认消息
     */
    private static String resolveErrorMessage(String message, IErrorCode errorCode) {
        return StringUtils.hasLength(message) ? message : errorCode.message();
    }
}
