package com.yu.market.server.raffle.model.dto;

import lombok.Data;

/**
 * @author yu
 * @description 抽奖奖品列表，请求对象
 * @date 2025-01-18
 */
@Data
public class RaffleAwardListDTO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 抽奖活动ID
     */
    private Long activityId;

}
