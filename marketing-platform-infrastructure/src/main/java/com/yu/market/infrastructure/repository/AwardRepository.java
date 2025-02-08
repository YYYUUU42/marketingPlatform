package com.yu.market.infrastructure.repository;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yu.market.common.event.EventPublisher;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.common.utils.BeanCopyUtil;
import com.yu.market.infrastructure.mapper.*;
import com.yu.market.server.activity.model.aggregate.GiveOutPrizesAggregate;
import com.yu.market.server.activity.model.aggregate.UserAwardRecordAggregate;
import com.yu.market.server.activity.envent.task.AwardTaskBO;
import com.yu.market.server.activity.model.bo.UserAwardRecordBO;
import com.yu.market.server.activity.model.bo.UserCreditAwardBO;
import com.yu.market.server.activity.model.enums.AccountStatusEnum;
import com.yu.market.infrastructure.pojo.*;
import com.yu.market.server.activity.respository.IAwardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AwardRepository implements IAwardRepository {

	private final AwardMapper awardMapper;
	private final TaskMapper taskMapper;
	private final UserAwardRecordMapper userAwardRecordMapper;
	private final UserCreditAccountMapper userCreditAccountMapper;
	private final TransactionTemplate transactionTemplate;
	private final EventPublisher eventPublisher;

	@Override
	public void saveUserAwardRecord(UserAwardRecordAggregate userAwardRecordAggregate) {

		UserAwardRecordBO userAwardRecordBO = userAwardRecordAggregate.getUserAwardRecordBO();
		AwardTaskBO awardTaskBO = userAwardRecordAggregate.getAwardTaskBO();
		String userId = userAwardRecordBO.getUserId();
		Long activityId = userAwardRecordBO.getActivityId();
		Integer awardId = userAwardRecordBO.getAwardId();

		UserAwardRecord userAwardRecord = BeanCopyUtil.copyProperties(userAwardRecordBO, UserAwardRecord.class);
		userAwardRecord.setAwardState(userAwardRecordBO.getAwardState().getCode());

		Task task = BeanCopyUtil.copyProperties(awardTaskBO, Task.class);
		task.setMessage(JSONUtil.toJsonStr(awardTaskBO.getMessage()));
		task.setState(awardTaskBO.getState().getCode());

		transactionTemplate.execute(status -> {
			try {
				// 写入记录
				userAwardRecordMapper.insert(userAwardRecord);
				// 写入任务
				taskMapper.insert(task);

				return 1;
			} catch (DuplicateKeyException e) {
				status.setRollbackOnly();
				log.error("写入中奖记录，唯一索引冲突 userId: {} activityId: {} awardId: {}", userId, activityId, awardId, e);
				throw new ServiceException(BaseErrorCode.INDEX_DUP);
			}
		});

		try {
			// 发送消息【在事务外执行，如果失败还有任务补偿】
			eventPublisher.publish(task.getTopic(), task.getMessage());
			// 更新数据库记录，task 任务表
			taskMapper.updateTaskSendMessageCompleted(task.getUserId(), task.getMessageId());
		} catch (Exception e) {
			log.error("写入中奖记录，发送MQ消息失败 userId: {} topic: {}", userId, task.getTopic());
			taskMapper.updateTaskSendMessageFail(task.getUserId(), task.getMessageId());
		}

	}

	@Override
	public String queryAwardConfig(Integer awardId) {
		Award award = awardMapper.selectOne(new LambdaQueryWrapper<Award>()
				.eq(Award::getAwardId, awardId));
		if (award != null) {
			return award.getAwardConfig();
		}

		return null;
	}

	@Override
	public void saveGiveOutPrizesAggregate(GiveOutPrizesAggregate giveOutPrizesAggregate) {
		String userId = giveOutPrizesAggregate.getUserId();
		UserCreditAwardBO userCreditAwardBO = giveOutPrizesAggregate.getUserCreditAwardBO();
		UserAwardRecordBO userAwardRecordBO = giveOutPrizesAggregate.getUserAwardRecordBO();

		// 更新发奖记录
		UserAwardRecord userAwardRecord = new UserAwardRecord();
		userAwardRecord.setUserId(userId);
		userAwardRecord.setOrderId(userAwardRecordBO.getOrderId());
		userAwardRecord.setAwardState(userAwardRecordBO.getAwardState().getCode());

		// 更新用户积分 - 首次则插入数据
		UserCreditAccount userCreditAccount = new UserCreditAccount();
		userCreditAccount.setUserId(userCreditAwardBO.getUserId());
		userCreditAccount.setTotalAmount(userCreditAwardBO.getCreditAmount());
		userCreditAccount.setAvailableAmount(userCreditAwardBO.getCreditAmount());
		userCreditAccount.setAccountStatus(AccountStatusEnum.open.getCode());

		transactionTemplate.execute(status -> {
			try {
				// 更新积分 || 创建积分账户
				int updateAccountCount = userCreditAccountMapper.updateAddAmount(userCreditAccount);
				if (updateAccountCount == 0) {
					userCreditAccountMapper.insert(userCreditAccount);
				}

				// 更新奖品记录
				int updateAwardCount = userAwardRecordMapper.updateAwardRecordCompletedState(userAwardRecord);
				if (updateAwardCount == 0) {
					log.warn("更新中奖记录，重复更新拦截 userId:{} giveOutPrizesAggregate:{}", userId, JSONUtil.toJsonStr(giveOutPrizesAggregate));
					status.setRollbackOnly();
				}
				return 1;
			} catch (DuplicateKeyException e) {
				status.setRollbackOnly();
				log.error("更新中奖记录，唯一索引冲突 userId: {} ", userId, e);
				throw new ServiceException(BaseErrorCode.INDEX_DUP);
			}
		});
	}

	@Override
	public String queryAwardKey(Integer awardId) {
		Award award = awardMapper.selectOne(new LambdaQueryWrapper<Award>()
				.eq(Award::getAwardId, awardId));
		if (award != null) {
			return award.getAwardKey();
		}

		return null;
	}
}
