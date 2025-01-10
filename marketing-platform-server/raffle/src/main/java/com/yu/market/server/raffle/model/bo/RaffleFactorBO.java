package com.yu.market.server.raffle.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleFactorBO {
	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 策略ID
	 */
	private Long strategyId;

	/**
	 * 奖品ID
	 */
	private Integer awardId;
}
