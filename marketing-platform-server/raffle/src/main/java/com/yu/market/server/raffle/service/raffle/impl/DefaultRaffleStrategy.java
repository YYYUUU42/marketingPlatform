package com.yu.market.server.raffle.service.raffle.impl;

import com.yu.market.common.exception.ServiceException;
import com.yu.market.server.raffle.model.bo.*;
import com.yu.market.server.raffle.model.vo.RuleWeightVO;
import com.yu.market.server.raffle.repository.IStrategyRepository;
import com.yu.market.server.raffle.service.armory.IStrategyDispatch;
import com.yu.market.server.raffle.service.raffle.IRaffleAward;
import com.yu.market.server.raffle.service.raffle.IRaffleRule;
import com.yu.market.server.raffle.service.raffle.IRaffleStock;
import com.yu.market.server.raffle.service.rule.chain.ILogicChain;
import com.yu.market.server.raffle.service.rule.chain.factory.DefaultChainFactory;
import com.yu.market.server.raffle.service.rule.tree.factory.DefaultTreeFactory;
import com.yu.market.server.raffle.service.rule.tree.factory.engine.IDecisionTreeEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


/**
 * @author yu
 * @description 默认的抽奖策略实现
 * @date 2025-01-10
 */
@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy implements IRaffleStock, IRaffleAward, IRaffleRule {

	public DefaultRaffleStrategy(IStrategyRepository strategyRepository, IStrategyDispatch strategyDispatch, DefaultChainFactory defaultChainFactory, DefaultTreeFactory defaultTreeFactory) {
		super(strategyRepository, strategyDispatch, defaultChainFactory, defaultTreeFactory);
	}

	@Override
	public DefaultChainFactory.StrategyAward raffleLogicChain(String userId, Long strategyId) {
		log.info("抽奖策略-责任链 userId:{} strategyId:{}", userId, strategyId);
		ILogicChain logicChain = defaultChainFactory.openLogicChain(strategyId);
		return logicChain.logic(userId, strategyId);
	}

	@Override
	public DefaultTreeFactory.StrategyAward raffleLogicTree(String userId, Long strategyId, Integer awardId) {
		StrategyAwardRuleModelBO strategyAwardRuleModelVO = strategyRepository.queryStrategyAwardRuleModelBO(strategyId, awardId);
		if (strategyAwardRuleModelVO == null) {
			return DefaultTreeFactory.StrategyAward.builder().awardId(awardId).build();
		}

		RuleTreeBO ruleTreeVO = strategyRepository.queryRuleTreeBoByTreeId(strategyAwardRuleModelVO.getRuleModels());
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
		return strategyRepository.takeQueueValue();
	}

	/**
	 * 更新奖品库存消耗记录
	 *
	 * @param strategyId 策略ID
	 * @param awardId    奖品ID
	 */
	@Override
	public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
		strategyRepository.updateStrategyAwardStock(strategyId, awardId);
	}

	/**
	 * 根据策略ID查询抽奖奖品列表配置
	 *
	 * @param strategyId 策略ID
	 * @return 奖品列表
	 */
	@Override
	public List<StrategyAwardBO> queryRaffleStrategyAwardList(Long strategyId) {
		return strategyRepository.queryStrategyAwardList(strategyId);
	}

	/**
	 * 根据活动ID查询抽奖奖品列表配置
	 *
	 * @param activityId 活动ID
	 * @return 奖品列表
	 */
	@Override
	public List<StrategyAwardBO> queryRaffleStrategyAwardListByActivityId(Long activityId) {
		Long strategyId = strategyRepository.queryStrategyIdByActivityId(activityId);
		return queryRaffleStrategyAwardList(strategyId);
	}

	/**
	 * 查询有效活动的奖品配置
	 *
	 * @return 奖品配置列表
	 */
	@Override
	public List<StrategyAwardStockKeyBO> queryOpenActivityStrategyAwardList() {
		return strategyRepository.queryOpenActivityStrategyAwardList();
	}

	/**
	 * 根据规则树ID集合查询奖品中加锁数量的配置「部分奖品需要抽奖N次解锁」
	 *
	 * @param treeIds 规则树ID值
	 * @return key 规则树，value rule_lock 加锁值
	 */
	@Override
	public Map<String, Integer> queryAwardRuleLockCount(String[] treeIds) {
		return strategyRepository.queryAwardRuleLockCount(treeIds);
	}

	/**
	 * 查询奖品权重配置
	 *
	 * @param strategyId 策略ID
	 * @return 权重规则
	 */
	@Override
	public List<RuleWeightVO> queryAwardRuleWeight(Long strategyId) {
		return strategyRepository.queryAwardRuleWeight(strategyId);
	}

	/**
	 * 查询奖品权重配置
	 *
	 * @param activityId 活动ID
	 * @return 权重规则
	 */
	@Override
	public List<RuleWeightVO> queryAwardRuleWeightByActivityId(Long activityId) {
		Long strategyId = strategyRepository.queryStrategyIdByActivityId(activityId);
		return queryAwardRuleWeight(strategyId);
	}
}
