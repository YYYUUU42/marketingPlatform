package com.yu.market.server.raffle.service;

/**
 * @author yu
 * @description 策略装配，负责初始化策略计算
 * @date 2025-01-07
 */
public interface IStrategyArmory {

    /**
     * 装配抽奖策略配置「触发的时机可以为活动审核通过后进行调用」
     *
     * @param strategyId 策略ID
     * @return 装配结果
     */
    boolean assembleLotteryStrategy(Long strategyId);

}
