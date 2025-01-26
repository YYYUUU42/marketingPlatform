package com.yu.market.server.activity.envent.listener;

import com.yu.market.server.activity.envent.topic.TopicProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author yu
 * @description 用户奖品记录消息消费者
 * @date 2025-01-26
 */
@Slf4j
@Component
public class SendAwardCustomer extends AbstractCustomer {

	@Value("${mq.topic.send_award}")
	private String topic;

	public SendAwardCustomer(RedissonClient redissonClient, TopicProperties topicProperties) {
		super(redissonClient, topicProperties);
	}

	@Override
	protected void process(String message) {
		try {
			log.info("监听用户奖品发送消息 topic: {} message: {}", topic, message);
		} catch (Exception e) {
			log.error("监听用户奖品发送消息，消费失败 topic: {} message: {}", topic, message);
			throw e;
		}
	}

	@PostConstruct
	public void init() {
		log.info("Starting queue listener: {}", topic);
		startListening(topic);
	}
}
