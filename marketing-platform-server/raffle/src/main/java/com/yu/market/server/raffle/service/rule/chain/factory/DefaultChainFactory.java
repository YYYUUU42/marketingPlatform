package com.yu.market.server.raffle.service.rule.chain.factory;

import com.yu.market.server.raffle.model.bo.StrategyBO;
import com.yu.market.server.raffle.repository.IStrategyRepository;
import com.yu.market.server.raffle.service.rule.chain.ILogicChain;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author yu
 * @description 责任链工厂
 * @date 2025-01-10
 */
@Service
public class DefaultChainFactory {

	private final Map<String, ILogicChain> logicChainGroup;
	protected IStrategyRepository repository;

	public DefaultChainFactory(Map<String, ILogicChain> logicChainGroup, IStrategyRepository repository) {
		this.logicChainGroup = logicChainGroup;
		this.repository = repository;
	}

	/**
	 * 通过策略ID，构建责任链
	 */
	public ILogicChain openLogicChain(Long strategyId) {
		StrategyBO strategy = repository.queryStrategyBOByStrategyId(strategyId);
		String[] ruleModels = strategy.ruleModels();

		// 如果未配置策略规则，则只装填一个默认责任链
		if (ruleModels == null || ruleModels.length == 0) {
			return logicChainGroup.get("default");
		}

		// 按照配置顺序装填用户配置的责任链；rule_blacklist、rule_weight
		ILogicChain logicChain = logicChainGroup.get(ruleModels[0]);
		ILogicChain current = logicChain;

		for (int i = 1; i < ruleModels.length; i++) {
			ILogicChain nextChain = logicChainGroup.get(ruleModels[i]);
			current = current.appendNext(nextChain);
		}

		// 责任链的最后装填默认责任链
		current.appendNext(logicChainGroup.get("default"));

		return logicChain;
	}
}
