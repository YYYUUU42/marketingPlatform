package com.yu.market.server.activity.service.armory;

import com.yu.market.common.contants.RedisKey;
import com.yu.market.server.activity.model.bo.ActivitySkuBO;
import com.yu.market.server.activity.respository.IActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author yu
 * @description 活动sku预热
 * @date 2025-01-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityArmory implements IActivityArmory, IActivityDispatch {

	private final IActivityRepository activityRepository;

	/**
	 * 根据活动ID 装配 sku
	 */
	@Override
	public boolean assembleActivitySkuByActivityId(Long activityId) {
		List<ActivitySkuBO> activitySkuBOList = activityRepository.queryActivitySkuListByActivityId(activityId);
		for (ActivitySkuBO activitySkuBO : activitySkuBOList) {
			cacheActivitySkuStockCount(activitySkuBO.getSku(), activitySkuBO.getStockCountSurplus());
			// 预热活动次数 - 查询时预热到缓存
			activityRepository.queryRaffleActivityCountByActivityCountId(activitySkuBO.getActivityCountId());
		}

		// 预热活动 - 查询时预热到缓存
		activityRepository.queryRaffleActivityByActivityId(activityId);

		return true;
	}

	/**
	 * 装配活动 sku
	 */
	@Override
	public boolean assembleActivitySku(Long sku) {
		// 预热活动sku库存
		ActivitySkuBO activitySkuBO = activityRepository.queryActivitySku(sku);
		cacheActivitySkuStockCount(sku, activitySkuBO.getStockCount());

		// 预热活动【查询时预热到缓存】
		activityRepository.queryRaffleActivityByActivityId(activitySkuBO.getActivityId());

		// 预热活动次数【查询时预热到缓存】
		activityRepository.queryRaffleActivityCountByActivityCountId(activitySkuBO.getActivityCountId());

		return true;
	}

	/**
	 *  缓存活动 sku 库存数量
	 */
	private void cacheActivitySkuStockCount(Long sku, Integer stockCount) {
		String cacheKey = RedisKey.ACTIVITY_SKU_STOCK_COUNT_KEY + sku;
		activityRepository.cacheActivitySkuStockCount(cacheKey, stockCount);
	}

	/**
	 * 根据策略ID和奖品ID，扣减奖品缓存库存
	 */
	@Override
	public boolean subtractionActivitySkuStock(Long sku, Date endDateTime) {
		String cacheKey = RedisKey.ACTIVITY_SKU_STOCK_COUNT_KEY + sku;
		return activityRepository.subtractionActivitySkuStock(sku, cacheKey, endDateTime);
	}

}
