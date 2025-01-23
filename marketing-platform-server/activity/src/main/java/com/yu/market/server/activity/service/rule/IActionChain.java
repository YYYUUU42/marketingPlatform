package com.yu.market.server.activity.service.rule;


import com.yu.market.server.activity.model.bo.ActivityBO;
import com.yu.market.server.activity.model.bo.ActivityCountBO;
import com.yu.market.server.activity.model.bo.ActivitySkuBO;

/**
 * @author yu
 * @description 下单规则过滤接口
 * @date 2025-01-23
 */
public interface IActionChain extends IActionChainArmory {

    boolean action(ActivitySkuBO activitySkuBO, ActivityBO activityBO, ActivityCountBO activityCountBO);

}
