package com.yu.market.server.activity.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yu
 * @description 活动账户（月）实体对象
 * @date 2025-01-26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityAccountMonthBO {

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 活动ID
	 */
	private Long activityId;

	/**
	 * 月（yyyy-mm）
	 */
	private String month;

	/**
	 * 月次数
	 */
	private Integer monthCount;

	/**
	 * 月次数-剩余
	 */
	private Integer monthCountSurplus;

}
