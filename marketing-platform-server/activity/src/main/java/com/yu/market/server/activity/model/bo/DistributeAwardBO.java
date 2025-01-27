package com.yu.market.server.activity.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yu
 * @description 分发奖品实体
 * @date 2025-01-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributeAwardBO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 奖品ID
     */
    private Integer awardId;

    /**
     * 奖品配置信息
     */
    private String awardConfig;

}