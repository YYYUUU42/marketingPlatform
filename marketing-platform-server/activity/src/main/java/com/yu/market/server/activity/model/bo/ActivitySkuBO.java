package com.yu.market.server.activity.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author yu
 * @description 活动sku实体对象
 * @date 2025-02-06
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivitySkuBO {

    /**
     * 商品sku
     */
    private Long sku;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 活动个人参数ID；在这个活动上，一个人可参与多少次活动（总、日、月）
     */
    private Long activityCountId;

    /**
     * 库存总量
     */
    private Integer stockCount;

    /**
     * 剩余库存
     */
    private Integer stockCountSurplus;

    /**
     * 商品金额【积分】
     */
    private BigDecimal productAmount;

}
