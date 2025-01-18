package com.yu.market.server.raffle.service.rule.tree;

import com.yu.market.server.raffle.service.rule.tree.factory.DefaultTreeFactory;

/**
 * @author yu
 * @description 规则树接口
 * @date 2025-01-15
 */
public interface ILogicTreeNode {

	DefaultTreeFactory.TreeActionBO logic(String userId, Long strategyId, Integer awardId, String ruleValue);

}
