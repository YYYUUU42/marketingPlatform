package com.yu.market.server.coupon.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author yu
 * @description 优惠券使用状态枚举类
 * @date 2025-02-15
 */
@Getter
@RequiredArgsConstructor
public enum CouponStatusEnum {

    /**
     * 生效中
     */
    EFFECTIVE(0),

    /**
     * 已结束
     */
    ENDED(1);

    private final int type;
}
