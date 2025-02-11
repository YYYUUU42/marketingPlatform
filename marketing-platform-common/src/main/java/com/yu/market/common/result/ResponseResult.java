package com.yu.market.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yu.market.common.exception.errorCode.IErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yu
 * @description 统一返回类
 * @date 2024-12-19
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseResult<T> implements Serializable {

	/**
	 * 返回状态码
	 */
	private String code;

	/**
	 * 返回消息
	 */
	private String info;

	/**
	 * 数据
	 */
	private T data;

	/**
	 * 正确返回码
	 */
	private static final String SUCCESS_CODE = "200";

	/**
	 * 正确返回信息
	 */
	private static final String SUCCESS_MEG = "success";

	/**
	 * 私有构造方法，强制使用静态工厂方法
	 */
	private ResponseResult(String code, String info, T data) {
		this.code = code;
		this.info = info;
		this.data = data;
	}

	private ResponseResult(IErrorCode errorCode, T data) {
		this.code = errorCode.code();
		this.info = errorCode.message();
		this.data = data;
	}

	// 成功结果
	public static <T> ResponseResult<T> success() {
		return new ResponseResult<>(SUCCESS_CODE, SUCCESS_MEG, null);
	}

	public static <T> ResponseResult<T> success(T data) {
		return new ResponseResult<>(SUCCESS_CODE, SUCCESS_MEG, data);
	}

	public static <T> ResponseResult<T> success(String info, T data) {
		return new ResponseResult<>(SUCCESS_CODE, info, data);
	}

	// 错误结果
	public static <T> ResponseResult<T> error(IErrorCode errorCode) {
		return new ResponseResult<>(errorCode, null);
	}

	public static <T> ResponseResult<T> error(IErrorCode errorCode, String customMessage) {
		return new ResponseResult<>(errorCode.code(), customMessage, null);
	}

	public static <T> ResponseResult<T> error(String code, String info) {
		return new ResponseResult<>(code, info, null);
	}
}
