package com.yu.market.server.activity.service;


import com.yu.market.server.activity.model.bo.ActivityAccountBO;
import com.yu.market.server.activity.model.bo.DeliveryOrderBO;
import com.yu.market.server.activity.model.bo.SkuRechargeBO;
import com.yu.market.server.activity.model.bo.UnpaidActivityOrderBO;

/**
 * @author yu
 * @description 抽奖活动账户额度服务
 * @date 2025-01-25
 */
public interface IRaffleActivityAccountQuotaService {

    /**
     * 创建 sku 账户充值订单，给用户增加抽奖次数
     * 1. 在【打卡、签到、分享、对话、积分兑换】等行为动作下，创建出活动订单，给用户的活动账户【日、月】充值可用的抽奖次数。
     * 2. 对于用户可获得的抽奖次数，比如首次进来就有一次，则是依赖于运营配置的动作，在前端页面上。用户点击后，可以获得一次抽奖次数。
     *
     * @param skuRechargeBO 活动商品充值实体对象
     * @return 未支付订单
     */
    UnpaidActivityOrderBO createOrder(SkuRechargeBO skuRechargeBO);

    /**
     * 查询活动账户额度「总、月、日」
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     * @return 账户实体
     */
    ActivityAccountBO queryActivityAccountBO(Long activityId, String userId);

    /**
     * 订单出货 - 积分充值
     *
     * @param deliveryOrderEntity 出货单实体对象
     */
    void updateOrder(DeliveryOrderBO deliveryOrderEntity);

    /**
     * 查询活动账户 - 总，参与次数
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     * @return 参与次数
     */
    Integer queryRaffleActivityAccountPartakeCount(Long activityId, String userId);

    /**
     * 查询活动账户 - 日，参与次数
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     * @return 参与次数
     */
    Integer queryRaffleActivityAccountDayPartakeCount(Long activityId, String userId);
}
