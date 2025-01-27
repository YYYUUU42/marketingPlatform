package com.yu.market.server.activity.respository;

import com.yu.market.server.activity.model.aggregate.TradeAggregate;

/**
 * @author yu
 * @description 用户积分仓储
 * @date 2025-01-27
 */
public interface ICreditRepository {

    void saveUserCreditTradeOrder(TradeAggregate tradeAggregate);

}
