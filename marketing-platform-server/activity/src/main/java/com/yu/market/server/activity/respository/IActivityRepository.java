package com.yu.market.server.activity.respository;


import com.yu.market.server.activity.model.aggregate.CreateOrderAggregate;
import com.yu.market.server.activity.model.bo.ActivityBO;
import com.yu.market.server.activity.model.bo.ActivityCountBO;
import com.yu.market.server.activity.model.bo.ActivitySkuBO;

/**
 * @author yu
 * @description 活动仓储接口
 * @date 2025-01-19
 */
public interface IActivityRepository {

    /**
     * 通过 sku 查询活动信息
     */
    ActivitySkuBO queryActivitySku(Long sku);

    /**
     * 查询活动信息
     */
    ActivityBO queryRaffleActivityByActivityId(Long activityId);

    /**
     * 查询次数信息（用户在活动上可参与的次数）
     */
    ActivityCountBO queryRaffleActivityCountByActivityCountId(Long activityCountId);

    /**
     * 保存订单
     */
    void doSaveOrder(CreateOrderAggregate createOrderAggregate);

}
