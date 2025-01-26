package com.yu.market.server.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.market.server.activity.model.pojo.RaffleActivityAccountDay;
import org.apache.ibatis.annotations.Param;

/**
* @description 针对表【raffle_activity_account_day(抽奖活动账户表-日次数)】的数据库操作Mapper
* @Entity com.yu.market.server.activity.model.pojo.RaffleActivityAccountDay
*/
public interface RaffleActivityAccountDayMapper extends BaseMapper<RaffleActivityAccountDay> {

	int updateActivityAccountDaySubtractionQuota(@Param("userId") String userId, @Param("activityId") Long activityId, @Param("day") String day);

}




