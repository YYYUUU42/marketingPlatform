package com.yu.market.server.raffle.service.rule.chain.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.yu.market.common.contants.Constants;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.server.raffle.repository.IStrategyRepository;
import com.yu.market.server.raffle.service.armory.IStrategyDispatch;
import com.yu.market.server.raffle.service.rule.chain.AbstractLogicChain;
import com.yu.market.server.raffle.service.rule.chain.factory.DefaultChainFactory;
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
@Component("rule_weight")
@RequiredArgsConstructor
public class RuleWeightLogicChain extends AbstractLogicChain {

	private final IStrategyRepository repository;
	private final IStrategyDispatch strategyDispatch;


	/**
	 * 权重规则过滤；
	 * 1. 权重规则格式；4000:102,103,104,105 5000:102,103,104,105,106,107 6000:102,103,104,105,106,107,108,109
	 * 2. 解析数据格式；判断哪个范围符合用户的特定抽奖范围
	 */
	@Override
	public DefaultChainFactory.StrategyAward logic(String userId, Long strategyId) {
		String ruleModel = getRuleModel();
		log.info("抽奖责任链 - 权重 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModel);

		String ruleValue = repository.queryStrategyRuleValue(strategyId, ruleModel);

		// 根据用户ID 查询用户抽奖消耗的积分值
		Map<Long, String> analyticalValueGroup = getAnalyticalValue(ruleValue);
		if (CollectionUtil.isEmpty(analyticalValueGroup)) {
			return null;
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
			Integer awardId = strategyDispatch.getRandomAwardId(strategyId, analyticalValueGroup.get(value));
			log.info("抽奖责任链 - 权重接管 userId: {} strategyId: {} ruleModel: {} awardId: {}", userId, strategyId, ruleModel, awardId);
			return DefaultChainFactory.StrategyAward.builder()
					.awardId(awardId)
					.logicModel(getRuleModel())
					.build();
		}

		// 过滤其他责任链
		log.info("抽奖责任链-权重放行 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModel);

		return next().logic(userId, strategyId);
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

	@Override
	protected String getRuleModel() {
		return DefaultChainFactory.LogicModel.RULE_WEIGHT.getCode();
	}
}
