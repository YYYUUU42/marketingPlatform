package com.yu.market.server.activity.mapper;

import com.yu.market.server.activity.model.pojo.RaffleActivitySku;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @description 针对表【raffle_activity_sku】的数据库操作Mapper
* @Entity com.yu.market.server.activity.model.pojo.RaffleActivitySku
*/
public interface RaffleActivitySkuMapper extends BaseMapper<RaffleActivitySku> {

	/**
	 * 修改 sku 库存
	 */
	void updateActivitySkuStock(Long sku);

	/**
	 * 将 sku 库存清零
	 */
	void clearActivitySkuStock(Long sku);
}




