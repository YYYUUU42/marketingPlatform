package com.yu.market.server.activity.respository;

import com.yu.market.server.activity.model.aggregate.TradeAggregate;
import com.yu.market.server.activity.model.bo.CreditAccountBO;

/**
 * @author yu
 * @description 用户积分仓储
 * @date 2025-01-27
 */
public interface ICreditRepository {

    void saveUserCreditTradeOrder(TradeAggregate tradeAggregate);

    CreditAccountBO queryUserCreditAccount(String userId);

}
