package com.yu.market.server.raffle.service.raffle;


import com.yu.market.server.raffle.model.bo.RaffleAwardBO;
import com.yu.market.server.raffle.model.bo.RaffleFactorBO;

/**
 * @author yu
 * @description 抽奖策略接口
 * @date 2025-01-09
 */
public interface IRaffleStrategy {

    /**
     * 执行抽奖；用抽奖因子入参，执行抽奖计算，返回奖品信息
     *
     * @param raffleFactorBO 抽奖因子实体对象，根据入参信息计算抽奖结果
     * @return 抽奖的奖品
     */
    RaffleAwardBO performRaffle(RaffleFactorBO raffleFactorBO);

}
