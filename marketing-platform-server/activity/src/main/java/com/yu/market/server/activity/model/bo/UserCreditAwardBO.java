package com.yu.market.server.activity.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author yu
 * @description 用户积分奖品实体对象
 * @date 2025-01-27
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreditAwardBO {

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 积分值
	 */
	private BigDecimal creditAmount;

}
