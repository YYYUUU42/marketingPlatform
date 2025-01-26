package com.yu.market.server.activity.model.aggregate;

import com.yu.market.server.activity.envent.task.AwardTaskBO;
import com.yu.market.server.activity.model.bo.UserAwardRecordBO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yu
 * @description 用户中奖记录聚合对象 - 聚合代表一个事务操作
 * @date 2025-01-26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAwardRecordAggregate {

    private UserAwardRecordBO userAwardRecordBO;

    private AwardTaskBO awardTaskBO;

}
