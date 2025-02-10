package com.yu.market.server.raffle.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yu
 * @description 抽奖策略规则，权重配置，查询N次抽奖可解锁奖品范围，请求对象
 * @date 2025-02-10
 */
@Data
public class RaffleStrategyRuleWeightDTO implements Serializable {

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 抽奖活动ID
	 */
	private Long activityId;

}