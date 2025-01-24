package com.yu.market.server.activity.service.armory;

/**
 * @author yu
 * @description 活动装配预热
 * @date 2025-01-24
 */
public interface IActivityArmory {

    /**
     * 装配活动 sku
     */
    boolean assembleActivitySku(Long sku);

}
