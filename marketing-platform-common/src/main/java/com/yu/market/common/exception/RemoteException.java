package com.yu.market.common.exception;


import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.common.exception.errorCode.IErrorCode;

/**
 * @author yu
 * @description 远程服务调用异常｜比如订单调用支付失败，向上抛出的异常应该是远程服务调用异常
 * @date 2024-12-19
 */
public class RemoteException extends AbstractException {

    public RemoteException(String message) {
        this(message, null, BaseErrorCode.REMOTE_ERROR);
    }

    public RemoteException(String message, IErrorCode errorCode) {
        this(message, null, errorCode);
    }

    public RemoteException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable, errorCode);
    }

    @Override
    public String toString() {
        return String.format("RemoteException{code='%s', message='%s'}", getErrorCode(), getErrorMessage());
    }
}
