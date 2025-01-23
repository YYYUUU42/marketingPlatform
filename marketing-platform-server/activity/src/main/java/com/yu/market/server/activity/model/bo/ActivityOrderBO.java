package com.yu.market.server.activity.model.bo;

import com.yu.market.server.activity.model.enums.OrderStateEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author yu
 * @description 活动参与实体对象
 * @date 2025-01-19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityOrderBO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * sku
     */
    private Long sku;


    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 抽奖策略ID
     */
    private Long strategyId;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 下单时间
     */
    private Date orderTime;

    /**
     * 总次数
     */
    private Integer totalCount;

    /**
     * 日次数
     */
    private Integer dayCount;

    /**
     * 月次数
     */
    private Integer monthCount;

    /**
     * 订单状态
     */
    private OrderStateEnum state;

    /**
     * 业务仿重ID - 外部透传的，确保幂等
     */
    private String outBusinessNo;

}
