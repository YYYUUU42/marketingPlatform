package com.yu.market.server.activity.model.bo;

import com.yu.market.server.activity.model.enums.UserRaffleOrderStateEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author yu
 * @description 用户抽奖订单实体对象
 * @date 2025-01-26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRaffleOrderBO {

	/**
	 * 活动ID
	 */
	private String userId;

	/**
	 * 活动名称
	 */
	private Long activityId;

	/**
	 * 抽奖策略ID
	 */
	private String activityName;

	/**
	 * 订单ID
	 */
	private Long strategyId;

	/**
	 * 下单时间
	 */
	private String orderId;

	/**
	 * 下单时间
	 */
	private Date orderTime;

	/**
	 * 订单状态；create-创建、used-已使用、cancel-已作废
	 */
	private UserRaffleOrderStateEnum orderState;

	/** 结束时间 */
	private Date endDateTime;

}
