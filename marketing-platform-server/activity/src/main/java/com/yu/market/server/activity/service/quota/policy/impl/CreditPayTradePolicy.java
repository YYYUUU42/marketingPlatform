package com.yu.market.server.activity.service.quota.policy.impl;

import com.yu.market.server.activity.model.aggregate.CreateQuotaOrderAggregate;
import com.yu.market.server.activity.model.enums.OrderStateEnum;
import com.yu.market.server.activity.respository.IActivityRepository;
import com.yu.market.server.activity.service.quota.policy.ITradePolicy;
import org.springframework.stereotype.Service;

/**
 * @author yu
 * @description 积分兑换，支付类订单
 * @date 2025-02-06
 */
@Service("credit_pay_trade")
public class CreditPayTradePolicy implements ITradePolicy {

    private final IActivityRepository activityRepository;

    public CreditPayTradePolicy(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public void trade(CreateQuotaOrderAggregate createQuotaOrderAggregate) {
        createQuotaOrderAggregate.setOrderState(OrderStateEnum.wait_pay);
        activityRepository.doSaveCreditPayOrder(createQuotaOrderAggregate);
    }

}
