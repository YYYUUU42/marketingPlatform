package com.yu.market.server.activity.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yu
 * @description 用户活动账户请求对象
 * @date 2025-02-06
 */
@Data
public class UserActivityAccountDTO implements Serializable {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 活动ID
     */
    private Long activityId;

}