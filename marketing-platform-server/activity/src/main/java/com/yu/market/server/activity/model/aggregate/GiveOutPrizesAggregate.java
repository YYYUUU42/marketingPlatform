package com.yu.market.server.activity.model.aggregate;

import com.yu.market.server.activity.model.bo.UserAwardRecordBO;
import com.yu.market.server.activity.model.bo.UserCreditAwardBO;
import com.yu.market.server.activity.model.enums.AwardStateEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author yu
 * @description 发放奖品聚合对象
 * @date 2025-01-27
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GiveOutPrizesAggregate {

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 用户发奖记录
	 */
	private UserAwardRecordBO userAwardRecordBO;

	/**
	 * 用户积分奖品
	 */
	private UserCreditAwardBO userCreditAwardBO;

	public static UserAwardRecordBO buildDistributeUserAwardRecordBO(String userId, String orderId, Integer awardId, AwardStateEnum awardState) {
		UserAwardRecordBO userAwardRecord = new UserAwardRecordBO();
		userAwardRecord.setUserId(userId);
		userAwardRecord.setOrderId(orderId);
		userAwardRecord.setAwardId(awardId);
		userAwardRecord.setAwardState(awardState);
		return userAwardRecord;
	}

	public static UserCreditAwardBO buildUserCreditAwardBO(String userId, BigDecimal creditAmount) {
		return UserCreditAwardBO.builder().userId(userId).creditAmount(creditAmount).build();
	}

}
