package com.yu.market.server.activity.envent.task;

import com.yu.market.common.event.BaseEvent;
import com.yu.market.server.activity.envent.SendAwardMessageEvent;
import com.yu.market.server.task.model.enums.TaskStateEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yu
 * @description 任务实体对象
 * @date 2025-01-26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AwardTaskBO {

	/**
	 * 活动ID
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
	private BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage> message;

	/**
	 * 任务状态；create-创建、completed-完成、fail-失败
	 */
	private TaskStateEnum state;

}