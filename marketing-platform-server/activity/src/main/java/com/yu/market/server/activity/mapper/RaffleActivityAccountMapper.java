package com.yu.market.server.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.market.server.activity.model.pojo.RaffleActivityAccount;
import org.apache.ibatis.annotations.Param;

/**
* @description 针对表【raffle_activity_account(抽奖活动账户表)】的数据库操作Mapper
 * @Entity com.yu.market.server.activity.model.pojo.RaffleActivityAccount
*/
public interface RaffleActivityAccountMapper extends BaseMapper<RaffleActivityAccount> {

	/**
	 * 更新总账户
	 */
	int updateActivityAccountSubtractionQuota(@Param("userId") String userId, @Param("activityId") Long activityId);

	int updateActivityAccountMonthSubtractionQuota(@Param("userId") String userId, @Param("activityId") Long activityId);

	int updateActivityAccountDaySubtractionQuota(@Param("userId") String userId, @Param("activityId") Long activityId);

	int updateActivityAccountMonthSurplusImageQuota(@Param("userId") String userId, @Param("activityId") Long activityId, @Param("monthCountSurplus") Integer monthCountSurplus);

	int updateActivityAccountDaySurplusImageQuota(@Param("userId") String userId, @Param("activityId") Long activityId, @Param("dayCountSurplus") Integer dayCountSurplus);
}




