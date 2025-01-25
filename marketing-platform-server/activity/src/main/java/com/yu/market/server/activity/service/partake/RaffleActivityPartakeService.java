package com.yu.market.server.activity.service.partake;

import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.common.utils.BeanCopyUtil;
import com.yu.market.common.utils.SnowFlakeUtil;
import com.yu.market.server.activity.model.aggregate.CreatePartakeOrderAggregate;
import com.yu.market.server.activity.model.bo.*;
import com.yu.market.server.activity.model.enums.UserRaffleOrderStateEnum;
import com.yu.market.server.activity.respository.IActivityRepository;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author yu
 * @description 抽奖活动参与
 * @date 2025-01-26
 */
@Service
public class RaffleActivityPartakeService extends AbstractRaffleActivityPartake{
	private final SimpleDateFormat dateFormatMonth = new SimpleDateFormat("yyyy-MM");
	private final SimpleDateFormat dateFormatDay = new SimpleDateFormat("yyyy-MM-dd");

	public RaffleActivityPartakeService(IActivityRepository activityRepository) {
		super(activityRepository);
	}

	@Override
	protected CreatePartakeOrderAggregate doFilterAccount(String userId, Long activityId, Date currentDate) {
		// 查询总账户额度
		ActivityAccountBO activityAccountBO = activityRepository.queryActivityAccountByUserId(userId, activityId);

		// 额度判断（只判断总剩余额度）
		if (activityAccountBO == null || activityAccountBO.getTotalCountSurplus() <= 0) {
			throw new ServiceException(BaseErrorCode.ACCOUNT_QUOTA_ERROR);
		}

		String month = dateFormatMonth.format(currentDate);
		String day = dateFormatDay.format(currentDate);

		// 查询月账户额度
		ActivityAccountMonthBO activityAccountMonthBO = activityRepository.queryActivityAccountMonthByUserId(userId, activityId, month);
		if (activityAccountMonthBO != null && activityAccountMonthBO.getMonthCountSurplus() <= 0) {
			throw new ServiceException(BaseErrorCode.ACCOUNT_MONTH_QUOTA_ERROR);
		}
		// 创建月账户额度；true = 存在月账户、false = 不存在月账户
		boolean isExistAccountMonth = activityAccountMonthBO != null;
		if (activityAccountMonthBO == null) {
			activityAccountMonthBO = BeanCopyUtil.copyProperties(activityAccountBO, ActivityAccountMonthBO.class);
		}

		// 查询日账户额度
		ActivityAccountDayBO activityAccountDayBO = activityRepository.queryActivityAccountDayByUserId(userId, activityId, day);
		if (activityAccountDayBO != null && activityAccountDayBO.getDayCountSurplus() <= 0) {
			throw new ServiceException(BaseErrorCode.ACCOUNT_DAY_QUOTA_ERROR);
		}
		// 创建日账户额度；true = 存在日账户、false = 不存在日账户
		boolean isExistAccountDay = activityAccountDayBO != null;
		if (activityAccountDayBO == null) {
			activityAccountDayBO = BeanCopyUtil.copyProperties(activityAccountBO, ActivityAccountDayBO.class);
		}

		return CreatePartakeOrderAggregate.builder()
				.userId(userId)
				.activityId(activityId)
				.activityAccountBO(activityAccountBO)
				.isExistAccountMonth(isExistAccountMonth)
				.activityAccountMonthBO(activityAccountMonthBO)
				.isExistAccountDay(isExistAccountDay)
				.activityAccountDayBO(activityAccountDayBO)
				.build();
	}

	@Override
	protected UserRaffleOrderBO buildUserRaffleOrder(String userId, Long activityId, Date currentDate) {
		ActivityBO activityBO = activityRepository.queryRaffleActivityByActivityId(activityId);

		return UserRaffleOrderBO.builder()
				.userId(userId)
				.activityId(activityId)
				.activityName(activityBO.getActivityName())
				.strategyId(activityBO.getStrategyId())
				.orderId(String.valueOf(new SnowFlakeUtil().nextId()))
				.orderTime(currentDate)
				.orderState(UserRaffleOrderStateEnum.create)
				.build();
	}
}
