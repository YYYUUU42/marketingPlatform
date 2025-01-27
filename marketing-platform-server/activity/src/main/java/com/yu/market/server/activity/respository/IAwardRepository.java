package com.yu.market.server.activity.respository;


import com.yu.market.server.activity.model.aggregate.GiveOutPrizesAggregate;
import com.yu.market.server.activity.model.aggregate.UserAwardRecordAggregate;

/**
 * @author yu
 * @description 奖品仓储服务
 * @date 2025-01-26
 */
public interface IAwardRepository {

    void saveUserAwardRecord(UserAwardRecordAggregate userAwardRecordAggregate);

    String queryAwardConfig(Integer awardId);

    void saveGiveOutPrizesAggregate(GiveOutPrizesAggregate giveOutPrizesAggregate);

    String queryAwardKey(Integer awardId);
}
