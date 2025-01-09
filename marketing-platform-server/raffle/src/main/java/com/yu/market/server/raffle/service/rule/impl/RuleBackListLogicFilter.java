package com.yu.market.server.raffle.service.rule.impl;

import com.yu.market.common.contants.Constants;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.server.raffle.model.annotation.LogicStrategy;
import com.yu.market.server.raffle.model.bo.RuleActionBO;
import com.yu.market.server.raffle.model.bo.RuleMatterBO;
import com.yu.market.server.raffle.model.enums.RuleLogicCheckType;
import com.yu.market.server.raffle.repository.IStrategyRepository;
import com.yu.market.server.raffle.service.rule.ILogicFilter;
import com.yu.market.server.raffle.service.rule.factory.DefaultLogicFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author yu
 * @description 抽奖前规则 - 黑名单用户过滤规则
 * @date 2025-01-09
 */
@Slf4j
@Component
@RequiredArgsConstructor
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.RULE_BLACKLIST)
public class RuleBackListLogicFilter implements ILogicFilter<RuleActionBO.RaffleBeforeBO> {

	private final IStrategyRepository repository;

	@Override
	public RuleActionBO<RuleActionBO.RaffleBeforeBO> filter(RuleMatterBO ruleMatterBO) {
		log.info("规则过滤-黑名单 userId:{} strategyId:{} ruleModel:{}", ruleMatterBO.getUserId(), ruleMatterBO.getStrategyId(), ruleMatterBO.getRuleModel());

		// 查询规则配置
		String ruleValue = repository.queryStrategyRuleValue(ruleMatterBO.getStrategyId(), ruleMatterBO.getAwardId(), ruleMatterBO.getRuleModel());

		// 校验规则值是否为空
		if (ruleValue == null || !ruleValue.contains(Constants.COLON)) {
			log.error("规则值格式错误，ruleValue: {}", ruleValue);
			throw new ServiceException("规则值格式错误，无法解析");
		}

		// 解析规则值，格式为 "101:user001,user002,user003"
		String[] splitRuleValue = ruleValue.split(Constants.COLON);
		if (splitRuleValue.length != 2) {
			log.error("规则值格式不符合预期，ruleValue: {}", ruleValue);
			throw new ServiceException("规则值格式不符合预期");
		}

		Integer awardId = Integer.parseInt(splitRuleValue[0]);

		// 过滤其他规则
		String userId = ruleMatterBO.getUserId();
		String[] userBlackIds = splitRuleValue[1].split(Constants.SPLIT);

		// 黑名单过滤逻辑
		boolean isBlacklisted = Arrays.asList(userBlackIds).contains(userId);

		if (isBlacklisted) {
			// 命中黑名单
			return RuleActionBO.<RuleActionBO.RaffleBeforeBO>builder()
					.ruleModel(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode())
					.data(RuleActionBO.RaffleBeforeBO.builder()
							.strategyId(ruleMatterBO.getStrategyId())
							.awardId(awardId)
							.build())
					.code(RuleLogicCheckType.TAKE_OVER.getCode())
					.info(RuleLogicCheckType.TAKE_OVER.getInfo())
					.build();
		}

		// 如果未命中黑名单，返回允许结果
		return RuleActionBO.<RuleActionBO.RaffleBeforeBO>builder()
				.code(RuleLogicCheckType.ALLOW.getCode())
				.info(RuleLogicCheckType.ALLOW.getInfo())
				.build();
	}
}
