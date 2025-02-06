package com.yu.market.server.activity.service.quota.policy.impl;

import com.yu.market.server.activity.model.aggregate.CreateQuotaOrderAggregate;
import com.yu.market.server.activity.model.enums.OrderStateEnum;
import com.yu.market.server.activity.respository.IActivityRepository;
import com.yu.market.server.activity.service.quota.policy.ITradePolicy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author yu
 * @description 返利无支付交易订单，直接充值到账
 * @date 2025-02-06
 */
@Service("rebate_no_pay_trade")
public class RebateNoPayTradePolicy implements ITradePolicy {

    private final IActivityRepository activityRepository;

    public RebateNoPayTradePolicy(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public void trade(CreateQuotaOrderAggregate createQuotaOrderAggregate) {
        // 不需要支付则修改订单金额为0，状态为完成，直接给用户账户充值
        createQuotaOrderAggregate.setOrderState(OrderStateEnum.completed);
        createQuotaOrderAggregate.getActivityOrderBO().setPayAmount(BigDecimal.ZERO);
        activityRepository.doSaveNoPayOrder(createQuotaOrderAggregate);
    }

}
