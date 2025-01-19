package com.yu.market.server.activity.respository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yu.market.common.contants.RedisKey;
import com.yu.market.common.redis.IRedisService;
import com.yu.market.common.utils.BeanCopyUtil;
import com.yu.market.server.activity.mapper.RaffleActivityCountMapper;
import com.yu.market.server.activity.mapper.RaffleActivityMapper;
import com.yu.market.server.activity.mapper.RaffleActivitySkuMapper;
import com.yu.market.server.activity.model.bo.ActivityBO;
import com.yu.market.server.activity.model.bo.ActivityCountBO;
import com.yu.market.server.activity.model.bo.ActivitySkuBO;
import com.yu.market.server.activity.model.pojo.RaffleActivity;
import com.yu.market.server.activity.model.pojo.RaffleActivityCount;
import com.yu.market.server.activity.model.pojo.RaffleActivitySku;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * @author yu
 * @description 活动仓储服务
 * @date 2025-01-19
 */
@Repository
@RequiredArgsConstructor
public class ActivityRepository implements IActivityRepository {

	private final IRedisService redisService;
	private final RaffleActivityMapper activityMapper;
	private final RaffleActivitySkuMapper activitySkuMapper;
	private final RaffleActivityCountMapper activityCountMapper;


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
}
