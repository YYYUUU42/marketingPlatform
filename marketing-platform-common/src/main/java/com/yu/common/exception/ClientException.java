package com.yu.common.exception;


import com.yu.common.exception.errorCode.BaseErrorCode;
import com.yu.common.exception.errorCode.IErrorCode;

/**
 * @author yu
 * @description 客户端异常｜用户发起调用请求后因客户端提交参数或其他客户端问题导致的异常
 * @date 2024-12-19
 */
public class ClientException extends AbstractException {

    public ClientException(IErrorCode errorCode) {
        this(null, null, errorCode);
    }

    public ClientException(String message) {
        this(message, null, BaseErrorCode.CLIENT_ERROR);
    }

    public ClientException(String message, IErrorCode errorCode) {
        this(message, null, errorCode);
    }

    public ClientException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable, errorCode);
    }

    @Override
    public String toString() {
        return String.format("ClientException{code='%s', message='%s'}", getErrorCode(), getErrorMessage());
    }
}
