package com.yu.market.server.task.service;

import com.yu.market.server.task.model.bo.TaskBO;

import java.util.List;

public interface ITaskService {

	/**
	 * 查询发送MQ失败和超时1分钟未发送的MQ
	 */
	List<TaskBO> queryNoSendMessageTaskList();

	/**
	 * 发送消息
	 */
	void sendMessage(TaskBO taskBO);

	/**
	 * 更新 - 消息发送成功
	 */
	void updateTaskSendMessageCompleted(String userId, String messageId);

	/**
	 * 更新 - 消息发送失败
	 */
	void updateTaskSendMessageFail(String userId, String messageId);
}
