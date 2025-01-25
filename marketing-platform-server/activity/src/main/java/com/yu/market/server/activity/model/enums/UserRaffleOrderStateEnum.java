package com.yu.market.server.activity.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRaffleOrderStateEnum {

	create("create", "创建"),
	used("used", "已使用"),
	cancel("cancel", "已作废"),
	;

	private final String code;
	private final String desc;

}