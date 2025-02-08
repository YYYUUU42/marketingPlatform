package com.yu.market.server.activity.service.partake;

import cn.hutool.json.JSONUtil;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.server.activity.model.aggregate.CreatePartakeOrderAggregate;
import com.yu.market.server.activity.model.bo.ActivityBO;
import com.yu.market.server.activity.model.bo.PartakeRaffleActivityBO;
import com.yu.market.server.activity.model.bo.UserRaffleOrderBO;
import com.yu.market.server.activity.model.enums.ActivityStateEnum;
import com.yu.market.server.activity.respository.IActivityRepository;
import com.yu.market.server.activity.service.IRaffleActivityPartakeService;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author yu
 * @description 抽奖活动参与抽象类
 * @date 2025-01-25
 */
@Slf4j
public abstract class AbstractRaffleActivityPartake implements IRaffleActivityPartakeService {

	protected final IActivityRepository activityRepository;

	public AbstractRaffleActivityPartake(IActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}

	/**
	 * 创建抽奖单；用户参与抽奖活动，扣减活动账户库存，产生抽奖单。如存在未被使用的抽奖单则直接返回已存在的抽奖单。
	 *
	 * @param userId     用户ID
	 * @param activityId 活动ID
	 * @return 用户抽奖订单实体对象
	 */
	@Override
	public UserRaffleOrderBO createOrder(String userId, Long activityId){
		return createOrder(PartakeRaffleActivityBO.builder()
				.userId(userId)
				.activityId(activityId)
				.build());
	}

	/**
	 * 创建抽奖单；用户参与抽奖活动，扣减活动账户库存，产生抽奖单。如存在未被使用的抽奖单则直接返回已存在的抽奖单。
	 *
	 * @param partakeRaffleActivityBO 参与抽奖活动实体对象
	 * @return 用户抽奖订单实体对象
	 */
	@Override
	public UserRaffleOrderBO createOrder(PartakeRaffleActivityBO partakeRaffleActivityBO) {
		// 基础信息
		String userId = partakeRaffleActivityBO.getUserId();
		Long activityId = partakeRaffleActivityBO.getActivityId();
		Date currentDate = new Date();

		// 活动查询
		ActivityBO activityBO = activityRepository.queryRaffleActivityByActivityId(activityId);

		// 校验；活动状态
		if (!activityBO.getState().equals(ActivityStateEnum.open)) {
			throw new ServiceException(BaseErrorCode.ACTIVITY_STATE_ERROR);
		}
		// 校验；活动日期「开始时间 <- 当前时间 -> 结束时间」
		if (activityBO.getBeginDateTime().after(currentDate) || activityBO.getEndDateTime().before(currentDate)) {
			throw new ServiceException(BaseErrorCode.ACTIVITY_DATE_ERROR);
		}

		// 查询未被使用的活动参与订单记录
		UserRaffleOrderBO userRaffleOrderBO = activityRepository.queryNoUsedRaffleOrder(partakeRaffleActivityBO);
		if (userRaffleOrderBO != null) {
			log.info("创建参与活动订单 userId:{} activityId:{} userRaffleOrderBO:{}", userId, activityId, JSONUtil.toJsonStr(userRaffleOrderBO));
			return userRaffleOrderBO;
		}

		// 额度账户过滤&返回账户构建对象
		CreatePartakeOrderAggregate createPartakeOrderAggregate = this.doFilterAccount(userId, activityId, currentDate);

		// 构建订单
		UserRaffleOrderBO userRaffleOrder = this.buildUserRaffleOrder(userId, activityId, currentDate);

		// 填充抽奖单实体对象
		createPartakeOrderAggregate.setUserRaffleOrderBO(userRaffleOrder);

		// 保存聚合对象 - 一个领域内的一个聚合是一个事务操作
		activityRepository.saveCreatePartakeOrderAggregate(createPartakeOrderAggregate);
		log.info("创建活动抽奖单完成 userId:{} activityId:{} orderId:{}", userId, activityId, userRaffleOrder.getOrderId());

		// 返回订单信息
		return userRaffleOrder;
	}

	protected abstract CreatePartakeOrderAggregate doFilterAccount(String userId, Long activityId, Date currentDate);

	protected abstract UserRaffleOrderBO buildUserRaffleOrder(String userId, Long activityId, Date currentDate);
}
