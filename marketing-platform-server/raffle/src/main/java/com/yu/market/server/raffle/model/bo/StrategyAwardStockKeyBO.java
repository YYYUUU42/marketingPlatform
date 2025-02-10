package com.yu.market.server.raffle.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yu
 * @description 策略奖品库存 Key 标识值对象
 * @date 2025-01-18
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyAwardStockKeyBO {

	/**
	 * 策略ID
	 */
	private Long strategyId;

	/**
	 * 奖品ID
	 */
	private Integer awardId;

}
