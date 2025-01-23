package com.yu.market.server.activity.respository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yu.market.common.contants.RedisKey;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.common.redis.IRedisService;
import com.yu.market.common.utils.BeanCopyUtil;
import com.yu.market.server.activity.mapper.RaffleActivityCountMapper;
import com.yu.market.server.activity.mapper.RaffleActivityMapper;
import com.yu.market.server.activity.mapper.RaffleActivityOrderMapper;
import com.yu.market.server.activity.mapper.RaffleActivitySkuMapper;
import com.yu.market.server.activity.model.aggregate.CreateOrderAggregate;
import com.yu.market.server.activity.model.bo.ActivityBO;
import com.yu.market.server.activity.model.bo.ActivityCountBO;
import com.yu.market.server.activity.model.bo.ActivityOrderBO;
import com.yu.market.server.activity.model.bo.ActivitySkuBO;
import com.yu.market.server.activity.model.pojo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;

/**
 * @author yu
 * @description 活动仓储服务
 * @date 2025-01-19
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ActivityRepository implements IActivityRepository {

	private final IRedisService redisService;
	private final RaffleActivityMapper activityMapper;
	private final RaffleActivitySkuMapper activitySkuMapper;
	private final RaffleActivityCountMapper activityCountMapper;
	private final RaffleActivityOrderMapper activityOrderMapper;
	private final TransactionTemplate transactionTemplate;


	/**
	 * 通过 sku 查询活动信息
	 */
	@Override
	public ActivitySkuBO queryActivitySku(Long sku) {
		RaffleActivitySku activitySku = activitySkuMapper.selectOne(new LambdaQueryWrapper<RaffleActivitySku>()
				.eq(RaffleActivitySku::getSku, sku));

		return BeanCopyUtil.copyProperties(activitySku, ActivitySkuBO.class);
	}

	/**
	 * 查询活动信息
	 */
	@Override
	public ActivityBO queryRaffleActivityByActivityId(Long activityId) {
		// 优先从缓存获取
		String cacheKey = RedisKey.ACTIVITY_KEY + activityId;
		ActivityBO activityBO = redisService.getValue(cacheKey);
		if (activityBO != null) {
			return activityBO;
		}

		RaffleActivity raffleActivity = activityMapper.selectOne(new LambdaQueryWrapper<RaffleActivity>()
				.eq(RaffleActivity::getActivityId, activityId));
		activityBO = BeanCopyUtil.copyProperties(raffleActivity, ActivityBO.class);

		redisService.setValue(cacheKey, activityBO);
		return activityBO;
	}

	/**
	 * 查询次数信息（用户在活动上可参与的次数
	 */
	@Override
	public ActivityCountBO queryRaffleActivityCountByActivityCountId(Long activityCountId) {
		// 优先从缓存获取
		String cacheKey = RedisKey.ACTIVITY_COUNT_KEY + activityCountId;
		ActivityCountBO activityCountBO = redisService.getValue(cacheKey);
		if (activityCountBO != null) {
			return activityCountBO;
		}

		RaffleActivityCount raffleActivityCount = activityCountMapper.selectOne(new LambdaQueryWrapper<RaffleActivityCount>()
				.eq(RaffleActivityCount::getActivityCountId, activityCountBO));
		activityCountBO  = BeanCopyUtil.copyProperties(raffleActivityCount, ActivityCountBO.class);

		redisService.setValue(cacheKey, activityCountBO);
		return activityCountBO;
	}

	/**
	 * 保存订单
	 */
	@Override
	public void doSaveOrder(CreateOrderAggregate createOrderAggregate) {
		// 订单对象
		ActivityOrderBO activityOrderBO = createOrderAggregate.getActivityOrderBO();
		RaffleActivityOrder raffleActivityOrder = new RaffleActivityOrder();
		raffleActivityOrder.setUserId(activityOrderBO.getUserId());
		raffleActivityOrder.setSku(activityOrderBO.getSku());
		raffleActivityOrder.setActivityId(activityOrderBO.getActivityId());
		raffleActivityOrder.setActivityName(activityOrderBO.getActivityName());
		raffleActivityOrder.setStrategyId(activityOrderBO.getStrategyId());
		raffleActivityOrder.setOrderId(activityOrderBO.getOrderId());
		raffleActivityOrder.setOrderTime(activityOrderBO.getOrderTime());
		raffleActivityOrder.setTotalCount(activityOrderBO.getTotalCount());
		raffleActivityOrder.setDayCount(activityOrderBO.getDayCount());
		raffleActivityOrder.setMonthCount(activityOrderBO.getMonthCount());
		raffleActivityOrder.setTotalCount(createOrderAggregate.getTotalCount());
		raffleActivityOrder.setDayCount(createOrderAggregate.getDayCount());
		raffleActivityOrder.setMonthCount(createOrderAggregate.getMonthCount());
		raffleActivityOrder.setState(activityOrderBO.getState().getCode());
		raffleActivityOrder.setOutBusinessNo(activityOrderBO.getOutBusinessNo());

		// 账户对象
		RaffleActivityAccount raffleActivityAccount = new RaffleActivityAccount();
		raffleActivityAccount.setUserId(createOrderAggregate.getUserId());
		raffleActivityAccount.setActivityId(createOrderAggregate.getActivityId());
		raffleActivityAccount.setTotalCount(createOrderAggregate.getTotalCount());
		raffleActivityAccount.setTotalCountSurplus(createOrderAggregate.getTotalCount());
		raffleActivityAccount.setDayCount(createOrderAggregate.getDayCount());
		raffleActivityAccount.setDayCountSurplus(createOrderAggregate.getDayCount());
		raffleActivityAccount.setMonthCount(createOrderAggregate.getMonthCount());
		raffleActivityAccount.setMonthCountSurplus(createOrderAggregate.getMonthCount());

		// 编程式事务
		transactionTemplate.execute(status -> {
			try {
				// 写入订单
				activityOrderMapper.insert(raffleActivityOrder);

				// 更新账户
				int count = activityCountMapper.updateAccountQuota(raffleActivityAccount);

				// 更新为0，则账户不存在，创新新账户
				if (count == 0) {
					activityCountMapper.insert((Collection<RaffleActivityCount>) raffleActivityAccount);
				}

				return 1;
			} catch (DuplicateKeyException e) {
				status.setRollbackOnly();
				log.error("写入订单记录，唯一索引冲突 userId: {} activityId: {} sku: {}", activityOrderBO.getUserId(), activityOrderBO.getActivityId(), activityOrderBO.getSku(), e);
				throw new ServiceException(BaseErrorCode.INDEX_DUP.code());
			}
		});
	}
}
