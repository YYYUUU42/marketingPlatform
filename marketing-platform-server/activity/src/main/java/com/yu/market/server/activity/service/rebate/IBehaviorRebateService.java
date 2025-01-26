package com.yu.market.server.activity.service.rebate;


import com.yu.market.server.activity.model.bo.BehaviorBO;

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

}
