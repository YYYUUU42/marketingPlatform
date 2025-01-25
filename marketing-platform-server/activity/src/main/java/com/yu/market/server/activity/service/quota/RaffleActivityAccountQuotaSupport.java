package com.yu.market.server.activity.service.quota;

import com.yu.market.server.activity.model.bo.ActivityBO;
import com.yu.market.server.activity.model.bo.ActivityCountBO;
import com.yu.market.server.activity.model.bo.ActivitySkuBO;
import com.yu.market.server.activity.respository.IActivityRepository;
import com.yu.market.server.activity.service.quota.rule.factory.DefaultActivityChainFactory;

/**
 * @author yu
 * @description 抽奖活动的支撑类
 * @date 2025-01-26
 */
public class RaffleActivityAccountQuotaSupport {
	protected DefaultActivityChainFactory defaultActivityChainFactory;

	protected IActivityRepository activityRepository;

	public RaffleActivityAccountQuotaSupport(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory) {
		this.activityRepository = activityRepository;
		this.defaultActivityChainFactory = defaultActivityChainFactory;
	}

	public ActivitySkuBO queryActivitySku(Long sku) {
		return activityRepository.queryActivitySku(sku);
	}

	public ActivityBO queryRaffleActivityByActivityId(Long activityId) {
		return activityRepository.queryRaffleActivityByActivityId(activityId);
	}

	public ActivityCountBO queryRaffleActivityCountByActivityCountId(Long activityCountId) {
		return activityRepository.queryRaffleActivityCountByActivityCountId(activityCountId);
	}
}
