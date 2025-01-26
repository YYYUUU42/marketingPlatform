package com.yu.market.server.activity.service.award;

import com.yu.market.common.event.BaseEvent;
import com.yu.market.common.utils.BeanCopyUtil;
import com.yu.market.server.activity.envent.SendAwardMessageEvent;
import com.yu.market.server.activity.model.aggregate.UserAwardRecordAggregate;
import com.yu.market.server.activity.model.bo.AwardTaskBO;
import com.yu.market.server.activity.model.bo.UserAwardRecordBO;
import com.yu.market.server.activity.respository.IAwardRepository;
import com.yu.market.server.task.model.enums.TaskStateEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author yu
 * @description 奖品服务
 * @date 2025-01-26
 */
@Service
@RequiredArgsConstructor
public class AwardService implements IAwardService{

	private final IAwardRepository awardRepository;
	private final SendAwardMessageEvent sendAwardMessageEvent;


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
}
