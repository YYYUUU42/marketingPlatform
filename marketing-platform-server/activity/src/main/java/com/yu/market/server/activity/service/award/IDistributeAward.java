package com.yu.market.server.activity.service.award;


import com.yu.market.server.activity.model.bo.DistributeAwardBO;

/**
 * @author yu
 * @description 分发奖品接口
 * @date 2025-01-27
 */
public interface IDistributeAward {

    void giveOutPrizes(DistributeAwardBO distributeAwardBO);

}
