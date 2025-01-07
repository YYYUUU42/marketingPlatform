package com.yu.market.common.exception;


import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.common.exception.errorCode.IErrorCode;

import java.util.Optional;

/**
 * @author yu
 * @description 服务端运行异常｜请求运行过程中出现的不符合业务预期的异常
 * @date 2024-12-19
 */
public class ServiceException extends AbstractException {

    public ServiceException(String message) {
        this(message, null, BaseErrorCode.SERVICE_ERROR);
    }

    public ServiceException(IErrorCode errorCode) {
        this(null, errorCode);
    }

    public ServiceException(String message, IErrorCode errorCode) {
        this(message, null, errorCode);
    }

    public ServiceException(String message, Throwable throwable, IErrorCode errorCode) {
        super(Optional.ofNullable(message).orElse(errorCode.message()), throwable, errorCode);
    }

    @Override
    public String toString() {
        return String.format("ServiceException{code='%s', message='%s'}", getErrorCode(), getErrorMessage());
    }
}
