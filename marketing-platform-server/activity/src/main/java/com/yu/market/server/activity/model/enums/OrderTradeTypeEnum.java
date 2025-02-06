package com.yu.market.server.activity.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yu
 * @description 订单交易类型
 * @date 2025-02-06
 */
@Getter
@AllArgsConstructor
public enum OrderTradeTypeEnum {

    credit_pay_trade("credit_pay_trade","积分兑换，需要支付类交易"),
    rebate_no_pay_trade("rebate_no_pay_trade", "返利奖品，不需要支付类交易"),
    ;

    private final String code;
    private final String desc;

}