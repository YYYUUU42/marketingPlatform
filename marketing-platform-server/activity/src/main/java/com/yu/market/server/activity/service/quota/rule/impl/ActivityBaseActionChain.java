package com.yu.market.server.activity.service.quota.rule.impl;

import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.server.activity.model.bo.ActivityBO;
import com.yu.market.server.activity.model.bo.ActivityCountBO;
import com.yu.market.server.activity.model.bo.ActivitySkuBO;
import com.yu.market.server.activity.model.enums.ActivityStateEnum;
import com.yu.market.server.activity.service.quota.rule.AbstractActionChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

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
        log.info("活动责任链-基础信息【有效期、状态、库存(sku)】校验开始。sku:{} activityId:{}", activitySkuBO.getSku(), activityBO.getActivityId());
        // 校验；活动状态
        if (!ActivityStateEnum.open.equals(activityBO.getState())) {
            throw new ServiceException(BaseErrorCode.ACTIVITY_STATE_ERROR);
        }

        // 校验；活动日期「开始时间 <- 当前时间 -> 结束时间」
        Date currentDate = new Date();
        if (activityBO.getBeginDateTime().after(currentDate) || activityBO.getEndDateTime().before(currentDate)) {
            throw new ServiceException(BaseErrorCode.ACTIVITY_DATE_ERROR);
        }

        // 校验；活动sku库存 「剩余库存从缓存获取的」
        if (activitySkuBO.getStockCountSurplus() <= 0) {
            throw new ServiceException(BaseErrorCode.ACTIVITY_SKU_STOCK_ERROR);
        }
        return next().action(activitySkuBO, activityBO, activityCountBO);
    }

}
