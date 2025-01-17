package com.yu.market.server.raffle.service.rule.chain.impl;

import com.yu.market.common.contants.Constants;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.server.raffle.repository.IStrategyRepository;
import com.yu.market.server.raffle.service.rule.chain.AbstractLogicChain;
import com.yu.market.server.raffle.service.rule.chain.factory.DefaultChainFactory;
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
@Component("rule_blacklist")
@RequiredArgsConstructor
public class RuleBackListLogicChain extends AbstractLogicChain {

	private final IStrategyRepository repository;

	@Override
	public DefaultChainFactory.StrategyAward logic(String userId, Long strategyId) {
		String ruleModel = getRuleModel();
		log.info("抽奖责任链 - 黑名单 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModel);
		// 查询规则配置
		String ruleValue = repository.queryStrategyRuleValue(strategyId, ruleModel);

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
		String[] userBlackIds = splitRuleValue[1].split(Constants.SPLIT);

		// 黑名单过滤逻辑
		boolean isBlacklisted = Arrays.asList(userBlackIds).contains(userId);

		if (isBlacklisted) {
			return DefaultChainFactory.StrategyAward.builder()
					.awardId(awardId)
					.logicModel(getRuleModel())
					.build();
		}

		// 过滤其他责任链
		log.info("抽奖责任链-黑名单放行 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModel);

		return next().logic(userId, strategyId);
	}

	@Override
	protected String getRuleModel() {
		return DefaultChainFactory.LogicModel.RULE_BLACKLIST.getCode();
	}
}
