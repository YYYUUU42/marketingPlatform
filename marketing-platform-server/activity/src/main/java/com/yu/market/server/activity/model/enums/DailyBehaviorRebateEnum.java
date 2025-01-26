package com.yu.market.server.activity.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DailyBehaviorRebateEnum {
	open("open","开启"),
	clone("clone","关闭");

	private final String code;
	private final String desc;
}
