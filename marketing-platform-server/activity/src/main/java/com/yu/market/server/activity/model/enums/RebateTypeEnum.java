package com.yu.market.server.activity.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yu
 * @description 利类型（sku 活动库存充值商品、integral 用户活动积分）
 * @date 2025-01-26
 */
@Getter
@AllArgsConstructor
public enum RebateTypeEnum {

	SKU("sku", "活动库存充值商品"),
	INTEGRAL("integral", "用户活动积分"),
	;

	private final String code;
	private final String info;
}
