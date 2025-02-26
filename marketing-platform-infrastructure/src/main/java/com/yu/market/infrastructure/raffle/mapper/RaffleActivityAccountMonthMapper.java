package com.yu.market.infrastructure.raffle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.market.infrastructure.raffle.pojo.RaffleActivityAccountMonth;
import org.apache.ibatis.annotations.Param;

/**
* @description 针对表【raffle_activity_account_month(抽奖活动账户表-月次数)】的数据库操作Mapper
* @Entity com.yu.market.infrastructure.pojo.RaffleActivityAccountMonth
*/
public interface RaffleActivityAccountMonthMapper extends BaseMapper<RaffleActivityAccountMonth> {

	int updateActivityAccountMonthSubtractionQuota(@Param("userId") String userId, @Param("activityId") Long activityId, @Param("month") String month);

	void addAccountQuota(RaffleActivityAccountMonth raffleActivityAccountMonth);
}




