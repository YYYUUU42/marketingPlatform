package com.yu.market.server.coupon.mq.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yu
 * @description 优惠券推送任务定时执行事件
 * @date 2025-02-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponTaskDelayEvent {

    /**
     * 推送任务id
     */
    private Long couponTaskId;

    /**
     * 发送状态
     */
    private Integer status;
}
