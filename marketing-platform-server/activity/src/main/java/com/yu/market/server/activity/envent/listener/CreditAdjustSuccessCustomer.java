package com.yu.market.server.activity.envent.listener;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.yu.market.common.event.BaseEvent;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.server.activity.envent.CreditAdjustSuccessMessageEvent;
import com.yu.market.server.activity.envent.topic.TopicProperties;
import com.yu.market.server.activity.model.bo.DeliveryOrderBO;
import com.yu.market.server.activity.service.IRaffleActivityAccountQuotaService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author yu
 * @description 积分调整成功消息
 * @date 2025-02-10
 */
@Slf4j
@Component
public class CreditAdjustSuccessCustomer extends AbstractCustomer {

	@Value("${mq.topic.credit_adjust_success}")
	private String topic;

	@Resource
	private IRaffleActivityAccountQuotaService raffleActivityAccountQuotaService;

	public CreditAdjustSuccessCustomer(RedissonClient redissonClient, TopicProperties topicProperties) {
		super(redissonClient, topicProperties);
	}

	@Override
	protected void process(String message) {
		try {
			log.info("监听积分账户调整成功消息，进行交易商品发货 topic: {} message: {}", topic, message);
			// 转换消息
			BaseEvent.EventMessage<CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage> eventMessage = JSONUtil.toBean(message, new TypeReference<BaseEvent.EventMessage<CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage>>() {
			}.getType(), true);
			CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage creditAdjustSuccessMessage = eventMessage.getData();

			DeliveryOrderBO deliveryOrderBO = DeliveryOrderBO.builder()
					.userId(creditAdjustSuccessMessage.getUserId())
					.outBusinessNo(creditAdjustSuccessMessage.getOutBusinessNo())
					.build();

			raffleActivityAccountQuotaService.updateOrder(deliveryOrderBO);
		} catch (ServiceException e) {
			if (BaseErrorCode.INDEX_DUP.code().equals(e.getErrorCode())) {
				log.warn("监听用户行为返利消息，消费重复 topic: {} message: {}", topic, message, e);
				return;
			}
			throw e;
		} catch (Exception e) {
			log.error("监听用户行为返利消息，消费失败 topic: {} message: {}", topic, message, e);
			throw e;
		}
	}
}
