package com.yu.market.server.raffle.service.rule.chain.factory;

import com.yu.market.server.raffle.model.bo.StrategyBO;
import com.yu.market.server.raffle.repository.IStrategyRepository;
import com.yu.market.server.raffle.service.rule.chain.ILogicChain;
import lombok.*;
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
			return logicChainGroup.get(LogicModel.RULE_DEFAULT.getCode());
		}

		// 按照配置顺序装填用户配置的责任链；rule_blacklist、rule_weight
		ILogicChain logicChain = logicChainGroup.get(ruleModels[0]);
		ILogicChain current = logicChain;

		for (int i = 1; i < ruleModels.length; i++) {
			ILogicChain nextChain = logicChainGroup.get(ruleModels[i]);
			current = current.appendNext(nextChain);
		}

		// 责任链的最后装填默认责任链
		current.appendNext(logicChainGroup.get(LogicModel.RULE_DEFAULT.getCode()));

		return logicChain;
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class StrategyAward {
		/**
		 * 抽奖奖品ID - 内部流转使用
		 */
		private Integer awardId;

		/**
		 * 规则模型
		 */
		private String logicModel;

		/**
		 * 抽奖奖品规则
		 */
		private String awardRuleValue;
	}

	@Getter
	@AllArgsConstructor
	public enum LogicModel {

		RULE_DEFAULT("rule_default", "默认抽奖"),
		RULE_BLACKLIST("rule_blacklist", "黑名单抽奖"),
		RULE_WEIGHT("rule_weight", "权重规则"),
		;

		private final String code;
		private final String info;

	}
}
