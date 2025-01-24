package com.yu.market.common.event;

import cn.hutool.json.JSONUtil;
import com.yu.market.common.redis.IRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RQueue;
import org.springframework.stereotype.Component;

/**
 * @author yu
 * @description 消息发送
 * @date 2025-01-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

	private final IRedisService redisService;

	public void publish(String topic, BaseEvent.EventMessage<?> eventMessage) {
		try {
			String messageJson = JSONUtil.toJsonStr(eventMessage);
			RQueue<String> queue = redisService.getQueue(topic);
			queue.add(messageJson);

			log.info("发送MQ消息 topic:{} message:{}", topic, messageJson);
		} catch (Exception e) {
			log.error("发送MQ消息失败 topic:{} message:{}", topic, JSONUtil.toJsonStr(eventMessage), e);
			throw e;
		}
	}
}
