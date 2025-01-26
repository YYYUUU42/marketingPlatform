package com.yu.market.server.activity.envent.listener;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.yu.market.common.event.BaseEvent;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.server.activity.envent.SendRebateMessageEvent;
import com.yu.market.server.activity.envent.topic.TopicProperties;
import com.yu.market.server.activity.model.bo.SkuRechargeBO;
import com.yu.market.server.activity.model.enums.RebateTypeEnum;
import com.yu.market.server.activity.service.IRaffleActivityAccountQuotaService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author yu
 * @description 监听返利消息
 * @date 2025-01-26
 */
@Slf4j
@Component
public class RebateMessageCustomer extends AbstractCustomer {

	@Value("${mq.topic.send_rebate}")
	private String topic;
	@Resource
	private IRaffleActivityAccountQuotaService raffleActivityAccountQuotaService;

	public RebateMessageCustomer(RedissonClient redissonClient, TopicProperties topicProperties) {
		super(redissonClient, topicProperties);
	}

	@Override
	protected void process(String message) {
		try {
			log.info("监听用户行为返利消息 topic: {} message: {}", topic, message);
			// 转换消息
			BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage> eventMessage = JSONUtil.toBean(message, new TypeReference<BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage>>() {
			}.getType(), true);
			SendRebateMessageEvent.RebateMessage rebateMessage = eventMessage.getData();

			if (!rebateMessage.getRebateType().equals(RebateTypeEnum.SKU.getCode())) {
				log.info("监听用户行为返利消息 - 非sku奖励暂时不处理 topic: {} message: {}", topic, message);
				return;
			}

			// 入账奖励
			SkuRechargeBO skuRechargeEntity = new SkuRechargeBO();
			skuRechargeEntity.setUserId(rebateMessage.getUserId());
			skuRechargeEntity.setSku(Long.valueOf(rebateMessage.getRebateConfig()));
			skuRechargeEntity.setOutBusinessNo(rebateMessage.getBizId());
			raffleActivityAccountQuotaService.createOrder(skuRechargeEntity);
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
