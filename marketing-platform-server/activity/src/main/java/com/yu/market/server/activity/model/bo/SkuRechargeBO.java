package com.yu.market.server.activity.model.bo;

import com.yu.market.server.activity.model.enums.OrderTradeTypeEnum;
import lombok.Builder;
import lombok.Data;

/**
 * @author yu
 * @description 活动商品充值实体对象
 * @date 2025-01-23
 */
@Data
@Builder
public class SkuRechargeBO {

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 商品SKU - activity + activity count
	 */
	private Long sku;

	/**
	 * 幂等业务单号，外部谁充值谁透传，这样来保证幂等（多次调用也能确保结果唯一，不会多次充值）。
	 */
	private String outBusinessNo;

	/**
	 * 订单交易类型
	 */
	private OrderTradeTypeEnum orderTradeType = OrderTradeTypeEnum.rebate_no_pay_trade;

}
