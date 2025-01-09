package com.yu.market.server.raffle.service.raffle.impl;

import cn.hutool.core.util.StrUtil;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.server.raffle.model.bo.RaffleAwardBO;
import com.yu.market.server.raffle.model.bo.RaffleFactorBO;
import com.yu.market.server.raffle.model.bo.RuleActionBO;
import com.yu.market.server.raffle.model.bo.StrategyBO;
import com.yu.market.server.raffle.model.enums.RuleLogicCheckType;
import com.yu.market.server.raffle.repository.IStrategyRepository;
import com.yu.market.server.raffle.service.armory.IStrategyDispatch;
import com.yu.market.server.raffle.service.raffle.IRaffleStrategy;
import com.yu.market.server.raffle.service.rule.factory.DefaultLogicFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yu
 * @description 抽奖策略抽象类，定义抽奖的标准流程
 * @date 2025-01-09
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractRaffleStrategy implements IRaffleStrategy {

	protected IStrategyRepository repository;
	protected IStrategyDispatch strategyDispatch;

	public AbstractRaffleStrategy(IStrategyRepository repository, IStrategyDispatch strategyDispatch) {
		this.repository = repository;
		this.strategyDispatch = strategyDispatch;
	}

	@Override
	public RaffleAwardBO performRaffle(RaffleFactorBO raffleFactorBO) {
		// 1. 参数校验
		Long strategyId = raffleFactorBO.getStrategyId();
		String userId = raffleFactorBO.getUserId();
		if (strategyId == null || StrUtil.isBlank(userId)) {
			throw new ServiceException(BaseErrorCode.ILLEGAL_PARAMETER);
		}

		// 策略查询
		StrategyBO strategy = repository.queryStrategyBOByStrategyId(strategyId);

		// 抽奖前 - 规则过滤
		RuleActionBO<RuleActionBO.RaffleBeforeBO> ruleActionBO = this.doCheckRaffleBeforeLogic(RaffleFactorBO.builder().userId(userId).strategyId(strategyId).build(), strategy.ruleModels());

		if (RuleLogicCheckType.TAKE_OVER.getCode().equals(ruleActionBO.getCode())) {
			if (DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode().equals(ruleActionBO.getRuleModel())) {
				// 黑名单返回固定的奖品ID
				return RaffleAwardBO.builder()
						.awardId(ruleActionBO.getData().getAwardId())
						.build();
			} else if (DefaultLogicFactory.LogicModel.RULE_WIGHT.getCode().equals(ruleActionBO.getRuleModel())) {
				// 权重根据返回的信息进行抽奖
				RuleActionBO.RaffleBeforeBO raffleBeforeBO = ruleActionBO.getData();
				String ruleWeightValueKey = raffleBeforeBO.getRuleWeightValueKey();
				Integer awardId = strategyDispatch.getRandomAwardId(strategyId, ruleWeightValueKey);
				return RaffleAwardBO.builder()
						.awardId(awardId)
						.build();
			}
		}

		// 4. 默认抽奖流程
		Integer awardId = strategyDispatch.getRandomAwardId(strategyId);

		return RaffleAwardBO.builder()
				.awardId(awardId)
				.build();
	}

	protected abstract RuleActionBO<RuleActionBO.RaffleBeforeBO> doCheckRaffleBeforeLogic(RaffleFactorBO raffleFactorBO, String... logics);

}
