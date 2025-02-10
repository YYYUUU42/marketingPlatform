package com.yu.market.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yu.market.common.contants.Constants;
import com.yu.market.common.contants.RedisKey;
import com.yu.market.common.event.EventPublisher;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.common.redis.IRedisService;
import com.yu.market.common.utils.BeanCopyUtil;
import com.yu.market.infrastructure.mapper.*;
import com.yu.market.server.activity.envent.ActivitySkuStockZeroMessageEvent;
import com.yu.market.server.activity.model.aggregate.CreatePartakeOrderAggregate;
import com.yu.market.server.activity.model.aggregate.CreateQuotaOrderAggregate;
import com.yu.market.server.activity.model.bo.*;
import com.yu.market.server.activity.model.enums.ActivityStateEnum;
import com.yu.market.server.activity.model.enums.OrderStateEnum;
import com.yu.market.server.activity.model.enums.UserRaffleOrderStateEnum;
import com.yu.market.infrastructure.pojo.*;
import com.yu.market.server.activity.respository.IActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RLock;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
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
	private final RaffleActivityAccountMonthMapper raffleActivityAccountMonthMapper;
	private final RaffleActivityAccountDayMapper raffleActivityAccountDayMapper;
	private final RaffleActivitySkuMapper raffleActivitySkuMapper;
	private final RaffleActivityCountMapper raffleActivityCountMapper;
	private final UserCreditAccountMapper userCreditAccountMapper;
	private final RaffleActivityAccountMapper raffleActivityAccountMapper;


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
		if (raffleActivity == null){
			return new ActivityBO();
		}
		activityBO = BeanCopyUtil.copyProperties(raffleActivity, ActivityBO.class);
		activityBO.setState(ActivityStateEnum.valueOf(raffleActivity.getState()));

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
				.eq(RaffleActivityCount::getActivityCountId, activityCountId));
		if (raffleActivityCount == null) {
			return new ActivityCountBO();
		}

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

	@Override
	public void doSaveNoPayOrder(CreateQuotaOrderAggregate createOrderAggregate) {
		RLock lock = redisService.getLock(RedisKey.ACTIVITY_ACCOUNT_LOCK + createOrderAggregate.getUserId() + Constants.UNDERLINE + createOrderAggregate.getActivityId());
		try {
			lock.lock(3, TimeUnit.SECONDS);

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

			// 账户对象 - 月
			RaffleActivityAccountMonth raffleActivityAccountMonth = new RaffleActivityAccountMonth();
			raffleActivityAccountMonth.setUserId(createOrderAggregate.getUserId());
			raffleActivityAccountMonth.setActivityId(createOrderAggregate.getActivityId());
			raffleActivityAccountMonth.setMonth(raffleActivityAccountMonth.getMonth());
			raffleActivityAccountMonth.setMonthCount(createOrderAggregate.getMonthCount());
			raffleActivityAccountMonth.setMonthCountSurplus(createOrderAggregate.getMonthCount());

			// 账户对象 - 日
			RaffleActivityAccountDay raffleActivityAccountDay = new RaffleActivityAccountDay();
			raffleActivityAccountDay.setUserId(createOrderAggregate.getUserId());
			raffleActivityAccountDay.setActivityId(createOrderAggregate.getActivityId());
			raffleActivityAccountDay.setDay(raffleActivityAccountDay.getDay());
			raffleActivityAccountDay.setDayCount(createOrderAggregate.getDayCount());
			raffleActivityAccountDay.setDayCountSurplus(createOrderAggregate.getDayCount());

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

					// 更新账户 - 月
					raffleActivityAccountMonthMapper.addAccountQuota(raffleActivityAccountMonth);
					// 更新账户 - 日
					raffleActivityAccountDayMapper.addAccountQuota(raffleActivityAccountDay);

					return 1;
				} catch (DuplicateKeyException e) {
					status.setRollbackOnly();
					log.error("写入订单记录，唯一索引冲突 userId: {} activityId: {} sku: {}", activityOrderBO.getUserId(), activityOrderBO.getActivityId(), activityOrderBO.getSku(), e);
					throw new ServiceException(BaseErrorCode.INDEX_DUP.code());
				}
			});
		} finally {
			if (lock.isLocked() && lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	/**
	 * 保存订单
	 */
	@Override
	public void doSaveCreditPayOrder(CreateQuotaOrderAggregate createOrderAggregate) {
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


		// 编程式事务
		transactionTemplate.execute(status -> {
			try {
				// 写入订单
				activityOrderMapper.insert(raffleActivityOrder);

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
				.eq(UserRaffleOrder::getOrderState, OrderStateEnum.create.getCode())
				.last("LIMIT 1"));
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

	@Override
	public void saveCreatePartakeOrderAggregate(CreatePartakeOrderAggregate createPartakeOrderAggregate) {
		String userId = createPartakeOrderAggregate.getUserId();
		Long activityId = createPartakeOrderAggregate.getActivityId();
		ActivityAccountMonthBO activityAccountMonthBO = createPartakeOrderAggregate.getActivityAccountMonthBO();
		ActivityAccountDayBO activityAccountDayBO = createPartakeOrderAggregate.getActivityAccountDayBO();
		UserRaffleOrderBO userRaffleOrderBO = createPartakeOrderAggregate.getUserRaffleOrderBO();

		transactionTemplate.execute(status -> {
			try {
				// 更新总账户
				int totalCount = activityAccountMapper.updateActivityAccountSubtractionQuota(userId, activityId);
				if (totalCount != 1) {
					status.setRollbackOnly();
					log.warn("写入创建参与活动记录，更新总账户额度不足，异常 userId: {} activityId: {}", userId, activityId);
					throw new ServiceException(BaseErrorCode.ACCOUNT_QUOTA_ERROR);
				}

				// 创建或更新月账户，true - 存在则更新，false - 不存在则插入
				if (createPartakeOrderAggregate.isExistAccountMonth()) {
					int updateMonthCount = activityAccountMonthMapper.updateActivityAccountMonthSubtractionQuota(userId, activityId, activityAccountMonthBO.getMonth());
					if (updateMonthCount != 1) {
						// 未更新成功则回滚
						status.setRollbackOnly();
						log.warn("写入创建参与活动记录，更新月账户额度不足，异常 userId: {} activityId: {} month: {}", userId, activityId, activityAccountMonthBO.getMonth());
						throw new ServiceException(BaseErrorCode.ACCOUNT_MONTH_QUOTA_ERROR);
					}

					// 更新总账户中月账户库存
					activityAccountMapper.updateActivityAccountSubtractionQuota(userId, activityId);
				} else {
					RaffleActivityAccountMonth raffleActivityAccountMonth = BeanCopyUtil.copyProperties(activityAccountMonthBO, RaffleActivityAccountMonth.class);
					raffleActivityAccountMonth.setMonthCountSurplus(activityAccountMonthBO.getMonthCountSurplus() - 1);
					activityAccountMonthMapper.insert(raffleActivityAccountMonth);

					activityAccountMapper.updateActivityAccountMonthSurplusImageQuota(userId, activityId, activityAccountMonthBO.getMonthCountSurplus());
				}

				// 创建或更新日账户，true - 存在则更新，false - 不存在则插入
				if (createPartakeOrderAggregate.isExistAccountDay()) {
					int updateDayCount = activityAccountDayMapper.updateActivityAccountDaySubtractionQuota(userId, activityId, activityAccountDayBO.getDay());
					if (updateDayCount != 1) {
						// 未更新成功则回滚
						status.setRollbackOnly();
						log.warn("写入创建参与活动记录，更新日账户额度不足，异常 userId: {} activityId: {} month: {}", userId, activityId, activityAccountDayBO.getDay());
						throw new ServiceException(BaseErrorCode.ACCOUNT_MONTH_QUOTA_ERROR);
					}

					// 更新总账户中月账户库存
					activityAccountMapper.updateActivityAccountSubtractionQuota(userId, activityId);
				} else {
					RaffleActivityAccountDay raffleActivityAccountDay = BeanCopyUtil.copyProperties(activityAccountDayBO, RaffleActivityAccountDay.class);
					raffleActivityAccountDay.setDayCountSurplus(activityAccountDayBO.getDayCountSurplus() - 1);
					activityAccountDayMapper.insert(raffleActivityAccountDay);

					activityAccountMapper.updateActivityAccountDaySurplusImageQuota(userId, activityId, activityAccountDayBO.getDayCountSurplus());
				}

				// 写入参与活动订单
				UserRaffleOrder userRaffleOrder = BeanCopyUtil.copyProperties(userRaffleOrderBO, UserRaffleOrder.class);
				userRaffleOrder.setOrderState(userRaffleOrderBO.getOrderState().getCode());
				userRaffleOrderMapper.insert(userRaffleOrder);

				return 1;
			} catch (DuplicateKeyException e) {
				status.setRollbackOnly();
				log.error("写入创建参与活动记录，唯一索引冲突 userId: {} activityId: {}", userId, activityId, e);
				throw new ServiceException(BaseErrorCode.INDEX_DUP);
			}
		});
	}

	/**
	 * 根据活动ID获得活动sku列表
	 */
	@Override
	public List<ActivitySkuBO> queryActivitySkuListByActivityId(Long activityId) {
		List<RaffleActivitySku> raffleActivitySkuList = activitySkuMapper.selectList(new LambdaQueryWrapper<RaffleActivitySku>()
				.eq(RaffleActivitySku::getActivityId, activityId));

		return BeanCopyUtil.copyListProperties(raffleActivitySkuList, ActivitySkuBO.class);
	}

	@Override
	public List<SkuProductBO> querySkuProductBOListByActivityId(Long activityId) {
		List<RaffleActivitySku> raffleActivitySkuList = raffleActivitySkuMapper.selectList(new LambdaQueryWrapper<RaffleActivitySku>()
				.eq(RaffleActivitySku::getActivityId, activityId));

		List<SkuProductBO> skuProductBOList = new ArrayList<>(raffleActivitySkuList.size());
		for (RaffleActivitySku raffleActivitySku : raffleActivitySkuList) {
			RaffleActivityCount raffleActivityCount = raffleActivityCountMapper.selectOne(new LambdaQueryWrapper<RaffleActivityCount>()
					.eq(RaffleActivityCount::getActivityCountId, raffleActivitySku.getActivityCountId()));

			SkuProductBO.ActivityCount activityCount = BeanCopyUtil.copyProperties(raffleActivityCount, SkuProductBO.ActivityCount.class);
			SkuProductBO skuProductBO = BeanCopyUtil.copyProperties(raffleActivitySku, SkuProductBO.class);
			skuProductBO.setActivityCount(activityCount);
			skuProductBOList.add(skuProductBO);
		}

		return skuProductBOList;
	}

	@Override
	public ActivityAccountBO queryActivityAccountBO(Long activityId, String userId) {
		// 查询总账户额度
		RaffleActivityAccount raffleActivityAccount = activityAccountMapper.selectOne(new LambdaQueryWrapper<RaffleActivityAccount>()
				.eq(RaffleActivityAccount::getActivityId, activityId)
				.eq(RaffleActivityAccount::getUserId, userId));
		if (raffleActivityAccount == null) {
			return ActivityAccountBO.builder()
					.activityId(activityId)
					.userId(userId)
					.totalCount(0)
					.totalCountSurplus(0)
					.monthCount(0)
					.monthCountSurplus(0)
					.dayCount(0)
					.dayCountSurplus(0)
					.build();
		}

		// 查询月账户额度
		RaffleActivityAccountMonth raffleActivityAccountMonth = raffleActivityAccountMonthMapper.selectOne(new LambdaQueryWrapper<RaffleActivityAccountMonth>()
				.eq(RaffleActivityAccountMonth::getActivityId, activityId)
				.eq(RaffleActivityAccountMonth::getUserId, userId)
				.eq(RaffleActivityAccountMonth::getMonth, RaffleActivityAccountMonth.currentMonth()));

		// 查询日账户额度
		RaffleActivityAccountDay raffleActivityAccountDay = raffleActivityAccountDayMapper.selectOne(new LambdaQueryWrapper<RaffleActivityAccountDay>()
				.eq(RaffleActivityAccountDay::getActivityId, activityId)
				.eq(RaffleActivityAccountDay::getUserId, userId)
				.eq(RaffleActivityAccountDay::getDay, RaffleActivityAccountDay.currentDay()));

		ActivityAccountBO activityAccountBO = ActivityAccountBO.builder()
				.userId(userId)
				.activityId(activityId)
				.totalCount(raffleActivityAccount.getTotalCount())
				.totalCountSurplus(raffleActivityAccount.getTotalCountSurplus())
				.build();

		// 如果没有创建日账户，则从总账户中获取日总额度填充。「当新创建日账户时，会获得总账户额度」
		if (null == raffleActivityAccountDay) {
			activityAccountBO.setDayCount(raffleActivityAccount.getDayCount());
			activityAccountBO.setDayCountSurplus(raffleActivityAccount.getDayCount());
		} else {
			activityAccountBO.setDayCount(raffleActivityAccountDay.getDayCount());
			activityAccountBO.setDayCountSurplus(raffleActivityAccountDay.getDayCountSurplus());
		}

		// 如果没有创建月账户，则从总账户中获取月总额度填充。「当新创建日账户时，会获得总账户额度」
		if (null == raffleActivityAccountMonth) {
			activityAccountBO.setMonthCount(raffleActivityAccount.getMonthCount());
			activityAccountBO.setMonthCountSurplus(raffleActivityAccount.getMonthCount());
		} else {
			activityAccountBO.setMonthCount(raffleActivityAccountMonth.getMonthCount());
			activityAccountBO.setMonthCountSurplus(raffleActivityAccountMonth.getMonthCountSurplus());
		}


		return activityAccountBO;
	}

	@Override
	public UnpaidActivityOrderBO queryUnpaidActivityOrder(SkuRechargeBO skuRechargeBO) {
		RaffleActivityOrder raffleActivityOrder = activityOrderMapper.selectOne(new LambdaQueryWrapper<RaffleActivityOrder>()
				.eq(RaffleActivityOrder::getUserId, skuRechargeBO.getUserId())
				.eq(RaffleActivityOrder::getSku, skuRechargeBO.getSku())
				.eq(RaffleActivityOrder::getState, "wait_pay")
				.ge(RaffleActivityOrder::getOrderTime, LocalDateTime.now().minusMonths(1))
				.last("LIMIT 1"));
		if (raffleActivityOrder == null) {
			return null;
		}

		return BeanCopyUtil.copyProperties(raffleActivityOrder, UnpaidActivityOrderBO.class);
	}

	@Override
	public BigDecimal queryUserCreditAccountAmount(String userId) {
		UserCreditAccount userCreditAccount = userCreditAccountMapper.selectOne(new LambdaQueryWrapper<UserCreditAccount>()
				.eq(UserCreditAccount::getUserId, userId));
		if (userCreditAccount == null) {
			return BigDecimal.ZERO;
		}

		return userCreditAccount.getAvailableAmount();
	}

	/**
	 * 订单出货 - 积分充值
	 */
	@Override
	public void updateOrder(DeliveryOrderBO deliveryOrderBO) {
		RLock lock = redisService.getLock(RedisKey.ACTIVITY_ACCOUNT_UPDATE_LOCK + deliveryOrderBO.getUserId() + Constants.UNDERLINE + deliveryOrderBO.getOutBusinessNo());
		try {
			RaffleActivityOrder raffleActivityOrder = activityOrderMapper.selectOne(new LambdaQueryWrapper<RaffleActivityOrder>()
					.eq(RaffleActivityOrder::getUserId, deliveryOrderBO.getUserId())
					.eq(RaffleActivityOrder::getOutBusinessNo, deliveryOrderBO.getOutBusinessNo()));

			if (raffleActivityOrder == null) {
				return;
			}

			lock.lock(3, TimeUnit.SECONDS);

			// 账户对象 - 总
			RaffleActivityAccount raffleActivityAccount = BeanCopyUtil.copyProperties(raffleActivityOrder, RaffleActivityAccount.class);

			// 账户对象 - 月
			RaffleActivityAccountMonth raffleActivityAccountMonth = BeanCopyUtil.copyProperties(raffleActivityOrder, RaffleActivityAccountMonth.class);
			raffleActivityAccountMonth.setMonth(RaffleActivityAccountMonth.currentMonth());
			raffleActivityAccountMonth.setMonthCountSurplus(raffleActivityAccount.getMonthCount());

			// 账户对象 - 日
			RaffleActivityAccountDay raffleActivityAccountDay = BeanCopyUtil.copyProperties(raffleActivityAccount, RaffleActivityAccountDay.class);
			raffleActivityAccountDay.setDay(RaffleActivityAccountDay.currentDay());
			raffleActivityAccountDay.setDayCountSurplus(raffleActivityAccount.getDayCount());

			transactionTemplate.execute(status -> {
				try {
					// 更新订单
					int updateCount = activityOrderMapper.updateOrderCompleted(raffleActivityOrder);
					if (updateCount != 1) {
						status.setRollbackOnly();
						return 1;
					}

					// 更新账户 - 总
					Long count = raffleActivityAccountMapper.selectCount(new LambdaQueryWrapper<RaffleActivityAccount>()
							.eq(RaffleActivityAccount::getUserId, deliveryOrderBO.getUserId())
							.eq(RaffleActivityAccount::getActivityId, raffleActivityAccount.getActivityId()));
					if (count == 0L) {
						raffleActivityAccountMapper.insert(raffleActivityAccount);
					}else{
						raffleActivityAccountMapper.updateAccountQuota(raffleActivityAccount);
					}

					// 更新账户 - 月
					raffleActivityAccountMonthMapper.addAccountQuota(raffleActivityAccountMonth);
					// 更新账户 - 日
					raffleActivityAccountDayMapper.addAccountQuota(raffleActivityAccountDay);

					return 1;
				} catch (DuplicateKeyException e) {
					status.setRollbackOnly();
					log.error("更新订单记录，完成态，唯一索引冲突 userId: {} outBusinessNo: {}", deliveryOrderBO.getUserId(), deliveryOrderBO.getOutBusinessNo(), e);
					throw new ServiceException(BaseErrorCode.INDEX_DUP);
				}
			});
		} finally {
			if (lock.isLocked() && lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}

	}

	@Override
	public Integer queryRaffleActivityAccountPartakeCount(Long activityId, String userId) {
		RaffleActivityAccount raffleActivityAccount = activityAccountMapper.selectOne(new LambdaQueryWrapper<RaffleActivityAccount>()
				.eq(RaffleActivityAccount::getActivityId, activityId)
				.eq(RaffleActivityAccount::getUserId, userId));
		if (raffleActivityAccount == null){
			return 0;
		}

		return raffleActivityAccount.getTotalCount() - raffleActivityAccount.getTotalCountSurplus();
	}

}
