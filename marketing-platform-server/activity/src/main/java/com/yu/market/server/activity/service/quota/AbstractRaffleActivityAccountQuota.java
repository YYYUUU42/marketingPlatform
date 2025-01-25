package com.yu.market.server.activity.service.quota;

import cn.hutool.core.util.StrUtil;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.server.activity.model.aggregate.CreateQuotaOrderAggregate;
import com.yu.market.server.activity.model.bo.ActivityBO;
import com.yu.market.server.activity.model.bo.ActivityCountBO;
import com.yu.market.server.activity.model.bo.ActivitySkuBO;
import com.yu.market.server.activity.model.bo.SkuRechargeBO;
import com.yu.market.server.activity.respository.IActivityRepository;
import com.yu.market.server.activity.service.IRaffleActivityAccountQuotaService;
import com.yu.market.server.activity.service.quota.rule.IActionChain;
import com.yu.market.server.activity.service.quota.rule.factory.DefaultActivityChainFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yu
 * @description 抽奖活动抽象类 - 定义标准的流程
 * @date 2025-01-26
 */
@Slf4j
public abstract class AbstractRaffleActivityAccountQuota extends RaffleActivityAccountQuotaSupport implements IRaffleActivityAccountQuotaService {

	public AbstractRaffleActivityAccountQuota(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory) {
		super(activityRepository, defaultActivityChainFactory);
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
	public String createOrder(SkuRechargeBO skuRechargeBO) {
		// 参数校验
		String userId = skuRechargeBO.getUserId();
		Long sku = skuRechargeBO.getSku();
		String outBusinessNo = skuRechargeBO.getOutBusinessNo();
		if (sku == null || StrUtil.isBlank(userId) || StrUtil.isBlank(outBusinessNo)) {
			throw new ServiceException(BaseErrorCode.ILLEGAL_PARAMETER);
		}

		// 通过sku查询活动信息
		ActivitySkuBO activitySkuBO = queryActivitySku(sku);

		// 查询活动信息
		ActivityBO activityBO = queryRaffleActivityByActivityId(activitySkuBO.getActivityId());

		// 查询次数信息（用户在活动上可参与的次数）
		ActivityCountBO activityCountBO = queryRaffleActivityCountByActivityCountId(activitySkuBO.getActivityCountId());

		// 活动动作规则校验
		IActionChain actionChain = defaultActivityChainFactory.openActionChain();
		actionChain.action(activitySkuBO, activityBO, activityCountBO);

		// 构建订单聚合对象
		CreateQuotaOrderAggregate createOrderAggregate = buildOrderAggregate(skuRechargeBO, activitySkuBO, activityBO, activityCountBO);

		// 保存订单
		doSaveOrder(createOrderAggregate);

		// 返回单号
		return createOrderAggregate.getActivityOrderBO().getOrderId();
	}

	protected abstract CreateQuotaOrderAggregate buildOrderAggregate(SkuRechargeBO skuRechargeBO, ActivitySkuBO activitySkuBO, ActivityBO activityBO, ActivityCountBO activityCountBO);

	protected abstract void doSaveOrder(CreateQuotaOrderAggregate createOrderAggregate);
}
