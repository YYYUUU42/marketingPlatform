package com.yu.market.server.activity.envent.listener;

import com.yu.market.server.activity.envent.topic.TopicProperties;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author yu
 * @description 消息监听抽象类
 * @date 2025-01-26
 */
@Slf4j
@RequiredArgsConstructor
@Component
public abstract class AbstractCustomer {

	private final RedissonClient redissonClient;

	/**
	 * 固定程线程池
	 */
	private final ExecutorService executorService ;

	public AbstractCustomer(RedissonClient redissonClient, TopicProperties topicProperties) {
		this.redissonClient = redissonClient;
		int topicCount = topicProperties.getTopicCount();
		log.info("Initializing thread pool with size: {}", topicCount);

		// 避免线程池大小为 0
		this.executorService = Executors.newFixedThreadPool(topicCount > 0 ? topicCount : 1);
	}

	public <T> void startListening(String topic) {
		RBlockingQueue<T> queue = redissonClient.getBlockingQueue(topic);

		// 提交一个任务到线程池中，监听队列
		executorService.submit(() -> {
			while (true) {
				try {
					// 阻塞直到有消息可用
					String message = (String) queue.take();
					log.info("Received message: {}", message);

					process(message);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					log.warn("Listener thread interrupted, shutting down.");
					break;
				} catch (Exception e) {
					// 捕获其他意外异常，防止线程退出
					log.warn("Error while processing message: {}", e.getMessage());
				}
			}
		});
	}

	protected abstract void process(String message);

	@PreDestroy
	public void shutdown() {
		log.info("Shutting down queue listener...");
		executorService.shutdownNow();
	}
}
