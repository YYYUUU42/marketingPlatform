package com.yu.market.server.raffle.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyAwardBO {
	/**
	 * 抽奖策略ID
	 */
	private Long strategyId;

	/**
	 * 抽奖奖品ID - 内部流转使用
	 */
	private Integer awardId;

	/**
	 * 奖品库存总量
	 */
	private Integer awardCount;

	/**
	 * 奖品库存剩余
	 */
	private Integer awardCountSurplus;

	/**
	 * 奖品中奖概率
	 */
	private BigDecimal awardRate;
}
