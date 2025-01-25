package com.yu.market.server.activity.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yu
 * @description 活动账户（日）实体对象
 * @date 2025-01-26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityAccountDayBO {

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 活动ID
	 */
	private Long activityId;

	/**
	 * 日期（yyyy-mm-dd）
	 */
	private String day;

	/**
	 * 日次数
	 */
	private Integer dayCount;

	/**
	 * 日次数-剩余
	 */
	private Integer dayCountSurplus;

}
