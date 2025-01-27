package com.yu.market.server.activity.envent.listener;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.yu.market.common.event.BaseEvent;
import com.yu.market.common.utils.BeanCopyUtil;
import com.yu.market.server.activity.envent.SendAwardMessageEvent;
import com.yu.market.server.activity.envent.topic.TopicProperties;
import com.yu.market.server.activity.model.bo.DistributeAwardBO;
import com.yu.market.server.activity.service.award.IAwardService;
import com.yu.market.server.activity.service.award.impl.AwardService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
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

	@Resource
	private IAwardService awardService;

	public SendAwardCustomer(RedissonClient redissonClient, TopicProperties topicProperties) {
		super(redissonClient, topicProperties);
	}

	@Override
	protected void process(String message) {
		try {
			log.info("监听用户奖品发送消息 topic: {} message: {}", topic, message);
			BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage> eventMessage = JSONUtil.toBean(message, new TypeReference<BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage>>() {
			}.getType(), true);
			SendAwardMessageEvent.SendAwardMessage sendAwardMessage = eventMessage.getData();

			// 发放奖品
			DistributeAwardBO distributeAwardBO = BeanCopyUtil.copyProperties(sendAwardMessage, DistributeAwardBO.class);
			awardService.distributeAward(distributeAwardBO);
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
