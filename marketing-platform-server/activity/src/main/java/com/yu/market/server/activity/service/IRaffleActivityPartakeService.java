package com.yu.market.server.activity.service;

import com.yu.market.server.activity.model.bo.PartakeRaffleActivityBO;
import com.yu.market.server.activity.model.bo.UserRaffleOrderBO;

/**
 * @author yu
 * @description 抽奖活动参与服务
 * @date 2025-01-25
 */
public interface IRaffleActivityPartakeService {

	/**
	 * 创建抽奖单；用户参与抽奖活动，扣减活动账户库存，产生抽奖单。如存在未被使用的抽奖单则直接返回已存在的抽奖单。
	 *
	 * @param userId     用户ID
	 * @param activityId 活动ID
	 * @return 用户抽奖订单实体对象
	 */
	UserRaffleOrderBO createOrder(String userId, Long activityId);

	/**
	 * 创建抽奖单；用户参与抽奖活动，扣减活动账户库存，产生抽奖单。如存在未被使用的抽奖单则直接返回已存在的抽奖单。
	 *
	 * @param partakeRaffleActivityBO 参与抽奖活动实体对象
	 * @return 用户抽奖订单实体对象
	 */
	UserRaffleOrderBO createOrder(PartakeRaffleActivityBO partakeRaffleActivityBO);

}
