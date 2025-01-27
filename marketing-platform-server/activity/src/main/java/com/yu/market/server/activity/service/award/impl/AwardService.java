package com.yu.market.server.activity.service.award.impl;

import cn.hutool.core.util.StrUtil;
import com.yu.market.common.event.BaseEvent;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.utils.BeanCopyUtil;
import com.yu.market.server.activity.envent.SendAwardMessageEvent;
import com.yu.market.server.activity.model.aggregate.UserAwardRecordAggregate;
import com.yu.market.server.activity.envent.task.AwardTaskBO;
import com.yu.market.server.activity.model.bo.DistributeAwardBO;
import com.yu.market.server.activity.model.bo.UserAwardRecordBO;
import com.yu.market.server.activity.respository.IAwardRepository;
import com.yu.market.server.activity.service.award.IAwardService;
import com.yu.market.server.activity.service.award.IDistributeAward;
import com.yu.market.server.task.model.enums.TaskStateEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author yu
 * @description 奖品服务
 * @date 2025-01-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AwardService implements IAwardService {

	private final IAwardRepository awardRepository;
	private final SendAwardMessageEvent sendAwardMessageEvent;
	private final Map<String, IDistributeAward> distributeAwardMap;


	@Override
	public void saveUserAwardRecord(UserAwardRecordBO userAwardRecordBO) {
		// 构建消息对象
		SendAwardMessageEvent.SendAwardMessage sendAwardMessage = BeanCopyUtil.copyProperties(userAwardRecordBO, SendAwardMessageEvent.SendAwardMessage.class);
		BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage> sendAwardMessageEventMessage = sendAwardMessageEvent.buildEventMessage(sendAwardMessage);

		AwardTaskBO awardTaskBO = AwardTaskBO.builder()
				.userId(userAwardRecordBO.getUserId())
				.topic(sendAwardMessageEvent.topic())
				.messageId(sendAwardMessageEventMessage.getId())
				.message(sendAwardMessageEventMessage)
				.state(TaskStateEnum.create)
				.build();

		// 构建聚合对象
		UserAwardRecordAggregate userAwardRecordAggregate = UserAwardRecordAggregate.builder()
				.awardTaskBO(awardTaskBO)
				.userAwardRecordBO(userAwardRecordBO)
				.build();

		// 存储聚合对象 - 一个事务下，用户的中奖记录
		awardRepository.saveUserAwardRecord(userAwardRecordAggregate);
	}

	/**
	 * 配送发货奖品
	 */
	@Override
	public void distributeAward(DistributeAwardBO distributeAwardBO) {
		String awardKey = awardRepository.queryAwardKey(distributeAwardBO.getAwardId());
		if (StrUtil.isBlank(awardKey)) {
			log.error("分发奖品，奖品ID不存在。awardKey:{}", awardKey);
			return;
		}

		IDistributeAward distributeAward = distributeAwardMap.get(awardKey);
		if (distributeAward == null) {
			log.error("分发奖品，对应的服务不存在。awardKey:{}", awardKey);
			throw new ServiceException("分发奖品，奖品" + awardKey + "对应的服务不存在");
		}

		distributeAward.giveOutPrizes(distributeAwardBO);
	}
}
