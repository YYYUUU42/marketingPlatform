package com.yu.market.server.raffle.service.raffle;


import com.yu.market.server.raffle.model.bo.StrategyAwardBO;

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

}
