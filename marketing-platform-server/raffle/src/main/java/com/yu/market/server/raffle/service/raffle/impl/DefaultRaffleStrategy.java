package com.yu.market.server.raffle.service.raffle.impl;

import com.yu.market.server.raffle.model.bo.RaffleFactorBO;
import com.yu.market.server.raffle.model.bo.RuleActionBO;
import com.yu.market.server.raffle.model.bo.RuleMatterBO;
import com.yu.market.server.raffle.model.enums.RuleLogicCheckType;
import com.yu.market.server.raffle.repository.IStrategyRepository;
import com.yu.market.server.raffle.service.armory.IStrategyDispatch;
import com.yu.market.server.raffle.service.rule.ILogicFilter;
import com.yu.market.server.raffle.service.rule.chain.factory.DefaultChainFactory;
import com.yu.market.server.raffle.service.rule.filter.factory.DefaultLogicFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author yu
 * @description 默认的抽奖策略实现
 * @date 2025-01-10
 */
@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy {

	@Resource
	private DefaultLogicFactory logicFactory;

	public DefaultRaffleStrategy(IStrategyRepository repository, IStrategyDispatch strategyDispatch, DefaultChainFactory defaultChainFactory) {
		super(repository, strategyDispatch, defaultChainFactory);
	}

	@Override
	protected RuleActionBO<RuleActionBO.RaffleCenterBO> doCheckRaffleCenterLogic(RaffleFactorBO raffleFactorBO, String... logics) {
		if (logics == null || logics.length == 0) {
			return RuleActionBO.<RuleActionBO.RaffleCenterBO>builder()
					.code(RuleLogicCheckType.ALLOW.getCode())
					.info(RuleLogicCheckType.ALLOW.getInfo())
					.build();
		}

		Map<String, ILogicFilter<RuleActionBO.RaffleCenterBO>> logicFilterGroup = logicFactory.getLogicFilters();

		// 顺序过滤规则
		for (String ruleModel : logics) {
			RuleActionBO<RuleActionBO.RaffleCenterBO> ruleResult = applyRuleFilter(
					raffleFactorBO, logicFilterGroup, ruleModel);
			log.info("抽奖中规则过滤 userId: {} ruleModel: {} code: {} info: {}",
					raffleFactorBO.getUserId(), ruleModel, ruleResult.getCode(), ruleResult.getInfo());
			if (!RuleLogicCheckType.ALLOW.getCode().equals(ruleResult.getCode())) {
				return ruleResult;
			}
		}

		// 所有规则放行
		return RuleActionBO.<RuleActionBO.RaffleCenterBO>builder()
				.code(RuleLogicCheckType.ALLOW.getCode())
				.info(RuleLogicCheckType.ALLOW.getInfo())
				.build();
	}


	/**
	 * 应用单个规则过滤逻辑
	 */
	private <T extends RuleActionBO.RaffleBO> RuleActionBO<T> applyRuleFilter(
			RaffleFactorBO raffleFactorBO,
			Map<String, ILogicFilter<T>> logicFilterGroup,
			String ruleModel) {

		// 获取对应的逻辑过滤器
		ILogicFilter<T> logicFilter = logicFilterGroup.get(ruleModel);
		if (logicFilter == null) {
			log.warn("未找到对应的逻辑过滤器，ruleModel: {}", ruleModel);
			return RuleActionBO.<T>builder()
					.code(RuleLogicCheckType.ALLOW.getCode())
					.info(RuleLogicCheckType.ALLOW.getInfo())
					.build();
		}

		// 构建规则实体
		RuleMatterBO ruleMatter = RuleMatterBO.builder()
				.userId(raffleFactorBO.getUserId())
				.awardId(null)
				.strategyId(raffleFactorBO.getStrategyId())
				.ruleModel(ruleModel)
				.build();

		// 执行规则过滤
		return logicFilter.filter(ruleMatter);
	}
}
