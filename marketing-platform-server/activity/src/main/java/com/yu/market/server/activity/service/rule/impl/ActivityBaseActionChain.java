package com.yu.market.server.activity.service.rule.impl;

import com.yu.market.server.activity.model.bo.ActivityBO;
import com.yu.market.server.activity.model.bo.ActivityCountBO;
import com.yu.market.server.activity.model.bo.ActivitySkuBO;
import com.yu.market.server.activity.service.rule.AbstractActionChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author yu
 * @description 活动规则过滤【日期、状态】
 * @date 2025-01-23
 */
@Slf4j
@Component("activity_base_action")
public class ActivityBaseActionChain extends AbstractActionChain {

    @Override
    public boolean action(ActivitySkuBO activitySkuBO, ActivityBO activityBO, ActivityCountBO activityCountBO) {

        log.info("活动责任链-基础信息【有效期、状态】校验开始。");

        return next().action(activitySkuBO, activityBO, activityCountBO);
    }

}
