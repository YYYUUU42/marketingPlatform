package com.yu.market.infrastructure.repository;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yu.market.common.event.EventPublisher;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.common.utils.BeanCopyUtil;
import com.yu.market.server.activity.envent.task.RebateTaskBO;
import com.yu.market.server.activity.model.aggregate.BehaviorRebateAggregate;
import com.yu.market.server.activity.model.bo.BehaviorRebateOrderBO;
import com.yu.market.server.activity.model.bo.DailyBehaviorRebateBO;
import com.yu.market.server.activity.model.enums.BehaviorTypeEnum;
import com.yu.market.server.activity.model.enums.DailyBehaviorRebateEnum;
import com.yu.market.infrastructure.pojo.*;
import com.yu.market.infrastructure.mapper.*;
import com.yu.market.server.activity.respository.IBehaviorRebateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.List;

/**
 * @author yu
 * @description 行为返利服务仓储实现
 * @date 2025-01-26
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class BehaviorRebateRepository implements IBehaviorRebateRepository {

	private final DailyBehaviorRebateMapper dailyBehaviorRebateMapper;
	private final UserBehaviorRebateOrderMapper userBehaviorRebateOrderMapper;
	private final TaskMapper taskMapper;
	private final TransactionTemplate transactionTemplate;
	private final EventPublisher eventPublisher;

	@Override
	public List<DailyBehaviorRebateBO> queryDailyBehaviorRebateConfig(BehaviorTypeEnum behaviorTypeEnum) {
		List<DailyBehaviorRebate> dailyBehaviorRebateList = dailyBehaviorRebateMapper.selectList(new LambdaQueryWrapper<DailyBehaviorRebate>()
				.eq(DailyBehaviorRebate::getState, DailyBehaviorRebateEnum.open.getCode()));

		return BeanCopyUtil.copyListProperties(dailyBehaviorRebateList, DailyBehaviorRebateBO.class);
	}

	@Override
	public void saveUserRebateRecord(String userId, List<BehaviorRebateAggregate> behaviorRebateAggregates) {
		transactionTemplate.execute(status -> {
			try {
				for (BehaviorRebateAggregate behaviorRebateAggregate : behaviorRebateAggregates) {
					BehaviorRebateOrderBO behaviorRebateOrderBO = behaviorRebateAggregate.getBehaviorRebateOrderBO();
					// 用户行为返利订单对象
					UserBehaviorRebateOrder userBehaviorRebateOrder = BeanCopyUtil.copyProperties(behaviorRebateOrderBO, UserBehaviorRebateOrder.class);
					userBehaviorRebateOrderMapper.insert(userBehaviorRebateOrder);

					// 任务对象
					RebateTaskBO rebateTaskBO = behaviorRebateAggregate.getRebateTaskBO();
					Task task = BeanCopyUtil.copyProperties(rebateTaskBO, Task.class);
					task.setMessage(JSONUtil.toJsonStr(rebateTaskBO.getMessage()));
					task.setState(rebateTaskBO.getState().getCode());
					taskMapper.insert(task);
				}
				return 1;
			} catch (DuplicateKeyException e) {
				status.setRollbackOnly();
				log.error("写入返利记录，唯一索引冲突 userId: {}", userId, e);
				throw new ServiceException(BaseErrorCode.INDEX_DUP);
			}
		});

		// 同步发送 MQ 消息
		for (BehaviorRebateAggregate behaviorRebateAggregate : behaviorRebateAggregates) {
			RebateTaskBO rebateTaskBO = behaviorRebateAggregate.getRebateTaskBO();

			try {
				// 发送消息【在事务外执行，如果失败还有任务补偿】
				eventPublisher.publish(rebateTaskBO.getTopic(), rebateTaskBO.getMessage());
				// 更新数据库记录，task 任务表
				taskMapper.updateTaskSendMessageCompleted(userId, rebateTaskBO.getMessageId());
			} catch (Exception e) {
				log.error("写入返利记录，发送MQ消息失败 userId: {} topic: {}", userId, rebateTaskBO.getUserId());
				taskMapper.updateTaskSendMessageFail(userId, rebateTaskBO.getMessageId());
			}
		}

	}

	@Override
	public List<BehaviorRebateOrderBO> queryOrderByOutBusinessNo(String userId, String outBusinessNo) {
		List<UserBehaviorRebateOrder> userBehaviorRebateOrderList = userBehaviorRebateOrderMapper.selectList(new LambdaQueryWrapper<UserBehaviorRebateOrder>()
				.eq(UserBehaviorRebateOrder::getUserId, userId)
				.eq(UserBehaviorRebateOrder::getOutBusinessNo, outBusinessNo));
		if (CollectionUtil.isEmpty(userBehaviorRebateOrderList)) {
			return List.of();
		}

		return BeanCopyUtil.copyListProperties(userBehaviorRebateOrderList, BehaviorRebateOrderBO.class);
	}
}
