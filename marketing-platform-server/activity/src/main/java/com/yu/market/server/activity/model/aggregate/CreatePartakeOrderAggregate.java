package com.yu.market.server.activity.model.aggregate;

import com.yu.market.server.activity.model.bo.ActivityAccountBO;
import com.yu.market.server.activity.model.bo.ActivityAccountDayBO;
import com.yu.market.server.activity.model.bo.ActivityAccountMonthBO;
import com.yu.market.server.activity.model.bo.UserRaffleOrderBO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yu
 * @description 参与活动订单聚合对象
 * @date 2025-01-26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePartakeOrderAggregate {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 账户总额度
     */
    private ActivityAccountBO activityAccountBO;

    /**
     * 是否存在月账户
     */
    private boolean isExistAccountMonth = true;

    /**
     * 账户月额度
     */
    private ActivityAccountMonthBO activityAccountMonthBO;

    /**
     * 是否存在日账户
     */
    private boolean isExistAccountDay = true;

    /**
     * 账户日额度
     */
    private ActivityAccountDayBO activityAccountDayBO;

    /**
     * 抽奖单实体
     */
    private UserRaffleOrderBO userRaffleOrderBO;

}
