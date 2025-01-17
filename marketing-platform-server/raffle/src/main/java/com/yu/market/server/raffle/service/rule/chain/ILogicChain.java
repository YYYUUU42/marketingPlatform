package com.yu.market.server.raffle.service.rule.chain;

import com.yu.market.server.raffle.service.rule.chain.factory.DefaultChainFactory;

/**
 * @author yu
 * @description 抽奖策略规则责任链接口
 * @date 2025-01-10
 */
public interface ILogicChain extends ILogicChainArmory{

    /**
     * 责任链接口
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @return 奖品对象
     */
    DefaultChainFactory.StrategyAward logic(String userId, Long strategyId);

}
