package com.yu.market.server.activity.service.rule.impl;

import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.server.activity.model.bo.ActivityBO;
import com.yu.market.server.activity.model.bo.ActivityCountBO;
import com.yu.market.server.activity.model.bo.ActivitySkuBO;
import com.yu.market.server.activity.model.bo.ActivitySkuStockKeyBO;
import com.yu.market.server.activity.respository.IActivityRepository;
import com.yu.market.server.activity.service.armory.IActivityDispatch;
import com.yu.market.server.activity.service.rule.AbstractActionChain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author yu
 * @description 商品库存规则节点
 * @date 2025-01-23
 */
@Slf4j
@Component("activity_sku_stock_action")
@RequiredArgsConstructor
public class ActivitySkuStockActionChain extends AbstractActionChain {

    private final IActivityDispatch activityDispatch;
    private final IActivityRepository activityRepository;

    @Override
    public boolean action(ActivitySkuBO activitySkuBO, ActivityBO activityBO, ActivityCountBO activityCountBO) {
        log.info("活动责任链-商品库存处理 - 有效期、状态、库存(sku) - 开始。sku:{} activityId:{}", activitySkuBO.getSku(), activityBO.getActivityId());
        // 扣减库存
        boolean status = activityDispatch.subtractionActivitySkuStock(activitySkuBO.getSku(), activityBO.getEndDateTime());
        // true；库存扣减成功
        if (status) {
            log.info("活动责任链-商品库存处理【有效期、状态、库存(sku)】成功。sku:{} activityId:{}", activitySkuBO.getSku(), activityBO.getActivityId());

            // 写入延迟队列，延迟消费更新库存记录
            activityRepository.activitySkuStockConsumeSendQueue(ActivitySkuStockKeyBO.builder()
                    .sku(activitySkuBO.getSku())
                    .activityId(activityBO.getActivityId())
                    .build());

            return true;
        }

        throw new ServiceException(BaseErrorCode.ACTIVITY_SKU_STOCK_ERROR);
    }

}
