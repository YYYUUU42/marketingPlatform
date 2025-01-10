package com.yu.market.server.raffle.service.raffle.impl;

import cn.hutool.core.util.StrUtil;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.server.raffle.model.bo.RaffleAwardBO;
import com.yu.market.server.raffle.model.bo.RaffleFactorBO;
import com.yu.market.server.raffle.model.bo.RuleActionBO;
import com.yu.market.server.raffle.model.bo.StrategyAwardRuleModelBO;
import com.yu.market.server.raffle.model.enums.RuleLogicCheckType;
import com.yu.market.server.raffle.repository.IStrategyRepository;
import com.yu.market.server.raffle.service.armory.IStrategyDispatch;
import com.yu.market.server.raffle.service.raffle.IRaffleStrategy;
import com.yu.market.server.raffle.service.rule.chain.ILogicChain;
import com.yu.market.server.raffle.service.rule.chain.factory.DefaultChainFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yu
 * @description 抽奖策略抽象类，定义抽奖的标准流程
 * @date 2025-01-09
 */
@Slf4j
public abstract class AbstractRaffleStrategy implements IRaffleStrategy {

	protected IStrategyRepository repository;
	protected IStrategyDispatch strategyDispatch;
	private final DefaultChainFactory defaultChainFactory;

	public AbstractRaffleStrategy(IStrategyRepository repository, IStrategyDispatch strategyDispatch, DefaultChainFactory defaultChainFactory) {
		this.repository = repository;
		this.strategyDispatch = strategyDispatch;
		this.defaultChainFactory = defaultChainFactory;
	}

	@Override
	public RaffleAwardBO performRaffle(RaffleFactorBO raffleFactorBO) {
		// 1. 参数校验
		Long strategyId = raffleFactorBO.getStrategyId();
		String userId = raffleFactorBO.getUserId();
		if (strategyId == null || StrUtil.isBlank(userId)) {
			throw new ServiceException(BaseErrorCode.ILLEGAL_PARAMETER);
		}

		// 获取抽奖责任链 - 前置规则的责任链处理
		ILogicChain logicChain = defaultChainFactory.openLogicChain(strategyId);

		// 通过责任链获得，奖品ID
		Integer awardId = logicChain.logic(userId, strategyId);

		// 查询奖品规则「抽奖中（拿到奖品ID时，过滤规则）、抽奖后（扣减完奖品库存后过滤，抽奖中拦截和无库存则走兜底）」
		StrategyAwardRuleModelBO strategyAwardRuleModelVO = repository.queryStrategyAwardRuleModelBO(strategyId, awardId);

		// 抽奖中 - 规则过滤
		RaffleFactorBO raffleFactor = RaffleFactorBO.builder()
				.userId(userId)
				.strategyId(strategyId)
				.awardId(awardId)
				.build();
		RuleActionBO<RuleActionBO.RaffleCenterBO> ruleActionCenterBO = this.doCheckRaffleCenterLogic(raffleFactor, strategyAwardRuleModelVO.raffleCenterRuleModelList());

		if (RuleLogicCheckType.TAKE_OVER.getCode().equals(ruleActionCenterBO.getCode())) {
			log.info("中奖中规则拦截，通过抽奖后规则 rule_luck_award 走兜底奖励。");
			return RaffleAwardBO.builder()
					.awardDesc("中奖中规则拦截，通过抽奖后规则 rule_luck_award 走兜底奖励。")
					.build();
		}

		return RaffleAwardBO.builder()
				.awardId(awardId)
				.build();
	}

	protected abstract RuleActionBO<RuleActionBO.RaffleCenterBO> doCheckRaffleCenterLogic(RaffleFactorBO raffleFactorBO, String... logics);

}
