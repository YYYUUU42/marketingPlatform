package com.yu.market.server.activity.model.aggregate;

import com.yu.market.common.utils.RandomStringUtil;
import com.yu.market.common.utils.SnowFlakeUtil;
import com.yu.market.server.activity.model.bo.CreditAccountBO;
import com.yu.market.server.activity.model.bo.CreditOrderBO;
import com.yu.market.server.activity.model.enums.TradeNameEnum;
import com.yu.market.server.activity.model.enums.TradeTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author yu
 * @description 交易聚合对象
 * @date 2025-01-27
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeAggregate {

	/**
	 * 交易聚合对象
	 */
	private String userId;

	/**
	 * 积分账户实体
	 */
	private CreditAccountBO creditAccountBO;

	/**
	 * 积分订单实体
	 */
	private CreditOrderBO creditOrderBO;

	public static CreditAccountBO createCreditAccountBO(String userId, BigDecimal adjustAmount) {
		return CreditAccountBO.builder().userId(userId).adjustAmount(adjustAmount).build();
	}

	public static CreditOrderBO createCreditOrderBO(String userId, TradeNameEnum tradeName, TradeTypeEnum tradeType, BigDecimal tradeAmount, String outBusinessNo) {
		return CreditOrderBO.builder()
				.userId(userId)
				.orderId(String.valueOf(new SnowFlakeUtil().nextId()))
				.tradeName(tradeName)
				.tradeType(tradeType)
				.tradeAmount(tradeAmount)
				.outBusinessNo(outBusinessNo)
				.build();
	}

}
