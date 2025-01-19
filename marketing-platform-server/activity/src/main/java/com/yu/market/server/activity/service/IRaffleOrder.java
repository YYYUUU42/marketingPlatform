package com.yu.market.server.activity.service;


import com.yu.market.server.activity.model.bo.ActivityOrderBO;
import com.yu.market.server.activity.model.bo.ActivityShopCartBO;

/**
 * @author yu
 * @description 抽奖活动订单接口
 * @date 2025-01-19
 */
public interface IRaffleOrder {

    /**
     * 以sku创建抽奖活动订单，获得参与抽奖资格（可消耗的次数）
     *
     * @param activityShopCartEntity 活动sku实体，通过sku领取活动。
     * @return 活动参与记录实体
     */
    ActivityOrderBO createRaffleActivityOrder(ActivityShopCartBO activityShopCartEntity);

}
