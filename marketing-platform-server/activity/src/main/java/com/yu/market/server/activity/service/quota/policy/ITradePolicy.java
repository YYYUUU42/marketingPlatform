package com.yu.market.server.activity.service.quota.policy;


import com.yu.market.server.activity.model.aggregate.CreateQuotaOrderAggregate;

/**
 * @author yu
 * @description 交易策略接口，包括；返利兑换（不用支付），积分订单（需要支付）
 * @date 2025-02-06
 */
public interface ITradePolicy {

    void trade(CreateQuotaOrderAggregate createQuotaOrderAggregate);

}
