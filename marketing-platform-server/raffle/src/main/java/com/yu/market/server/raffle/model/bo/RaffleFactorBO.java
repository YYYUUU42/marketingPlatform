package com.yu.market.server.raffle.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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
	 * 结束时间
	 */
	private Date endDateTime;
}
