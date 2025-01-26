package com.yu.market.server.activity.model.aggregate;

import com.yu.market.server.activity.envent.task.RebateTaskBO;
import com.yu.market.server.activity.model.bo.BehaviorRebateOrderBO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yu
 * @description 行为返利聚合对象
 * @date 2025-01-26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorRebateAggregate {

	/**
	 * 用户ID
	 */
	private String userId;
	
	/**
	 * 行为返利订单实体对象
	 */
	private BehaviorRebateOrderBO behaviorRebateOrderBO;
	
	/**
	 * 任务实体对象
	 */
	private RebateTaskBO rebateTaskBO;

}
