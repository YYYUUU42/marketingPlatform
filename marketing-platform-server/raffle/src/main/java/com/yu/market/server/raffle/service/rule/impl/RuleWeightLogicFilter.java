package com.yu.market.server.raffle.service.rule.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
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
 * @description 抽奖前规则 - 根据抽奖权重返回可抽奖范围KEY
 * @date 2025-01-09
 */
@Slf4j
@Component
@RequiredArgsConstructor
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.RULE_WIGHT)
public class RuleWeightLogicFilter implements ILogicFilter<RuleActionBO.RaffleBeforeBO> {

	private final IStrategyRepository repository;


	/**
	 * 权重规则过滤；
	 * 1. 权重规则格式；4000:102,103,104,105 5000:102,103,104,105,106,107 6000:102,103,104,105,106,107,108,109
	 * 2. 解析数据格式；判断哪个范围符合用户的特定抽奖范围
	 *
	 * @param ruleMatterBO 规则物料实体对象
	 * @return 规则过滤结果
	 */
	@Override
	public RuleActionBO<RuleActionBO.RaffleBeforeBO> filter(RuleMatterBO ruleMatterBO) {
		log.info("规则过滤-权重范围 userId:{} strategyId:{} ruleModel:{}", ruleMatterBO.getUserId(), ruleMatterBO.getStrategyId(), ruleMatterBO.getRuleModel());

		String userId = ruleMatterBO.getUserId();
		Long strategyId = ruleMatterBO.getStrategyId();
		String ruleValue = repository.queryStrategyRuleValue(ruleMatterBO.getStrategyId(), ruleMatterBO.getAwardId(), ruleMatterBO.getRuleModel());

		// 根据用户ID 查询用户抽奖消耗的积分值
		Map<Long, String> analyticalValueGroup = getAnalyticalValue(ruleValue);
		if (CollectionUtil.isEmpty(analyticalValueGroup)) {
			return RuleActionBO.<RuleActionBO.RaffleBeforeBO>builder()
					.code(RuleLogicCheckType.ALLOW.getCode())
					.info(RuleLogicCheckType.ALLOW.getInfo())
					.build();
		}

		// 查询用户使用了多少积分
		Integer userScore = repository.queryActivityAccountTotalUseCount(userId, strategyId);

		// 转换Keys值，并默认排序
		List<Long> analyticalSortedKeys = new ArrayList<>(analyticalValueGroup.keySet());
		Collections.sort(analyticalSortedKeys);

		// 找出最大符合的值
		Long value = analyticalSortedKeys.stream()
				.filter(key -> userScore >= key)
				.max(Long::compareTo)
				.orElse(null);

		if (value != null) {
			return RuleActionBO.<RuleActionBO.RaffleBeforeBO>builder()
					.data(RuleActionBO.RaffleBeforeBO.builder()
							.strategyId(strategyId)
							.ruleWeightValueKey(analyticalValueGroup.get(value))
							.build())
					.ruleModel(DefaultLogicFactory.LogicModel.RULE_WIGHT.getCode())
					.code(RuleLogicCheckType.TAKE_OVER.getCode())
					.info(RuleLogicCheckType.TAKE_OVER.getInfo())
					.build();
		}

		return RuleActionBO.<RuleActionBO.RaffleBeforeBO>builder()
				.code(RuleLogicCheckType.ALLOW.getCode())
				.info(RuleLogicCheckType.ALLOW.getInfo())
				.build();
	}

	/**
	 * 解析规则值，将其转换为键值对映射。
	 */
	private Map<Long, String> getAnalyticalValue(String ruleValue) {
		// 校验输入是否为空
		if (StrUtil.isBlank(ruleValue)) {
			return Collections.emptyMap();
		}

		String[] ruleValueGroups = ruleValue.split(Constants.SPACE);
		Map<Long, String> ruleValueMap = new HashMap<>();

		for (String ruleValueKey : ruleValueGroups) {
			// 检查输入是否为空
			if (ruleValueKey == null || ruleValueKey.isEmpty()) {
				return ruleValueMap;
			}

			// 分割字符串以获取键和值
			String[] parts = ruleValueKey.split(Constants.COLON);
			if (parts.length != 2) {
				log.error("规则值格式无效，ruleValueKey: {}", ruleValueKey);
				throw new ServiceException("无效的规则值格式: " + ruleValueKey);
			}

			ruleValueMap.put(Long.parseLong(parts[0]), ruleValueKey);
		}

		return ruleValueMap;
	}

}
