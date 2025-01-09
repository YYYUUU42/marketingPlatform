package com.yu.market.server.raffle.service.armory;

/**
 * @author yu
 * @description 策略抽奖调度
 * @date 2025-01-09
 */
public interface IStrategyDispatch {
	/**
	 * 获取抽奖策略装配的随机结果
	 */
	Integer getRandomAwardId(Long strategyId);


	/**
	 * 获取抽奖策略装配的随机结果 - 权重

	 */
	Integer getRandomAwardId(Long strategyId, String ruleWeightValue);
}
