package com.yu.market.server.raffle.service.raffle.impl;

import cn.hutool.core.util.StrUtil;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.server.raffle.model.bo.RaffleAwardBO;
import com.yu.market.server.raffle.model.bo.RaffleFactorBO;
import com.yu.market.server.raffle.model.bo.StrategyAwardBO;
import com.yu.market.server.raffle.repository.IStrategyRepository;
import com.yu.market.server.raffle.service.armory.IStrategyDispatch;
import com.yu.market.server.raffle.service.raffle.IRaffleStrategy;
import com.yu.market.server.raffle.service.rule.chain.factory.DefaultChainFactory;
import com.yu.market.server.raffle.service.rule.tree.factory.DefaultTreeFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yu
 * @description 抽奖策略抽象类，定义抽奖的标准流程
 * @date 2025-01-09
 */
@Slf4j
public abstract class AbstractRaffleStrategy implements IRaffleStrategy {

	/**
	 * 策略仓储服务 -> 仓储层提供数据
	 */
	protected IStrategyRepository strategyRepository;

	/**
	 * 策略调度服务 -> 只负责抽奖处理，通过新增接口的方式，隔离职责，不需要使用方关心或者调用抽奖的初始化
	 */
	protected IStrategyDispatch strategyDispatch;

	/**
	 * 抽奖的责任链 -> 从抽奖的规则中，解耦出前置规则为责任链处理
	 */
	protected final DefaultChainFactory defaultChainFactory;

	/**
	 * 抽奖的决策树 -> 负责抽奖中到抽奖后的规则过滤，如抽奖到A奖品ID，之后要做次数的判断和库存的扣减等
	 */
	protected final DefaultTreeFactory defaultTreeFactory;

	public AbstractRaffleStrategy(IStrategyRepository strategyRepository, IStrategyDispatch strategyDispatch, DefaultChainFactory defaultChainFactory, DefaultTreeFactory defaultTreeFactory) {
		this.strategyRepository = strategyRepository;
		this.strategyDispatch = strategyDispatch;
		this.defaultChainFactory = defaultChainFactory;
		this.defaultTreeFactory = defaultTreeFactory;
	}

	@Override
	public RaffleAwardBO performRaffle(RaffleFactorBO raffleFactorBO) {
		// 参数校验
		Long strategyId = raffleFactorBO.getStrategyId();
		String userId = raffleFactorBO.getUserId();
		if (strategyId == null || StrUtil.isBlank(userId)) {
			throw new ServiceException(BaseErrorCode.ILLEGAL_PARAMETER);
		}

		// 责任链抽奖计算 - 这步拿到的是初步的抽奖ID，之后需要根据ID处理抽奖
		// 黑名单、权重等非默认抽奖的直接返回抽奖结果
		DefaultChainFactory.StrategyAward chainStrategyAwardVO = raffleLogicChain(userId, strategyId);
		log.info("抽奖策略计算-责任链 {} {} {} {}", userId, strategyId, chainStrategyAwardVO.getAwardId(), chainStrategyAwardVO.getLogicModel());
		if (!chainStrategyAwardVO.getLogicModel().equals(DefaultChainFactory.LogicModel.RULE_DEFAULT.getCode())) {
			return buildRaffleAwardBO(strategyId, chainStrategyAwardVO.getAwardId(), chainStrategyAwardVO.getAwardRuleValue());
		}

		// 规则树抽奖过滤 - 奖品ID，会根据抽奖次数判断、库存判断、兜底兜里返回最终的可获得奖品信息
		DefaultTreeFactory.StrategyAward treeStrategyAwardVO = raffleLogicTree(userId, strategyId, chainStrategyAwardVO.getAwardId());
		log.info("抽奖策略计算-规则树 {} {} {} {}", userId, strategyId, treeStrategyAwardVO.getAwardId(), treeStrategyAwardVO.getAwardRuleValue());

		// 返回抽奖结果
		return buildRaffleAwardBO(strategyId, treeStrategyAwardVO.getAwardId(), treeStrategyAwardVO.getAwardRuleValue());
	}

	private RaffleAwardBO buildRaffleAwardBO(Long strategyId, Integer awardId, String awardConfig) {
		StrategyAwardBO strategyAwardBO = strategyRepository.queryStrategyAwardBO(strategyId, awardId);
		return RaffleAwardBO.builder()
				.awardId(awardId)
				.awardTitle(strategyAwardBO.getAwardTitle())
				.awardConfig(awardConfig)
				.sort(strategyAwardBO.getSort())
				.build();
	}


	/**
	 * 抽奖计算，责任链抽象方法
	 *
	 * @param userId     用户ID
	 * @param strategyId 策略ID
	 * @return 奖品ID
	 */
	public abstract DefaultChainFactory.StrategyAward raffleLogicChain(String userId, Long strategyId);

	/**
	 * 抽奖结果过滤，决策树抽象方法
	 *
	 * @param userId     用户ID
	 * @param strategyId 策略ID
	 * @param awardId    奖品ID
	 * @return 过滤结果【奖品ID，会根据抽奖次数判断、库存判断、兜底兜里返回最终的可获得奖品信息】
	 */
	public abstract DefaultTreeFactory.StrategyAward raffleLogicTree(String userId, Long strategyId, Integer awardId);

}