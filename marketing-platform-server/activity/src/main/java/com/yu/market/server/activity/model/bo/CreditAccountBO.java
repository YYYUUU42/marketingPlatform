package com.yu.market.server.activity.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author yu
 * @description 积分账户实体
 * @date 2025-01-27
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreditAccountBO {

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 可用积分，每次扣减的值
	 */
	private BigDecimal adjustAmount;

}
