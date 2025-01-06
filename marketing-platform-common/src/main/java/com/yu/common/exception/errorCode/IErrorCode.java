package com.yu.common.exception.errorCode;

/**
 * @author yu
 * @description 平台错误码｜定义错误码抽象接口，由各错误码类实现接口方法
 * @date 2024-12-19
 */
public interface IErrorCode {

    /**
     * 错误码
     */
    String code();

    /**
     * 错误信息
     */
    String message();
}
