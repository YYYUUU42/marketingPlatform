package com.yu.market.server.activity.respository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yu.market.common.contants.Constants;
import com.yu.market.common.contants.RedisKey;
import com.yu.market.common.redis.IRedisService;
import com.yu.market.server.activity.mapper.UserCreditAccountMapper;
import com.yu.market.server.activity.mapper.UserCreditOrderMapper;
import com.yu.market.server.activity.model.aggregate.TradeAggregate;
import com.yu.market.server.activity.model.bo.CreditAccountBO;
import com.yu.market.server.activity.model.bo.CreditOrderBO;
import com.yu.market.server.activity.model.pojo.UserCreditAccount;
import com.yu.market.server.activity.model.pojo.UserCreditOrder;
import com.yu.market.server.activity.respository.ICreditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author yu
 * @description 用户积分
 * @date 2025-01-27
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CreditRepository implements ICreditRepository {

	private final IRedisService redisService;
	private final UserCreditAccountMapper userCreditAccountMapper;
	private final UserCreditOrderMapper userCreditOrderMapper;
	private final TransactionTemplate transactionTemplate;

	@Override
	public void saveUserCreditTradeOrder(TradeAggregate tradeAggregate) {
		String userId = tradeAggregate.getUserId();
		CreditAccountBO creditAccountBO = tradeAggregate.getCreditAccountBO();
		CreditOrderBO creditOrderBO = tradeAggregate.getCreditOrderBO();

		// 积分账户
		UserCreditAccount userCreditAccount = new UserCreditAccount();
		userCreditAccount.setUserId(userId);
		userCreditAccount.setTotalAmount(creditAccountBO.getAdjustAmount());
		userCreditAccount.setAvailableAmount(creditAccountBO.getAdjustAmount());

		// 积分订单
		UserCreditOrder userCreditOrder = new UserCreditOrder();
		userCreditOrder.setUserId(creditOrderBO.getUserId());
		userCreditOrder.setOrderId(creditOrderBO.getOrderId());
		userCreditOrder.setTradeName(creditOrderBO.getTradeName().getName());
		userCreditOrder.setTradeType(creditOrderBO.getTradeType().getCode());
		userCreditOrder.setTradeAmount(creditOrderBO.getTradeAmount());
		userCreditOrder.setOutBusinessNo(creditOrderBO.getOutBusinessNo());

		RLock lock = redisService.getLock(RedisKey.USER_CREDIT_ACCOUNT_LOCK + userId + Constants.UNDERLINE + creditOrderBO.getOutBusinessNo());

		lock.lock(3, TimeUnit.SECONDS);
		transactionTemplate.execute(status -> {
			try {
				// 保存账户积分
				Long userCreditAccountCount = userCreditAccountMapper.selectCount(new LambdaQueryWrapper<UserCreditAccount>()
						.eq(UserCreditAccount::getUserId, userId));
				if (userCreditAccountCount == 0) {
					userCreditAccountMapper.insert(userCreditAccount);
				} else {
					userCreditAccountMapper.updateAddAmount(userCreditAccount);
				}

				// 保存账户订单
				userCreditOrderMapper.insert(userCreditOrder);

			} catch (DuplicateKeyException e) {
				status.setRollbackOnly();
				log.error("调整账户积分额度异常，唯一索引冲突 userId:{} orderId:{}", userId, creditOrderBO.getOrderId(), e);
			} catch (Exception e) {
				status.setRollbackOnly();
				log.error("调整账户积分额度失败 userId:{} orderId:{}", userId, creditOrderBO.getOrderId(), e);
			}
			return 1;
		});
	}


}
