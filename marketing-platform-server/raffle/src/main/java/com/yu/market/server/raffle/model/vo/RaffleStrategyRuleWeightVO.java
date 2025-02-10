package com.yu.market.server.raffle.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yu
 * @description 抽奖策略规则，权重配置，查询N次抽奖可解锁奖品范围，应答对象
 * @date 2025-02-10
 */
@Data
public class RaffleStrategyRuleWeightVO implements Serializable {

	/**
	 * 权重规则配置的抽奖次数
	 */
	private Integer ruleWeightCount;

	/**
	 * 用户在一个活动下完成的总抽奖次数
	 */
	private Integer userActivityAccountTotalUseCount;

	/**
	 * 当前权重可抽奖范围
	 */
	private List<StrategyAward> strategyAwards;

	@Data
	public static class StrategyAward {
		/**
		 * 奖品ID
		 */
		private Integer awardId;

		/**
		 * 奖品标题
		 */
		private String awardTitle;
	}

}
