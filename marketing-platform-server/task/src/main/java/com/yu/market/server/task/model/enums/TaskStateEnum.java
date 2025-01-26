package com.yu.market.server.task.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yu
 * @description 任务状态枚举
 * @date 2025-01-26
 */
@Getter
@AllArgsConstructor
public enum TaskStateEnum {
	create("create", "任务创建"),
	fail("fail", "任务失败"),
	completed("completed", "任务完成");

	private final String code;
	private final String message;
}
