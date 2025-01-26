package com.yu.market.server.activity.respository;


import com.yu.market.server.activity.model.aggregate.BehaviorRebateAggregate;
import com.yu.market.server.activity.model.bo.DailyBehaviorRebateBO;
import com.yu.market.server.activity.model.enums.BehaviorTypeEnum;

import java.util.List;

/**
 * @author yu
 * @description 行为返利服务仓储接口
 * @date 2025-01-26
 */
public interface IBehaviorRebateRepository {

	List<DailyBehaviorRebateBO> queryDailyBehaviorRebateConfig(BehaviorTypeEnum behaviorTypeEnum);

	void saveUserRebateRecord(String userId, List<BehaviorRebateAggregate> behaviorRebateAggregates);

}
