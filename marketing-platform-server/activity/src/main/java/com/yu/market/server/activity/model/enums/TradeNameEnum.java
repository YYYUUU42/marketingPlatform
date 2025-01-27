package com.yu.market.server.activity.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yu
 * @description 交易名称枚举值
 * @date 2025-01-27
 */
@Getter
@AllArgsConstructor
public enum TradeNameEnum {

	REBATE("行为返利"),
	CONVERT_SKU("兑换抽奖"),

	;

	private final String name;

}
