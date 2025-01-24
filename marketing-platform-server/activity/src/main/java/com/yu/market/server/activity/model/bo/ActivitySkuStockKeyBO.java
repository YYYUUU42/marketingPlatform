package com.yu.market.server.activity.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yu
 * @description 活动sku库存 key 值对象
 * @date 2025-01-24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivitySkuStockKeyBO {

	/**
	 * 商品sku
	 */
	private Long sku;

	/**
	 * 活动ID
	 */
	private Long activityId;

}