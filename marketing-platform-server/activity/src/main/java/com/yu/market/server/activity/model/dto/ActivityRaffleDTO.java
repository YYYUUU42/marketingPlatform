package com.yu.market.server.activity.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yu
 * @description 活动抽奖请求对象
 * @date 2025-02-02
 */
@Data
public class ActivityRaffleDTO implements Serializable {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 活动ID
     */
    private Long activityId;

}
