package com.yu.market.server.raffle.service.raffle.impl;

import com.yu.market.common.exception.ServiceException;
import com.yu.market.server.raffle.model.bo.*;
import com.yu.market.server.raffle.repository.IStrategyRepository;
import com.yu.market.server.raffle.service.armory.IStrategyDispatch;
import com.yu.market.server.raffle.service.raffle.IRaffleAward;
import com.yu.market.server.raffle.service.raffle.IRaffleStock;
import com.yu.market.server.raffle.service.rule.chain.ILogicChain;
import com.yu.market.server.raffle.service.rule.chain.factory.DefaultChainFactory;
import com.yu.market.server.raffle.service.rule.tree.factory.DefaultTreeFactory;
import com.yu.market.server.raffle.service.rule.tree.factory.engine.IDecisionTreeEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author yu
 * @description 默认的抽奖策略实现
 * @date 2025-01-10
 */
@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy implements IRaffleStock, IRaffleAward {

	public DefaultRaffleStrategy(IStrategyRepository repository, IStrategyDispatch strategyDispatch, DefaultChainFactory defaultChainFactory, DefaultTreeFactory defaultTreeFactory) {
		super(repository, strategyDispatch, defaultChainFactory, defaultTreeFactory);
	}

	@Override
	public DefaultChainFactory.StrategyAward raffleLogicChain(String userId, Long strategyId) {
		ILogicChain logicChain = defaultChainFactory.openLogicChain(strategyId);
		return logicChain.logic(userId, strategyId);
	}

	@Override
	public DefaultTreeFactory.StrategyAward raffleLogicTree(String userId, Long strategyId, Integer awardId) {
		StrategyAwardRuleModelBO strategyAwardRuleModelVO = repository.queryStrategyAwardRuleModelBO(strategyId, awardId);
		if (strategyAwardRuleModelVO == null) {
			return DefaultTreeFactory.StrategyAward.builder().awardId(awardId).build();
		}

		RuleTreeBO ruleTreeVO = repository.queryRuleTreeBoByTreeId(strategyAwardRuleModelVO.getRuleModels());
		if (ruleTreeVO == null) {
			throw new ServiceException("存在抽奖策略配置的规则模型 Key，未在库表 rule_tree、rule_tree_node、rule_tree_line 配置对应的规则树信息 " + strategyAwardRuleModelVO.getRuleModels());
		}

		IDecisionTreeEngine treeEngine = defaultTreeFactory.createDecisionTreeEngine(ruleTreeVO);
		return treeEngine.process(userId, strategyId, awardId);
	}

	/**
	 * 获取奖品库存消耗队列
	 *
	 * @return 奖品库存Key信息
	 */
	@Override
	public StrategyAwardStockKeyBO takeQueueValue()  {
		return repository.takeQueueValue();
	}

	/**
	 * 更新奖品库存消耗记录
	 *
	 * @param strategyId 策略ID
	 * @param awardId    奖品ID
	 */
	@Override
	public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
		repository.updateStrategyAwardStock(strategyId, awardId);
	}

	/**
	 * 根据策略ID查询抽奖奖品列表配置
	 *
	 * @param strategyId 策略ID
	 * @return 奖品列表
	 */
	@Override
	public List<StrategyAwardBO> queryRaffleStrategyAwardList(Long strategyId) {
		return repository.queryStrategyAwardList(strategyId);
	}
}
