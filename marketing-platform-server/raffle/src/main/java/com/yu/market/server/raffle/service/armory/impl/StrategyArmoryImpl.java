package com.yu.market.server.raffle.service.armory.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.yu.market.common.contants.Constants;
import com.yu.market.common.contants.RedisKey;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.server.raffle.model.bo.StrategyAwardBO;
import com.yu.market.server.raffle.model.bo.StrategyBO;
import com.yu.market.server.raffle.model.bo.StrategyRuleBO;
import com.yu.market.server.raffle.repository.StrategyRepository;
import com.yu.market.server.raffle.service.armory.IStrategyArmory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.*;
import java.util.*;

import static com.yu.market.common.exception.errorCode.BaseErrorCode.STRATEGY_RULE_WEIGHT_IS_NULL;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyArmoryImpl implements IStrategyArmory {

	private final StrategyRepository repository;

	/**
	 * 装配抽奖策略配置，触发的时机为活动审核通过后进行调用
	 *
	 * @param strategyId 策略ID
	 * @return 装配结果
	 */
	@Override
	public boolean assembleLotteryStrategy(Long strategyId) {
		// 查询策略配置
		List<StrategyAwardBO> strategyAwardBOList = repository.queryStrategyAwardList(strategyId);
		if (CollectionUtil.isEmpty(strategyAwardBOList)) {
			log.warn("策略配置为空，无法装配抽奖策略，策略ID: {}", strategyId);
			return false;
		}

		// 缓存奖品库存 - 用于 Redis 扣减库存
		for (StrategyAwardBO strategyAward : strategyAwardBOList) {
			Integer awardId = strategyAward.getAwardId();
			Integer awardCount = strategyAward.getAwardCount();
			cacheStrategyAwardCount(strategyId, awardId, awardCount);
		}

		// 默认装配配置 - 全量抽奖概率
		assembleLotteryStrategy(String.valueOf(strategyId), strategyAwardBOList);

		// 查询权重策略配置
		StrategyBO strategyBO = repository.queryStrategyBOByStrategyId(strategyId);
		String ruleWeight = strategyBO.getRuleWeight();
		if (StrUtil.isBlank(ruleWeight)) {
			log.info("策略没有配置权重规则，直接跳过权重装配，策略ID: {}", strategyId);
			return true;
		}

		// 查询策略规则中 rule_weight 权重规则
		StrategyRuleBO strategyRuleBO = repository.queryStrategyRule(strategyId, ruleWeight);
		if (strategyRuleBO == null) {
			log.warn("策略规则中 rule_weight 权重规则已适用但未配置 - 策略ID: {}, 权重规则: {}", strategyId, ruleWeight);
			throw new ServiceException(STRATEGY_RULE_WEIGHT_IS_NULL);
		}

		// 按照权重规则装配策略
		Map<String, List<Integer>> ruleWeightValueMap = strategyRuleBO.getRuleWeightValues();
		ruleWeightValueMap.forEach((key, ruleWeightValues) -> {
			List<StrategyAwardBO> strategyAwardBOS = strategyAwardBOList.stream()
					.filter(strategyAward -> ruleWeightValues.contains(strategyAward.getAwardId()))
					.toList();

			assembleLotteryStrategy(String.valueOf(strategyId).concat(Constants.UNDERLINE).concat(key), strategyAwardBOS);
		});

		return true;
	}

	/**
	 * 装配抽奖策略并存储到 Redis
	 */
	private void assembleLotteryStrategy(String cacheKey, List<StrategyAwardBO> strategyAwardBOList) {
		// 获取最小概率值
		BigDecimal minAwardRate = strategyAwardBOList.stream()
				.map(StrategyAwardBO::getAwardRate)
				.min(BigDecimal::compareTo)
				.orElse(BigDecimal.ZERO);
		// 获取概率总值
		BigDecimal totalAwardRate = strategyAwardBOList.stream()
				.map(StrategyAwardBO::getAwardRate)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		// 计算概率范围，将总的概率值（totalAwardRate）除以最小的概率值（minAwardRate），保留 0 位小数（即结果为整数），并向上取整
		BigDecimal rateRange = totalAwardRate.divide(minAwardRate, 0, RoundingMode.CEILING);

		// 生成策略奖品查找表
		List<Integer> strategyAwardSearchRateTable  = new ArrayList<>(strategyAwardBOList.stream()
				.flatMap(strategyAwardBO -> {
					Integer awardId = strategyAwardBO.getAwardId();
					BigDecimal awardRate = strategyAwardBO.getAwardRate();

					// 计算每个奖品在查找表中的占位数量
					int occupyCount = rateRange.multiply(awardRate).setScale(0, RoundingMode.CEILING).intValue();

					// 使用 stream 将奖品id 按占位数填充
					return Collections.nCopies(occupyCount, awardId).stream();
				}).toList());

		// 对查找表进行乱序操作
		Collections.shuffle(strategyAwardSearchRateTable);

		// 构建概率查找表的 Map	key 为查找表的索引，value 为奖品id
		Map<Integer, Integer> shuffleStrategyAwardSearchRateTable = new LinkedHashMap<>();
		for (int i = 0; i < strategyAwardSearchRateTable.size(); i++) {
			shuffleStrategyAwardSearchRateTable.put(i, strategyAwardSearchRateTable.get(i));
		}

		// 存到 Redis 中
		repository.storeStrategyAwardSearchRateTable(cacheKey, shuffleStrategyAwardSearchRateTable.size(), shuffleStrategyAwardSearchRateTable);
	}

	/**
	 * 缓存奖品库存到Redis
	 *
	 * @param strategyId 策略ID
	 * @param awardId    奖品ID
	 * @param awardCount 奖品库存
	 */
	private void cacheStrategyAwardCount(Long strategyId, Integer awardId, Integer awardCount) {
		String cacheKey = RedisKey.STRATEGY_AWARD_COUNT_KEY + strategyId + Constants.UNDERLINE + awardId;
		repository.cacheStrategyAwardCount(cacheKey, awardCount);
	}
}
