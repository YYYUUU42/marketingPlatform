package com.yu.market.server.activity.service.quota;


import com.yu.market.common.utils.SnowFlakeUtil;
import com.yu.market.server.activity.model.aggregate.CreateQuotaOrderAggregate;
import com.yu.market.server.activity.model.bo.*;
import com.yu.market.server.activity.model.enums.OrderStateEnum;
import com.yu.market.server.activity.respository.IActivityRepository;
import com.yu.market.server.activity.service.IRaffleActivitySkuStockService;
import com.yu.market.server.activity.service.quota.policy.ITradePolicy;
import com.yu.market.server.activity.service.quota.rule.factory.DefaultActivityChainFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * @author yu
 * @description 抽奖活动服务
 * @date 2025-01-26
 */
@Service
public class RaffleActivityAccountQuotaService extends AbstractRaffleActivityAccountQuota implements IRaffleActivitySkuStockService {

	public RaffleActivityAccountQuotaService(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory, Map<String, ITradePolicy> tradePolicyGroup) {
		super(activityRepository, defaultActivityChainFactory, tradePolicyGroup);
	}

	@Override
	protected CreateQuotaOrderAggregate buildOrderAggregate(SkuRechargeBO skuRechargeBO, ActivitySkuBO activitySkuBO, ActivityBO activityBO, ActivityCountBO activityCountBO) {
		// 订单实体对象
		ActivityOrderBO activityOrderBO = ActivityOrderBO.builder()
				.userId(skuRechargeBO.getUserId())
				.sku(skuRechargeBO.getSku())
				.activityId(activityBO.getActivityId())
				.activityName(activityBO.getActivityName())
				.strategyId(activityBO.getStrategyId())
				.orderId(String.valueOf(new SnowFlakeUtil().nextId()))
				.orderTime(new Date())
				.totalCount(activityCountBO.getTotalCount())
				.dayCount(activityCountBO.getDayCount())
				.monthCount(activityCountBO.getMonthCount())
				.payAmount(activitySkuBO.getProductAmount())
				.outBusinessNo(skuRechargeBO.getOutBusinessNo())
				.build();

		// 构建聚合对象
		return CreateQuotaOrderAggregate.builder()
				.userId(skuRechargeBO.getUserId())
				.activityId(activitySkuBO.getActivityId())
				.totalCount(activityCountBO.getTotalCount())
				.dayCount(activityCountBO.getDayCount())
				.monthCount(activityCountBO.getMonthCount())
				.activityOrderBO(activityOrderBO)
				.build();
	}

	/**
	 * 获取活动sku库存消耗队列
	 *
	 * @return 奖品库存Key信息
	 * @throws InterruptedException 异常
	 */
	@Override
	public ActivitySkuStockKeyBO takeQueueValue() throws InterruptedException {
		return activityRepository.takeQueueValue();
	}

	/**
	 * 清空队列
	 */
	@Override
	public void clearQueueValue() {
		activityRepository.clearQueueValue();
	}

	/**
	 * 延迟队列 + 任务趋势更新活动sku库存
	 *
	 * @param sku 活动商品
	 */
	@Override
	public void updateActivitySkuStock(Long sku) {
		activityRepository.updateActivitySkuStock(sku);
	}

	/**
	 * 缓存库存以消耗完毕，清空数据库库存
	 *
	 * @param sku 活动商品
	 */
	@Override
	public void clearActivitySkuStock(Long sku) {
		activityRepository.clearActivitySkuStock(sku);
	}

	/**
	 * 查询活动账户额度「总、月、日」
	 *
	 * @param activityId 活动ID
	 * @param userId     用户ID
	 * @return 账户实体
	 */
	@Override
	public ActivityAccountBO queryActivityAccountBO(Long activityId, String userId) {
		return activityRepository.queryActivityAccountBO(activityId,userId);
	}

	/**
	 * 订单出货 - 积分充值
	 *
	 * @param deliveryOrderEntity 出货单实体对象
	 */
	@Override
	public void updateOrder(DeliveryOrderBO deliveryOrderEntity) {
		activityRepository.updateOrder(deliveryOrderEntity);
	}
}
