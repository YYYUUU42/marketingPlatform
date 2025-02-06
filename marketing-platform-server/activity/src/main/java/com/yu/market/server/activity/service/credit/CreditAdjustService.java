package com.yu.market.server.activity.service.credit;

import com.yu.market.server.activity.model.bo.CreditAccountBO;
import com.yu.market.server.activity.model.bo.TradeBO;
import com.yu.market.server.activity.respository.ICreditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author yu
 * @description 积分调额服务【正逆向，增减积分】
 * @date 2025-02-06
 */
@Service
@RequiredArgsConstructor
public class CreditAdjustService implements ICreditAdjustService{

	private final ICreditRepository creditRepository;


	/**
	 * 创建增加积分额度订单
	 *
	 * @param tradeBO 交易实体对象
	 * @return 单号
	 */
	@Override
	public String createOrder(TradeBO tradeBO) {
		return "";
	}

	/**
	 * 查询用户积分账户
	 *
	 * @param userId 用户ID
	 * @return 积分账户实体
	 */
	@Override
	public CreditAccountBO queryUserCreditAccount(String userId) {
		return creditRepository.queryUserCreditAccount(userId);
	}
}
