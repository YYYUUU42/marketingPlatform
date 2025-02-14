package com.yu.market.infrastructure.raffle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.market.infrastructure.raffle.pojo.StrategyAward;

/**
* @author yu
* @description 针对表【strategy_award(抽奖策略奖品概率)】的数据库操作Mapper
* @Entity com.yu.server.raffle.model.pojo.StrategyAward
*/
public interface StrategyAwardMapper extends BaseMapper<StrategyAward> {

	/**
	 * 库存减一
	 */
	void updateStrategyAwardStock(StrategyAward strategyAward);

}