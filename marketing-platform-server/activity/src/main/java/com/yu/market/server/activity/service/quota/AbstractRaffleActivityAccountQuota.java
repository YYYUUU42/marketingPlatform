package com.yu.market.server.activity.service.quota;

import cn.hutool.core.util.StrUtil;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.server.activity.model.aggregate.CreateQuotaOrderAggregate;
import com.yu.market.server.activity.model.bo.*;
import com.yu.market.server.activity.model.enums.OrderTradeTypeEnum;
import com.yu.market.server.activity.respository.IActivityRepository;
import com.yu.market.server.activity.service.IRaffleActivityAccountQuotaService;
import com.yu.market.server.activity.service.quota.policy.ITradePolicy;
import com.yu.market.server.activity.service.quota.rule.IActionChain;
import com.yu.market.server.activity.service.quota.rule.factory.DefaultActivityChainFactory;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author yu
 * @description 抽奖活动抽象类 - 定义标准的流程
 * @date 2025-01-26
 */
@Slf4j
public abstract class AbstractRaffleActivityAccountQuota extends RaffleActivityAccountQuotaSupport implements IRaffleActivityAccountQuotaService {

	private final Map<String, ITradePolicy> tradePolicyGroup;
	
	public AbstractRaffleActivityAccountQuota(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory, Map<String, ITradePolicy> tradePolicyGroup) {
		super(activityRepository, defaultActivityChainFactory);
		this.tradePolicyGroup = tradePolicyGroup;
	}

	/**
	 * 创建 sku 账户充值订单，给用户增加抽奖次数
	 * 1. 在【打卡、签到、分享、对话、积分兑换】等行为动作下，创建出活动订单，给用户的活动账户【日、月】充值可用的抽奖次数。
	 * 2. 对于用户可获得的抽奖次数，比如首次进来就有一次，则是依赖于运营配置的动作，在前端页面上。用户点击后，可以获得一次抽奖次数。
	 *
	 * @param skuRechargeBO 活动商品充值实体对象
	 * @return 活动ID
	 */
	@Override
	public UnpaidActivityOrderBO createOrder(SkuRechargeBO skuRechargeBO) {
		// 参数校验
		String userId = skuRechargeBO.getUserId();
		Long sku = skuRechargeBO.getSku();
		String outBusinessNo = skuRechargeBO.getOutBusinessNo();
		if (sku == null || StrUtil.isBlank(userId) || StrUtil.isBlank(outBusinessNo)) {
			throw new ServiceException(BaseErrorCode.ILLEGAL_PARAMETER);
		}

		// 查询未支付订单「一个月以内的未支付订单」& 支付类型查询，非支付的走兑换
		if (skuRechargeBO.getOrderTradeType().equals(OrderTradeTypeEnum.credit_pay_trade)) {
			UnpaidActivityOrderBO unpaidActivityOrderBO = activityRepository.queryUnpaidActivityOrder(skuRechargeBO);
			if (unpaidActivityOrderBO != null) {
				return unpaidActivityOrderBO;
			}
		}

		// 查询基础信息（sku、活动、次数）
		ActivitySkuBO activitySkuBO = queryActivitySku(sku);
		ActivityBO activityBO = queryRaffleActivityByActivityId(activitySkuBO.getActivityId());
		ActivityCountBO activityCountBO = queryRaffleActivityCountByActivityCountId(activitySkuBO.getActivityCountId());

		// 账户额度 【交易属性的兑换，需要校验额度账户】
		if (skuRechargeBO.getOrderTradeType().equals(OrderTradeTypeEnum.credit_pay_trade)) {
			BigDecimal availableAmount = activityRepository.queryUserCreditAccountAmount(userId);
			if (activitySkuBO.getProductAmount().compareTo(availableAmount) > 0){
				throw new ServiceException(BaseErrorCode.USER_CREDIT_ACCOUNT_NO_AVAILABLE_AMOUNT);
			}
		}

		// 活动动作规则校验
		IActionChain actionChain = defaultActivityChainFactory.openActionChain();
		actionChain.action(activitySkuBO, activityBO, activityCountBO);

		// 构建订单聚合对象
		CreateQuotaOrderAggregate createOrderAggregate = buildOrderAggregate(skuRechargeBO, activitySkuBO, activityBO, activityCountBO);

		// 交易策略 - 【积分兑换，支付类订单】【返利无支付交易订单，直接充值到账】【订单状态变更交易类型策略】
		ITradePolicy tradePolicy = tradePolicyGroup.get(skuRechargeBO.getOrderTradeType().getCode());
		tradePolicy.trade(createOrderAggregate);

		// 返回订单信息
		ActivityOrderBO activityOrderBO = createOrderAggregate.getActivityOrderBO();
		return UnpaidActivityOrderBO.builder()
				.userId(userId)
				.orderId(activityOrderBO.getOrderId())
				.outBusinessNo(activityOrderBO.getOutBusinessNo())
				.payAmount(activityOrderBO.getPayAmount())
				.build();
	}

	protected abstract CreateQuotaOrderAggregate buildOrderAggregate(SkuRechargeBO skuRechargeBO, ActivitySkuBO activitySkuBO, ActivityBO activityBO, ActivityCountBO activityCountBO);
}
