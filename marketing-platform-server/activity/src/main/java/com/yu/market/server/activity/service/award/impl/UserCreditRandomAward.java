package com.yu.market.server.activity.service.award.impl;

import cn.hutool.core.util.StrUtil;
import com.yu.market.common.contants.Constants;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.server.activity.model.aggregate.GiveOutPrizesAggregate;
import com.yu.market.server.activity.model.bo.DistributeAwardBO;
import com.yu.market.server.activity.model.bo.UserAwardRecordBO;
import com.yu.market.server.activity.model.bo.UserCreditAwardBO;
import com.yu.market.server.activity.model.enums.AwardStateEnum;
import com.yu.market.server.activity.respository.IAwardRepository;
import com.yu.market.server.activity.service.award.IDistributeAward;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * @author yu
 * @description 用户积分奖品，支持 award_config 透传，满足黑名单积分奖励。
 * @date 2025-01-27
 */
@Component("user_credit_random")
@RequiredArgsConstructor
public class UserCreditRandomAward implements IDistributeAward {

	private final IAwardRepository awardRepository;


	@Override
	public void giveOutPrizes(DistributeAwardBO distributeAwardBO) {
		// 查询奖品配置
		String awardConfig = distributeAwardBO.getAwardConfig();
		if (StrUtil.isBlank(awardConfig)) {
			awardRepository.queryAwardConfig(distributeAwardBO.getAwardId());
		}

		String[] creditRange = awardConfig.split(Constants.SPLIT);
		if (creditRange.length != 2) {
			throw new ServiceException("award_config 「" + awardConfig + "」配置不是一个范围值，如 1,100");
		}

		// 生成随机积分值
		BigDecimal creditAmount = generateRandom(new BigDecimal(creditRange[0]), new BigDecimal(creditRange[1]));

		// 构建聚合对象
		UserAwardRecordBO userAwardRecordBO = GiveOutPrizesAggregate.buildDistributeUserAwardRecordBO(
				distributeAwardBO.getUserId(),
				distributeAwardBO.getOrderId(),
				distributeAwardBO.getAwardId(),
				AwardStateEnum.complete
		);

		UserCreditAwardBO userCreditAwardBO = GiveOutPrizesAggregate.buildUserCreditAwardBO(distributeAwardBO.getUserId(), creditAmount);

		GiveOutPrizesAggregate giveOutPrizesAggregate = new GiveOutPrizesAggregate();
		giveOutPrizesAggregate.setUserId(distributeAwardBO.getUserId());
		giveOutPrizesAggregate.setUserAwardRecordBO(userAwardRecordBO);
		giveOutPrizesAggregate.setUserCreditAwardBO(userCreditAwardBO);

		// 存储发奖对象
		awardRepository.saveGiveOutPrizesAggregate(giveOutPrizesAggregate);
	}

	/**
	 * 生成随机值
	 */
	private BigDecimal generateRandom(BigDecimal min, BigDecimal max) {
		if (min.equals(max)) {
			return min;
		}
		BigDecimal randomBigDecimal = min.add(BigDecimal.valueOf(Math.random()).multiply(max.subtract(min)));

		return randomBigDecimal.round(new MathContext(3));
	}
}
