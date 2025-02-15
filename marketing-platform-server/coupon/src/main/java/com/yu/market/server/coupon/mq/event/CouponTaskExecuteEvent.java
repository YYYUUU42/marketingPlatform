package com.yu.market.server.coupon.mq.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yu
 * @description 优惠券推送任务执行事件
 * @date 2025-02-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponTaskExecuteEvent {

    /**
     * 推送任务id
     */
    private Long couponTaskId;

    /**
     * 定时发送的时间
     */
    private Long delayTime;
}
