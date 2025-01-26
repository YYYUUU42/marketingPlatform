package com.yu.market.server.activity.respository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yu.market.common.contants.Constants;
import com.yu.market.common.contants.RedisKey;
import com.yu.market.common.event.EventPublisher;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.common.redis.IRedisService;
import com.yu.market.common.utils.BeanCopyUtil;
import com.yu.market.server.activity.envent.ActivitySkuStockZeroMessageEvent;
import com.yu.market.server.activity.mapper.*;
import com.yu.market.server.activity.model.aggregate.CreateQuotaOrderAggregate;
import com.yu.market.server.activity.model.bo.*;
import com.yu.market.server.activity.model.enums.OrderStateEnum;
import com.yu.market.server.activity.model.enums.UserRaffleOrderStateEnum;
import com.yu.market.server.activity.model.pojo.*;
import com.yu.market.server.activity.respository.IActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
	private final RaffleActivityAccountMapper activityAccountMapper;
	private final UserRaffleOrderMapper userRaffleOrderMapper;
	private final RaffleActivityAccountMonthMapper activityAccountMonthMapper;
	private final RaffleActivityAccountDayMapper activityAccountDayMapper;
	private final TransactionTemplate transactionTemplate;
	private final EventPublisher eventPublisher;
	private final ActivitySkuStockZeroMessageEvent activitySkuStockZeroMessageEvent;


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
	 * 缓存活动 sku 数量
	 */
	@Override
	public void cacheActivitySkuStockCount(String cacheKey, Integer stockCount) {
		if (!redisService.isExists(cacheKey)) {
			redisService.setAtomicLong(cacheKey, stockCount);
		}
	}

	/**
	 * 保存订单
	 */
	@Override
	public void doSaveOrder(CreateQuotaOrderAggregate createOrderAggregate) {
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

	/**
	 * 扣减活动sku数量
	 */
	@Override
	public boolean subtractionActivitySkuStock(Long sku, String cacheKey, Date endDateTime) {
		// 减少库存并获取剩余库存数量
		long surplus = redisService.decr(cacheKey);

		if (surplus == 0) {
			// 库存消耗完毕，发送消息更新数据库库存
			eventPublisher.publish(
					activitySkuStockZeroMessageEvent.topic(),
					activitySkuStockZeroMessageEvent.buildEventMessage(sku)
			);
			return false;
		}

		if (surplus < 0) {
			// 如果库存小于0，恢复库存为0，并返回
			redisService.setAtomicLong(cacheKey, 0);
			log.warn("库存不足警告：sku={}，cacheKey={}，surplus={}", sku, cacheKey, surplus);
			return false;
		}

		// 计算锁的过期时间：活动结束时间 + 延迟1天
		long expireMillis = endDateTime.getTime() - System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);

		// 拼接锁的唯一标识
		String lockKey = cacheKey + Constants.UNDERLINE + surplus;

		// 尝试加锁
		boolean lockAcquired = redisService.setNx(lockKey, expireMillis, TimeUnit.MILLISECONDS);
		if (!lockAcquired) {
			log.info("活动SKU库存加锁失败：sku={}，cacheKey={}，lockKey={}", sku, cacheKey, lockKey);
		}

		return lockAcquired;
	}

	/**
	 * 将消耗的发送到消息队列
	 */
	@Override
	public void activitySkuStockConsumeSendQueue(ActivitySkuStockKeyBO activitySkuStockKeyVO) {
		String cacheKey = RedisKey.ACTIVITY_SKU_COUNT_QUERY_KEY;
		RBlockingQueue<ActivitySkuStockKeyBO> blockingQueue = redisService.getBlockingQueue(cacheKey);
		RDelayedQueue<ActivitySkuStockKeyBO> delayedQueue = redisService.getDelayedQueue(blockingQueue);
		delayedQueue.offer(activitySkuStockKeyVO, 3, TimeUnit.SECONDS);
	}

	/**
	 * 获取活动 sku 库存消耗队列
	 */
	@Override
	public ActivitySkuStockKeyBO takeQueueValue() {
		String cacheKey = RedisKey.ACTIVITY_SKU_COUNT_QUERY_KEY;
		RBlockingQueue<ActivitySkuStockKeyBO> destinationQueue = redisService.getBlockingQueue(cacheKey);
		return destinationQueue.poll();
	}

	/**
	 * 清空队列
	 */
	@Override
	public void clearQueueValue() {
		String cacheKey = RedisKey.ACTIVITY_SKU_COUNT_QUERY_KEY;
		RBlockingQueue<ActivitySkuStockKeyBO> destinationQueue = redisService.getBlockingQueue(cacheKey);
		destinationQueue.clear();
	}

	/**
	 * 延迟队列 + 任务趋势更新活动sku库存
	 */
	@Override
	public void updateActivitySkuStock(Long sku) {
		activitySkuMapper.updateActivitySkuStock(sku);
	}

	/**
	 * 缓存库已消耗完毕，清空数据库库存
	 */
	@Override
	public void clearActivitySkuStock(Long sku) {
		activitySkuMapper.clearActivitySkuStock(sku);
	}

	/**
	 * 查询未被使用的活动参与订单记录
	 */
	@Override
	public UserRaffleOrderBO queryNoUsedRaffleOrder(PartakeRaffleActivityBO partakeRaffleActivityBO) {
		UserRaffleOrder userRaffleOrder = userRaffleOrderMapper.selectOne(new LambdaQueryWrapper<UserRaffleOrder>()
				.eq(UserRaffleOrder::getUserId, partakeRaffleActivityBO.getUserId())
				.eq(UserRaffleOrder::getActivityId, partakeRaffleActivityBO.getUserId())
				.eq(UserRaffleOrder::getOrderState, OrderStateEnum.create.getCode()));
		if (userRaffleOrder == null) {
			return null;
		}

		UserRaffleOrderBO userRaffleOrderBO = BeanCopyUtil.copyProperties(userRaffleOrder, UserRaffleOrderBO.class);
		userRaffleOrderBO.setOrderState(UserRaffleOrderStateEnum.valueOf(userRaffleOrder.getOrderState()));

		return userRaffleOrderBO;
	}

	/**
	 * 查询总账户额度
	 */
	@Override
	public ActivityAccountBO queryActivityAccountByUserId(String userId, Long activityId) {
		RaffleActivityAccount raffleActivityAccount = activityAccountMapper.selectOne(new LambdaQueryWrapper<RaffleActivityAccount>()
				.eq(RaffleActivityAccount::getUserId, userId)
				.eq(RaffleActivityAccount::getActivityId, activityId));
		if (raffleActivityAccount == null) {
			return null;
		}

		return ActivityAccountBO.builder()
				.userId(raffleActivityAccount.getUserId())
				.activityId(raffleActivityAccount.getActivityId())
				.totalCount(raffleActivityAccount.getTotalCount())
				.totalCountSurplus(raffleActivityAccount.getTotalCountSurplus())
				.dayCount(raffleActivityAccount.getDayCount())
				.dayCountSurplus(raffleActivityAccount.getDayCountSurplus())
				.monthCount(raffleActivityAccount.getMonthCount())
				.monthCountSurplus(raffleActivityAccount.getMonthCountSurplus())
				.build();
	}

	/**
	 * 查询月账户额度
	 */
	@Override
	public ActivityAccountMonthBO queryActivityAccountMonthByUserId(String userId, Long activityId, String month) {
		RaffleActivityAccountMonth raffleActivityAccountMonth = activityAccountMonthMapper.selectOne(new LambdaQueryWrapper<RaffleActivityAccountMonth>()
				.eq(RaffleActivityAccountMonth::getUserId, userId)
				.eq(RaffleActivityAccountMonth::getActivityId, activityId)
				.eq(RaffleActivityAccountMonth::getMonth, month));
		if (raffleActivityAccountMonth == null){
			return null;
		}

		return ActivityAccountMonthBO.builder()
				.userId(raffleActivityAccountMonth.getUserId())
				.activityId(raffleActivityAccountMonth.getActivityId())
				.month(raffleActivityAccountMonth.getMonth())
				.monthCount(raffleActivityAccountMonth.getMonthCount())
				.monthCountSurplus(raffleActivityAccountMonth.getMonthCountSurplus())
				.build();
	}

	/**
	 * 查询日账户额度
	 */
	@Override
	public ActivityAccountDayBO queryActivityAccountDayByUserId(String userId, Long activityId, String day) {
		RaffleActivityAccountDay raffleActivityAccountDay = activityAccountDayMapper.selectOne(new LambdaQueryWrapper<RaffleActivityAccountDay>()
				.eq(RaffleActivityAccountDay::getUserId, userId)
				.eq(RaffleActivityAccountDay::getActivityId, activityId)
				.eq(RaffleActivityAccountDay::getDay, day));
		if (raffleActivityAccountDay == null){
			return null;
		}

		return ActivityAccountDayBO.builder()
				.userId(raffleActivityAccountDay.getUserId())
				.activityId(raffleActivityAccountDay.getActivityId())
				.day(raffleActivityAccountDay.getDay())
				.dayCount(raffleActivityAccountDay.getDayCount())
				.dayCountSurplus(raffleActivityAccountDay.getDayCountSurplus())
				.build();
	}

}
