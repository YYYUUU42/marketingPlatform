package com.yu.market.infrastructure.raffle.mapper;

import com.yu.market.infrastructure.raffle.pojo.RaffleActivityAccount;
import com.yu.market.infrastructure.raffle.pojo.RaffleActivityCount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @description 针对表【raffle_activity_count(抽奖活动次数配置表)】的数据库操作Mapper
* @Entity com.yu.market.infrastructure.pojo.RaffleActivityCount
*/
public interface RaffleActivityCountMapper extends BaseMapper<RaffleActivityCount> {

	/**
	 * 修改账户配额
	 */
	int updateAccountQuota(RaffleActivityAccount raffleActivityAccount);

}




