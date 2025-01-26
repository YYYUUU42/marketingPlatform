package com.yu.market.server.task.model.bo;

import lombok.Data;

/**
 * @author yu
 * @description 任务实体对象
 * @date 2025-01-26
 */
@Data
public class TaskBO {

	/**
	 * 用户 ID
	 */
	private String userId;

	/**
	 * 消息主题
	 */
	private String topic;

	/**
	 * 消息编号
	 */
	private String messageId;

	/**
	 * 消息主体
	 */
	private String message;

}
