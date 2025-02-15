package com.yu.market.server.coupon.service.admin.chain;

import org.springframework.core.Ordered;

/**
 * @author yu
 * @description 抽象商家后管业务责任链组件
 * @date 2025-02-14
 */
public abstract class MerchantAdminAbstractChainHandler<T> implements Ordered {
	/**
	 * 获取标识
	 * @return 标识字符串
	 */
	public abstract String mark();

	/**
	 * 处理请求参数
	 * @param requestParam 请求参数
	 */
	public abstract void handle(T requestParam);
}