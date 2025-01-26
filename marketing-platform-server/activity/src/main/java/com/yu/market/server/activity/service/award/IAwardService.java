package com.yu.market.server.activity.service.award;


import com.yu.market.server.activity.model.bo.UserAwardRecordBO;

/**
 * @author yu
 * @description 奖品服务接口
 * @date 2025-01-26
 */
public interface IAwardService {

    void saveUserAwardRecord(UserAwardRecordBO userAwardRecordBO);

}
