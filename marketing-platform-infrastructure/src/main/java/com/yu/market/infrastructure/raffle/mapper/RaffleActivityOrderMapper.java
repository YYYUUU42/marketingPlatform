package com.yu.market.infrastructure.raffle.mapper;

import com.yu.market.infrastructure.raffle.pojo.RaffleActivityOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @description 针对表【raffle_activity_order(抽奖活动单)】的数据库操作Mapper
* @Entity com.yu.market.infrastructure.pojo.RaffleActivityOrder
*/
public interface RaffleActivityOrderMapper extends BaseMapper<RaffleActivityOrder> {

	int updateOrderCompleted(RaffleActivityOrder raffleActivityOrderReq);
}




