package com.yu.market.server.activity.model.bo;

import lombok.*;

import java.math.BigDecimal;

/**
 * @author yu
 * @description 未完成支付的活动单
 * @date 2025-02-06
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnpaidActivityOrderBO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 外部透传ID
     */
    private String outBusinessNo;

    /**
     * 订单金额
     */
    private BigDecimal payAmount;

}
