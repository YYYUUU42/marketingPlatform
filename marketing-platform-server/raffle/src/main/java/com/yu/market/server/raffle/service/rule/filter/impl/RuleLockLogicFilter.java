package com.yu.market.server.raffle.service.rule.filter.impl;

import com.yu.market.server.raffle.model.annotation.LogicStrategy;
import com.yu.market.server.raffle.model.bo.RuleMatterBO;
import com.yu.market.server.raffle.model.enums.RuleLogicCheckType;
import com.yu.market.server.raffle.repository.IStrategyRepository;
import com.yu.market.server.raffle.service.rule.ILogicFilter;
import com.yu.market.server.raffle.service.rule.filter.factory.DefaultLogicFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.yu.market.server.raffle.model.bo.RuleActionBO;

/**
 * @author yu
 * @description 用户抽奖n次后，对应奖品可解锁抽奖
 * @date 2025-01-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.RULE_LOCK)
public class RuleLockLogicFilter implements ILogicFilter<RuleActionBO.RaffleCenterBO> {
	
	private final IStrategyRepository repository;

	@Override
	public RuleActionBO<RuleActionBO.RaffleCenterBO> filter(RuleMatterBO ruleMatterBO) {
		log.info("规则过滤-次数锁 userId:{} strategyId:{} ruleModel:{}", ruleMatterBO.getUserId(), ruleMatterBO.getStrategyId(), ruleMatterBO.getRuleModel());

		String ruleValue = repository.queryStrategyRuleValue(ruleMatterBO.getStrategyId(), ruleMatterBO.getAwardId(), ruleMatterBO.getRuleModel());
		long raffleCount = Long.parseLong(ruleValue);

		//todo 用户抽奖次数
		long userRaffleCount = 0L;

		// 用户抽奖次数大于规则限定值，规则放行
		if (userRaffleCount >= raffleCount) {
			return RuleActionBO.<RuleActionBO.RaffleCenterBO>builder()
					.code(RuleLogicCheckType.ALLOW.getCode())
					.info(RuleLogicCheckType.ALLOW.getInfo())
					.build();
		}

		// 用户抽奖次数小于规则限定值，规则拦截
		return RuleActionBO.<RuleActionBO.RaffleCenterBO>builder()
				.code(RuleLogicCheckType.TAKE_OVER.getCode())
				.info(RuleLogicCheckType.TAKE_OVER.getInfo())
				.build();
	}
}
