package com.yu.market.server.activity.model.bo;

import lombok.Builder;
import lombok.Data;

/**
 * @author yu
 * @description 参与抽奖活动实体对象
 * @date 2025-01-26
 */
@Data
@Builder
public class PartakeRaffleActivityBO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 活动ID
     */
    private Long activityId;

}
