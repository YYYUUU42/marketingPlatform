package com.yu.market.server.activity.service.rebate;


import com.yu.market.server.activity.model.bo.BehaviorBO;
import com.yu.market.server.activity.model.bo.BehaviorRebateOrderBO;

import java.util.List;

/**
 * @author yu
 * @description 行为返利服务接口
 * @date 2025-01-26
 */
public interface IBehaviorRebateService {

    /**
     * 创建行为动作的入账订单
     *
     * @param behaviorBO 行为实体对象
     * @return 订单ID
     */
    List<String> createOrder(BehaviorBO behaviorBO);


    /**
     * 根据外部单号查询订单
     *
     * @param userId        用户ID
     * @param outBusinessNo 业务ID；签到则是日期字符串，支付则是外部的业务ID
     * @return 返利订单实体
     */
    List<BehaviorRebateOrderBO> queryOrderByOutBusinessNo(String userId, String outBusinessNo);
}
