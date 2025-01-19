package com.yu.market.server.activity.model.bo;

import com.yu.market.server.activity.model.enums.ActivityStateEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author yu
 * @description 活动实体对象
 * @date 2025-01-19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityBO {

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 活动描述
     */
    private String activityDesc;

    /**
     * 开始时间
     */
    private Date beginDateTime;

    /**
     * 结束时间
     */
    private Date endDateTime;

    /**
     * 活动参与次数配置
     */
    private Long activityCountId;

    /**
     * 抽奖策略ID
     */
    private Long strategyId;

    /**
     * 活动状态
     */
    private ActivityStateEnum state;

}
