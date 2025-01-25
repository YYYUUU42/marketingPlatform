package com.yu.market.server.activity.envent.listener;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.yu.market.common.event.BaseEvent;
import com.yu.market.server.activity.service.IRaffleActivitySkuStockService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yu
 * @description 活动sku库存耗尽
 * @date 2025-01-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivitySkuStockZeroCustomer {

	private final RedissonClient redissonClient;
	private final IRaffleActivitySkuStockService skuStock;

	@Value("${mq.topic.activity_sku_stock_zero}")
	private String topic;

	/**
	 * 单线程线程池
	 */
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

	/**
	 * 监听带消息后，执行业务功能
	 */
	private void process(String message) {
		log.info("监听活动sku库存消耗为0消息 topic: {} message: {}", topic, message);
		// 转换对象
		BaseEvent.EventMessage<Long> eventMessage = JSONUtil.toBean(message, new TypeReference<BaseEvent.EventMessage<Long>>() {
		}.getType(), true);
		Long sku = eventMessage.getData();

		// 更新库存
		skuStock.clearActivitySkuStock(sku);

		// 清空队列 - 此时就不需要延迟更新数据库记录了
		skuStock.clearQueueValue();
	}

	@PostConstruct
	public void init() {
		log.info("Starting queue listener...");
		startListening(topic);
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
}
