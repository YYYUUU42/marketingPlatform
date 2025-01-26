package com.yu.market.server.activity.envent.listener;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.yu.market.common.event.BaseEvent;
import com.yu.market.server.activity.envent.topic.TopicProperties;
import com.yu.market.server.activity.service.IRaffleActivitySkuStockService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author yu
 * @description 活动sku库存耗尽
 * @date 2025-01-24
 */
@Slf4j
@Component
public class ActivitySkuStockZeroCustomer extends AbstractCustomer {

	@Resource
	private IRaffleActivitySkuStockService skuStock;

	@Value("${mq.topic.activity_sku_stock_zero}")
	private String topic;


	public ActivitySkuStockZeroCustomer(RedissonClient redissonClient, TopicProperties topicProperties) {
		super(redissonClient, topicProperties);
	}

	/**
	 * 监听带消息后，执行业务功能
	 */
	@Override
	protected void process(String message) {
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
		log.info("Starting queue listener: {}", topic);
		startListening(topic);
	}
}
