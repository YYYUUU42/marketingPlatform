package com.yu.market.server.raffle.service.raffle;


import com.yu.market.server.raffle.model.bo.StrategyAwardBO;
import com.yu.market.server.raffle.model.bo.StrategyAwardStockKeyBO;

import java.util.List;

/**
 * @author yu
 * @description 策略奖品接口
 * @date 2025-01-18
 */
public interface IRaffleAward {

    /**
     * 根据策略ID查询抽奖奖品列表配置
     *
     * @param strategyId 策略ID
     * @return 奖品列表
     */
    List<StrategyAwardBO> queryRaffleStrategyAwardList(Long strategyId);

    /**
     * 根据活动ID查询抽奖奖品列表配置
     *
     * @param activityId 活动ID
     * @return 奖品列表
     */
    List<StrategyAwardBO> queryRaffleStrategyAwardListByActivityId(Long activityId);

    /**
     * 查询有效活动的奖品配置
     *
     * @return 奖品配置列表
     */
    List<StrategyAwardStockKeyBO> queryOpenActivityStrategyAwardList();

}
