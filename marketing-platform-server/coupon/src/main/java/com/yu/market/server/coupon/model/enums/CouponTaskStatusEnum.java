package com.yu.market.server.coupon.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author yu
 * @description 优惠券推送任务状态枚举
 * @date 2025-02-15
 */
@Getter
@RequiredArgsConstructor
public enum CouponTaskStatusEnum {

    /**
     * 待执行
     */
    PENDING(0),

    /**
     * 执行中
     */
    IN_PROGRESS(1),

    /**
     * 执行失败
     */
    FAILED(2),

    /**
     * 执行成功
     */
    SUCCESS(3),

    /**
     * 取消
     */
    CANAL(4);

    private final int status;
}