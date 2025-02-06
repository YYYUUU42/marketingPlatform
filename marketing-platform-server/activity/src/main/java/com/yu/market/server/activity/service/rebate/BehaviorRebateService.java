package com.yu.market.server.activity.service.rebate;

import cn.hutool.core.collection.CollectionUtil;
import com.yu.market.common.contants.Constants;
import com.yu.market.common.event.BaseEvent;
import com.yu.market.common.utils.BeanCopyUtil;
import com.yu.market.common.utils.SnowFlakeUtil;
import com.yu.market.server.activity.envent.SendRebateMessageEvent;
import com.yu.market.server.activity.envent.task.RebateTaskBO;
import com.yu.market.server.activity.model.aggregate.BehaviorRebateAggregate;
import com.yu.market.server.activity.model.bo.BehaviorBO;
import com.yu.market.server.activity.model.bo.BehaviorRebateOrderBO;
import com.yu.market.server.activity.model.bo.DailyBehaviorRebateBO;
import com.yu.market.server.activity.respository.IBehaviorRebateRepository;
import com.yu.market.server.task.model.enums.TaskStateEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yu
 * @description 行为返利服务实现
 * @date 2025-01-26
 */
@Service
@RequiredArgsConstructor
public class BehaviorRebateService implements IBehaviorRebateService{

	private final IBehaviorRebateRepository behaviorRebateRepository;
	private final SendRebateMessageEvent sendRebateMessageEvent;

	/**
	 * 创建行为动作的入账订单
	 *
	 * @param behaviorBO 行为实体对象
	 * @return 订单ID
	 */
	@Override
	public List<String> createOrder(BehaviorBO behaviorBO) {
		// 查询返利配置
		List<DailyBehaviorRebateBO> dailyBehaviorRebateBOS = behaviorRebateRepository.queryDailyBehaviorRebateConfig(behaviorBO.getBehaviorTypeEnum());
		if (CollectionUtil.isEmpty(dailyBehaviorRebateBOS)){
			return List.of();
		}

		// 构建聚合对象
		List<String> orderIds = new ArrayList<>();
		List<BehaviorRebateAggregate> behaviorRebateAggregates = new ArrayList<>();
		for (DailyBehaviorRebateBO dailyBehaviorRebateBO : dailyBehaviorRebateBOS) {
			// 拼装业务ID；用户ID_返利类型_外部透彻业务ID
			String bizId = behaviorBO.getUserId() + Constants.UNDERLINE + dailyBehaviorRebateBO.getBehaviorType() + Constants.UNDERLINE + behaviorBO.getOutBusinessNo();

			BehaviorRebateOrderBO behaviorRebateOrderBO = BeanCopyUtil.copyProperties(dailyBehaviorRebateBO, BehaviorRebateOrderBO.class);
			behaviorRebateOrderBO.setUserId(behaviorBO.getUserId());
			behaviorRebateOrderBO.setOrderId(String.valueOf(new SnowFlakeUtil().nextId()));
			behaviorRebateOrderBO.setBizId(bizId);
			orderIds.add(behaviorRebateOrderBO.getOrderId());

			// Mq 消息对象
			SendRebateMessageEvent.RebateMessage rebateMessage = SendRebateMessageEvent.RebateMessage.builder()
					.userId(behaviorBO.getUserId())
					.rebateType(dailyBehaviorRebateBO.getBehaviorType())
					.rebateConfig(dailyBehaviorRebateBO.getRebateConfig())
					.bizId(bizId)
					.build();

			// 构建事件消息
			BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage> rebateMessageEventMessage = sendRebateMessageEvent.buildEventMessage(rebateMessage);

			// 构建任务对象
			RebateTaskBO rebateTaskBO = RebateTaskBO.builder()
					.userId(behaviorBO.getUserId())
					.topic(sendRebateMessageEvent.topic())
					.messageId(rebateMessageEventMessage.getId())
					.message(rebateMessageEventMessage)
					.state(TaskStateEnum.create)
					.build();

			BehaviorRebateAggregate behaviorRebateAggregate = BehaviorRebateAggregate.builder()
					.userId(behaviorBO.getUserId())
					.behaviorRebateOrderBO(behaviorRebateOrderBO)
					.rebateTaskBO(rebateTaskBO)
					.build();

			behaviorRebateAggregates.add(behaviorRebateAggregate);
		}

		// 存储聚合对象数据
		behaviorRebateRepository.saveUserRebateRecord(behaviorBO.getUserId(), behaviorRebateAggregates);

		return orderIds;
	}

	/**
	 * 根据外部单号查询订单
	 *
	 * @param userId        用户ID
	 * @param outBusinessNo 业务ID；签到则是日期字符串，支付则是外部的业务ID
	 * @return 返利订单实体
	 */
	@Override
	public List<BehaviorRebateOrderBO> queryOrderByOutBusinessNo(String userId, String outBusinessNo) {
		return behaviorRebateRepository.queryOrderByOutBusinessNo(userId,outBusinessNo);
	}
}
