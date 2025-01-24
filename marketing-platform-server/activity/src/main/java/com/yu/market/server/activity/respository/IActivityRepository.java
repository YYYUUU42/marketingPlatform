package com.yu.market.server.activity.respository;


import com.yu.market.server.activity.model.aggregate.CreateOrderAggregate;
import com.yu.market.server.activity.model.bo.ActivityBO;
import com.yu.market.server.activity.model.bo.ActivityCountBO;
import com.yu.market.server.activity.model.bo.ActivitySkuBO;
import com.yu.market.server.activity.model.bo.ActivitySkuStockKeyBO;

import java.util.Date;

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

    /**
     * 缓存活动 sku 数量
     */
    void cacheActivitySkuStockCount(String cacheKey, Integer stockCount);

    /**
     * 扣减活动sku数量
     */
    boolean subtractionActivitySkuStock(Long sku, String cacheKey, Date endDateTime);

    /**
     * 将消耗的发送到消息队列
     */
    void activitySkuStockConsumeSendQueue(ActivitySkuStockKeyBO activitySkuStockKeyVO);

    /**
     * 获取活动 sku 库存消耗队列
     */
    ActivitySkuStockKeyBO takeQueueValue();

    /**
     * 清空队列
     */
    void clearQueueValue();

    /**
     * 延迟队列 + 任务趋势更新活动sku库存
     */
    void updateActivitySkuStock(Long sku);

    /**
     * 缓存库存已消耗完毕，清空数据库库存
     */
    void clearActivitySkuStock(Long sku);
}
