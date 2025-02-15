package com.yu.market.server.coupon.mq.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yu
 * @description 优惠券模板定时执行事件
 * @date 2025-02-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponTemplateDelayEvent {

    /**
     * 店铺id
     */
    private Long shopNumber;

    /**
     * 优惠券模板id
     */
    private Long couponTemplateId;

    /**
     * 具体延迟时间
     */
    private Long delayTime;
}
