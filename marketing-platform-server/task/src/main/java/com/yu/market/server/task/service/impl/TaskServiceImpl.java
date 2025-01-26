package com.yu.market.server.task.service.impl;

import com.yu.market.server.task.model.bo.TaskBO;
import com.yu.market.server.task.repository.TaskRepository;
import com.yu.market.server.task.service.ITaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements ITaskService {

	private final TaskRepository taskRepository;

	/**
	 * 查询发送MQ失败和超时1分钟未发送的MQ
	 */
	@Override
	public List<TaskBO> queryNoSendMessageTaskList() {
		return taskRepository.queryNoSendMessageTaskList();
	}

	/**
	 * 发送消息
	 */
	@Override
	public void sendMessage(TaskBO taskBO) {
		taskRepository.sendMessage(taskBO);

	}

	/**
	 * 更新 - 消息发送成功
	 */
	@Override
	public void updateTaskSendMessageCompleted(String userId, String messageId) {
		taskRepository.updateTaskSendMessageCompleted(userId, messageId);
	}

	/**
	 * 更新 - 消息发送失败
	 */
	@Override
	public void updateTaskSendMessageFail(String userId, String messageId) {
		taskRepository.updateTaskSendMessageFail(userId, messageId);
	}
}
