package com.yu.market.server.activity.service.credit;


import com.yu.market.server.activity.model.bo.TradeBO;

/**
 * @author yu
 * @description 积分调额接口 - 正逆向，增减积分
 * @date 2025-01-27
 */
public interface ICreditAdjustService {

	/**
	 * 创建增加积分额度订单
	 *
	 * @param tradeBO 交易实体对象
	 * @return 单号
	 */
	String createOrder(TradeBO tradeBO);

}
