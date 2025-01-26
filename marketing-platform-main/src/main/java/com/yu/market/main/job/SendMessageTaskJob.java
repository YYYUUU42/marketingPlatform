package com.yu.market.main.job;

import com.yu.market.server.task.model.bo.TaskBO;
import com.yu.market.server.task.service.ITaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yu
 * @description 发送MQ消息任务队列
 * @date 2025-01-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SendMessageTaskJob {

	private final ITaskService taskService;

	@Scheduled(cron = "0/5 * * * * ?")
	public void exec() {
		int availableProcessors = Runtime.getRuntime().availableProcessors();

		ThreadPoolExecutor executor = new ThreadPoolExecutor(
				availableProcessors, // corePoolSize
				availableProcessors * 2, // maximumPoolSize
				60, // keepAliveTime
				TimeUnit.SECONDS, // unit
				new LinkedBlockingQueue<>(100), // workQueue
				Executors.defaultThreadFactory(), // threadFactory
				new ThreadPoolExecutor.AbortPolicy() // handler
		);


		executor.execute(() -> {

			List<TaskBO> taskBOS = taskService.queryNoSendMessageTaskList();
			if (taskBOS.isEmpty()) return;
			// 发送MQ消息
			for (TaskBO taskBO : taskBOS) {
				// 开启线程发送，提高发送效率
				executor.execute(() -> {
					try {
						taskService.sendMessage(taskBO);
						taskService.updateTaskSendMessageCompleted(taskBO.getUserId(), taskBO.getMessageId());
					} catch (Exception e) {
						log.error("定时任务，发送MQ消息失败 userId: {} topic: {}", taskBO.getUserId(), taskBO.getTopic());
						taskService.updateTaskSendMessageFail(taskBO.getUserId(), taskBO.getMessageId());
					}
				});
			}

		});
	}
}
