package com.yu.market.server.raffle.service.raffle.impl;

import cn.hutool.core.util.StrUtil;
import com.yu.market.server.raffle.model.bo.RaffleFactorBO;
import com.yu.market.server.raffle.model.bo.RuleActionBO;
import com.yu.market.server.raffle.model.bo.RuleMatterBO;
import com.yu.market.server.raffle.model.enums.RuleLogicCheckType;
import com.yu.market.server.raffle.repository.IStrategyRepository;
import com.yu.market.server.raffle.service.armory.IStrategyDispatch;
import com.yu.market.server.raffle.service.rule.ILogicFilter;
import com.yu.market.server.raffle.service.rule.factory.DefaultLogicFactory;
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

	public DefaultRaffleStrategy(IStrategyRepository repository, IStrategyDispatch strategyDispatch) {
		super(repository, strategyDispatch);
	}

	@Override
	protected RuleActionBO<RuleActionBO.RaffleBeforeBO> doCheckRaffleBeforeLogic(RaffleFactorBO raffleFactorBO, String... logics) {
		// 获取逻辑过滤器映射表
		Map<String, ILogicFilter<RuleActionBO.RaffleBeforeBO>> logicFilterGroup = logicFactory.getLogicFilters();
		if (logicFilterGroup == null || logicFilterGroup.isEmpty()) {
			log.warn("规则过滤器映射表为空，无法执行抽奖前规则过滤");
			return RuleActionBO.<RuleActionBO.RaffleBeforeBO>builder()
					.code(RuleLogicCheckType.ALLOW.getCode())
					.info(RuleLogicCheckType.ALLOW.getInfo())
					.build();
		}

		// 黑名单规则优先过滤
		String blacklistRule = Arrays.stream(logics)
				.filter(rule -> rule.contains(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode()))
				.findFirst()
				.orElse(null);

		if (StrUtil.isBlank(blacklistRule)) {
			RuleActionBO<RuleActionBO.RaffleBeforeBO> blacklistResult = applyRuleFilter(raffleFactorBO, logicFilterGroup, DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode());
			if (!RuleLogicCheckType.ALLOW.getCode().equals(blacklistResult.getCode())) {
				log.info("黑名单规则过滤结果 userId: {} code: {} info: {}", raffleFactorBO.getUserId(), blacklistResult.getCode(), blacklistResult.getInfo());
				return blacklistResult;
			}
		}

		// 顺序过滤剩余规则
		List<String> otherRules = Arrays.stream(logics)
				.filter(rule -> !rule.equals(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode()))
				.toList();

		for (String ruleModel : otherRules) {
			RuleActionBO<RuleActionBO.RaffleBeforeBO> ruleResult = applyRuleFilter(
					raffleFactorBO, logicFilterGroup, ruleModel);
			log.info("抽奖前规则过滤 userId: {} ruleModel: {} code: {} info: {}",
					raffleFactorBO.getUserId(), ruleModel, ruleResult.getCode(), ruleResult.getInfo());
			if (!RuleLogicCheckType.ALLOW.getCode().equals(ruleResult.getCode())) {
				return ruleResult;
			}
		}

		// 所有规则放行
		return RuleActionBO.<RuleActionBO.RaffleBeforeBO>builder()
				.code(RuleLogicCheckType.ALLOW.getCode())
				.info(RuleLogicCheckType.ALLOW.getInfo())
				.build();
	}

	/**
	 * 应用单个规则过滤逻辑
	 */
	private RuleActionBO<RuleActionBO.RaffleBeforeBO> applyRuleFilter(RaffleFactorBO raffleFactorBO,
																	  Map<String, ILogicFilter<RuleActionBO.RaffleBeforeBO>> logicFilterGroup,
																	  String ruleModel) {

		// 获取对应的逻辑过滤器
		ILogicFilter<RuleActionBO.RaffleBeforeBO> logicFilter = logicFilterGroup.get(ruleModel);
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
