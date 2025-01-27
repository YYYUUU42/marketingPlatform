package com.yu.market.server.activity.model.bo;

import com.yu.market.server.activity.model.enums.TradeNameEnum;
import com.yu.market.server.activity.model.enums.TradeTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author yu
 * @description 增加额度实体
 * @date 2025-01-27
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeBO {

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 交易名称
	 */
	private TradeNameEnum tradeName;

	/**
	 * 交易类型；交易类型；forward-正向、reverse-逆向
	 */
	private TradeTypeEnum tradeType;

	/**
	 * 交易金额
	 */
	private BigDecimal amount;

	/**
	 * 业务仿重ID - 外部透传。返利、行为等唯一标识
	 */
	private String outBusinessNo;

}
