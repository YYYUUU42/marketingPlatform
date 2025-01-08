package com.yu.market.server.raffle.repository;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yu.market.common.contants.RedisKey;
import com.yu.market.common.redis.IRedisService;
import com.yu.market.server.raffle.mapper.StrategyAwardMapper;
import com.yu.market.server.raffle.mapper.StrategyMapper;
import com.yu.market.server.raffle.mapper.StrategyRuleMapper;
import com.yu.market.server.raffle.model.bo.StrategyAwardBO;
import com.yu.market.server.raffle.model.bo.StrategyBO;
import com.yu.market.server.raffle.model.bo.StrategyRuleBO;
import com.yu.market.server.raffle.model.pojo.Strategy;
import com.yu.market.server.raffle.model.pojo.StrategyAward;
import com.yu.market.server.raffle.model.pojo.StrategyRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author yu
 * @description 策略服务仓储实现类
 * @date 2025-01-07
 */
@Repository
@RequiredArgsConstructor
public class StrategyRepository implements IStrategyRepository {

	private final StrategyMapper strategyMapper;
	private final StrategyAwardMapper strategyAwardMapper;
	private final StrategyRuleMapper strategyRuleMapper;
	private final IRedisService redisService;

	/**
	 * 查询策略奖项列表（优先缓存，次选数据库）
	 */
	@Override
	public List<StrategyAwardBO> queryStrategyAwardList(Long strategyId) {
		// 优先缓存
		String cacheKey = RedisKey.STRATEGY_AWARD_KEY + strategyId;
		List<StrategyAwardBO> strategyAwardBoList = redisService.getValue(cacheKey);
		if (!CollectionUtil.isEmpty(strategyAwardBoList)) {
			return strategyAwardBoList;
		}

		// 从数据库中取出数据
		List<StrategyAward> strategyAwardList = strategyAwardMapper.selectList(new LambdaQueryWrapper<StrategyAward>()
				.eq(StrategyAward::getStrategyId, strategyId));
		if (CollectionUtil.isEmpty(strategyAwardList)) {
			return List.of();
		}

		// 将数据库对象转换为业务实体
		strategyAwardBoList = strategyAwardList.stream()
				.map(strategyAward -> StrategyAwardBO.builder()
						.strategyId(strategyAward.getStrategyId())
						.awardId(strategyAward.getAwardId())
						.awardCount(strategyAward.getAwardCount())
						.awardCountSurplus(strategyAward.getAwardCountSurplus())
						.awardRate(strategyAward.getAwardRate())
						.build())
				.toList();

		redisService.setValue(cacheKey, strategyAwardBoList);

		return strategyAwardBoList;
	}

	/**
	 * 将与某个策略（strategyId）相关的概率查找表和抽奖范围值存储到缓存中，供后续抽奖逻辑使用
	 */
	@Override
	public void storeStrategyAwardSearchRateTable(String cacheKey, Integer rateRange, Map<Integer, Integer> strategyAwardSearchRateTable) {
		// 存储抽奖策略范围值，如10000，用于生成1000以内的随机数
		redisService.setValue(RedisKey.STRATEGY_RATE_RANGE_KEY + cacheKey, rateRange);

		// 存储概率查找表
		Map<Integer, Integer> cacheRateTable = redisService.getMap(RedisKey.STRATEGY_RATE_TABLE_KEY + cacheKey);
		cacheRateTable.putAll(strategyAwardSearchRateTable);
	}

	/**
	 * 根据策略 ID 和随机数（rateKey），从缓存中获取对应的奖项 ID
	 */
	@Override
	public Integer getStrategyAwardAssemble(String cacheKey, Integer rateKey) {
		return redisService.getFromMap(RedisKey.STRATEGY_RATE_TABLE_KEY + cacheKey, rateKey);
	}

	/**
	 * 获取某个策略（strategyId）的抽奖范围值（rateRange）
	 */
	@Override
	public int getRateRange(String cacheKey) {
		return redisService.getValue(RedisKey.STRATEGY_RATE_RANGE_KEY + cacheKey);
	}

	/**
	 * 查询抽奖策略
	 */
	@Override
	public StrategyBO queryStrategyBOByStrategyId(Long strategyId) {
		// 优先缓存
		String cacheKey = RedisKey.STRATEGY_KEY + strategyId;
		StrategyBO strategyBO = redisService.getValue(cacheKey);
		if (strategyBO != null) {
			return strategyBO;
		}

		// 查询数据库
		Strategy strategy = strategyMapper.selectOne(new LambdaQueryWrapper<Strategy>()
				.eq(Strategy::getStrategyId, strategyId));
		strategyBO = StrategyBO.builder()
				.strategyId(strategy.getStrategyId())
				.strategyDesc(strategy.getStrategyDesc())
				.ruleModels(strategy.getRuleModels())
				.build();

		redisService.setValue(cacheKey, strategyBO);

		return strategyBO;
	}

	/**
	 * 根据策略id和规则模型得到抽奖策略规则
	 */
	@Override
	public StrategyRuleBO queryStrategyRule(Long strategyId, String ruleModel) {
		StrategyRule strategyRule = strategyRuleMapper.selectOne(new LambdaQueryWrapper<StrategyRule>()
				.eq(StrategyRule::getStrategyId, strategyId)
				.eq(StrategyRule::getRuleModel, ruleModel));

		return StrategyRuleBO.builder()
				.strategyId(strategyRule.getStrategyId())
				.awardId(strategyRule.getAwardId())
				.ruleType(strategyRule.getRuleType())
				.ruleModel(strategyRule.getRuleModel())
				.ruleValue(strategyRule.getRuleValue())
				.ruleDesc(strategyRule.getRuleDesc())
				.build();
	}
}
