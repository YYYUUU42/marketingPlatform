package com.yu.market.server.activity.service;

import com.yu.market.server.activity.model.bo.SkuProductBO;

import java.util.List;

/**
 * @author yu
 * @description sku商品服务接口
 * @date 2025-02-06
 */
public interface IRaffleActivitySkuProductService {

	/**
	 * 查询当前活动ID下，创建的 sku 商品。「sku可以兑换活动抽奖次数」
	 *
	 * @param activityId 活动ID
	 * @return 返回sku商品集合
	 */
	List<SkuProductBO> querySkuProductBOListByActivityId(Long activityId);

}