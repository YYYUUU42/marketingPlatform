package com.yu.market.server.raffle.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yu
 * @description 抽奖响应结果
 * @date 2025-01-18
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleVO {

    /**
     * 奖品ID
     */
    private Integer awardId;

    /**
     * 排序编号 - 策略奖品配置的奖品顺序编号
     */
    private Integer awardIndex;

}
