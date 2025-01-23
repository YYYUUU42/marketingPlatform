package com.yu.market.server.activity.service.rule.impl;

import com.yu.market.server.activity.model.bo.ActivityBO;
import com.yu.market.server.activity.model.bo.ActivityCountBO;
import com.yu.market.server.activity.model.bo.ActivitySkuBO;
import com.yu.market.server.activity.service.rule.AbstractActionChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author yu
 * @description 商品库存规则节点
 * @date 2025-01-23
 */
@Slf4j
@Component("activity_sku_stock_action")
public class ActivitySkuStockActionChain extends AbstractActionChain {

    @Override
    public boolean action(ActivitySkuBO activitySkuBO, ActivityBO activityBO, ActivityCountBO activityCountBO) {
        log.info("活动责任链-商品库存处理【校验&扣减】开始。");

        return true;
    }

}
