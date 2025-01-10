package com.yu.market.server.raffle.model.bo;

import com.yu.market.server.raffle.model.enums.RuleLogicCheckType;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleActionBO<T extends RuleActionBO.RaffleBO> {

	private String code = RuleLogicCheckType.ALLOW.getCode();
	private String info = RuleLogicCheckType.ALLOW.getInfo();
	private String ruleModel;
	private T data;

	public static class RaffleBO {

	}

	/**
	 * 抽奖前置
	 */
	@EqualsAndHashCode(callSuper = true)
	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class RaffleBeforeBO extends RaffleBO {
		/**
		 * 策略ID
		 */
		private Long strategyId;

		/**
		 * 权重值Key；用于抽奖时可以选择权重抽奖。
		 */
		private String ruleWeightValueKey;

		/**
		 * 奖品ID；
		 */
		private Integer awardId;
	}

	/**
	 * 抽奖中置
	 */
	static public class RaffleCenterBO extends RaffleBO {

	}

	/**
	 * 抽奖后置
	 */
	static public class RaffleAfterBO extends RaffleBO {

	}

}
