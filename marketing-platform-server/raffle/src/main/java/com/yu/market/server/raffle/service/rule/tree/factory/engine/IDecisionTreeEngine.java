package com.yu.market.server.raffle.service.rule.tree.factory.engine;


import com.yu.market.server.raffle.service.rule.tree.factory.DefaultTreeFactory;

/**
 * @author yu
 * @description 规则树组合接口
 * @date 2025-01-15
 */
public interface IDecisionTreeEngine {

    DefaultTreeFactory.StrategyAwardData process(String userId, Long strategyId, Integer awardId);

}
