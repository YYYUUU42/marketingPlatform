package com.yu.market.server.raffle.service.armory.impl;

import com.yu.market.server.raffle.repository.IStrategyRepository;
import com.yu.market.server.raffle.service.armory.IStrategyDispatch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class StrategyDispatchImpl implements IStrategyDispatch {

	private final IStrategyRepository repository;

	/**
	 * 获取抽奖策略装配的随机结果
	 */
	@Override
	public Integer getRandomAwardId(Long strategyId) {
		int rateRange = repository.getRateRange(String.valueOf(strategyId));

		return repository.getStrategyAwardAssemble(String.valueOf(strategyId), new SecureRandom().nextInt(rateRange));
	}

	/**
	 * 获取抽奖策略装配的随机结果 - 权重
	 */
	@Override
	public Integer getRandomAwardId(Long strategyId, String ruleWeightValue) {
		String key = String.valueOf(strategyId).concat("_").concat(ruleWeightValue);
		int rateRange = repository.getRateRange(key);

		return repository.getStrategyAwardAssemble(key, new SecureRandom().nextInt(rateRange));
	}
}
