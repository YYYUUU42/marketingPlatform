package com.yu.market.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yu.market.common.event.EventPublisher;
import com.yu.market.common.utils.BeanCopyUtil;
import com.yu.market.infrastructure.pojo.*;
import com.yu.market.infrastructure.mapper.*;
import com.yu.market.server.task.model.bo.TaskBO;
import com.yu.market.server.task.model.enums.TaskStateEnum;
import com.yu.market.server.task.repository.ITaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yu
 * @description 任务服务仓储实现
 * @date 2025-01-26
 */
@Repository
@RequiredArgsConstructor
public class TaskRepository implements ITaskRepository {

	private final TaskMapper taskMapper;
	private final EventPublisher eventPublisher;

	/**
	 * 查询发送MQ失败和超时1分钟未发送的MQ
	 */
	@Override
	public List<TaskBO> queryNoSendMessageTaskList() {
		/*
		 * SELECT *
		 * FROM task
		 * WHERE (state = 'fail' OR (state = 'create' AND update_time < NOW() - INTERVAL 1 MINUTE))
		 * LIMIT 10;
		 */
		List<Task> taskList = taskMapper.selectList(new LambdaQueryWrapper<Task>()
				.nested(wrapper -> wrapper.eq(Task::getState, TaskStateEnum.fail.getCode())
						.or(w -> w.eq(Task::getState, TaskStateEnum.create.getCode())
								.lt(Task::getUpdateTime, LocalDateTime.now().minus(Duration.ofMinutes(1))))
				)
				.last("limit 10"));
		if (taskList == null) {
			return List.of();
		}

		return BeanCopyUtil.copyListProperties(taskList, TaskBO.class);
	}

	/**
	 * 发送消息
	 */
	@Override
	public void sendMessage(TaskBO taskBO) {
		eventPublisher.publish(taskBO.getTopic(), taskBO.getMessage());
	}

	/**
	 * 更新 - 消息发送成功
	 */
	@Override
	public void updateTaskSendMessageCompleted(String userId, String messageId) {
		Task task = new Task();
		task.setUserId(userId);
		task.setMessageId(messageId);
		taskMapper.updateTaskSendMessageCompleted(userId, messageId);
	}

	/**
	 * 更新 - 消息发送失败
	 */
	@Override
	public void updateTaskSendMessageFail(String userId, String messageId) {
		taskMapper.updateTaskSendMessageFail(userId, messageId);
	}
}
