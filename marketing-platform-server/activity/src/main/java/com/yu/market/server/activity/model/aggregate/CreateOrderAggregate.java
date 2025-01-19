package com.yu.market.server.activity.model.aggregate;

import com.yu.market.server.activity.model.bo.ActivityAccountBO;
import com.yu.market.server.activity.model.bo.ActivityOrderBO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yu
 * @description 下单聚合对象
 * @date 2025-01-19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderAggregate {

    /**
     * 活动账户实体
     */
    private ActivityAccountBO activityAccountBO;
    /**
     * 活动订单实体
     */
    private ActivityOrderBO activityOrderBO;

}