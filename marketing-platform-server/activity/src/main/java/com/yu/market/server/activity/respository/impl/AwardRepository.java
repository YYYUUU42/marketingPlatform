package com.yu.market.server.activity.respository.impl;

import cn.hutool.json.JSONUtil;
import com.yu.market.common.event.EventPublisher;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.common.utils.BeanCopyUtil;
import com.yu.market.server.activity.mapper.UserAwardRecordMapper;
import com.yu.market.server.activity.model.aggregate.UserAwardRecordAggregate;
import com.yu.market.server.activity.envent.task.AwardTaskBO;
import com.yu.market.server.activity.model.bo.UserAwardRecordBO;
import com.yu.market.server.activity.model.pojo.UserAwardRecord;
import com.yu.market.server.activity.respository.IAwardRepository;
import com.yu.market.server.task.mapper.TaskMapper;
import com.yu.market.server.task.model.pojo.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwardRepository implements IAwardRepository {

	private final TaskMapper taskMapper;
	private final UserAwardRecordMapper userAwardRecordMapper;
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
}
