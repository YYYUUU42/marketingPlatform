package com.yu.market.server.activity.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yu
 * @description 行为类型枚举值对象
 * @date 2025-01-26
 */
@Getter
@AllArgsConstructor
public enum BehaviorTypeEnum {

    SIGN("sign", "签到（日历）"),
    PAY("pay", "外部支付完成"),
    ;

    private final String code;
    private final String info;

}
