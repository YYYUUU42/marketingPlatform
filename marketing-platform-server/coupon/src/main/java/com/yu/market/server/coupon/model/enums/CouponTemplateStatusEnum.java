package com.yu.market.server.coupon.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author yu
 * @description 优惠券模板状态枚举
 * @date 2025-02-15
 */
@Getter
@RequiredArgsConstructor
public enum CouponTemplateStatusEnum {

    /**
     * 0: 表示优惠券处于生效中的状态。
     */
    ACTIVE(0),

    /**
     * 1: 表示优惠券已经结束，不可再使用。
     */
    ENDED(1);

    private final int status;
}