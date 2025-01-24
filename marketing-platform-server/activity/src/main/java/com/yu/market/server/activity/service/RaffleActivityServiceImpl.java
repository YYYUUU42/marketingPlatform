package com.yu.market.server.activity.service;

import com.yu.market.common.utils.SnowFlakeUtil;
import com.yu.market.server.activity.model.aggregate.CreateOrderAggregate;
import com.yu.market.server.activity.model.bo.*;
import com.yu.market.server.activity.model.enums.OrderStateEnum;
import com.yu.market.server.activity.respository.IActivityRepository;
import com.yu.market.server.activity.service.rule.factory.DefaultActivityChainFactory;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author yu
 * @description 抽奖活动服务
 * @date 2025-01-23
 */
@Service
public class RaffleActivityServiceImpl extends AbstractRaffleActivity implements ISkuStock{

	public RaffleActivityServiceImpl(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory) {
		super(activityRepository, defaultActivityChainFactory);
	}

	@Override
	protected CreateOrderAggregate buildOrderAggregate(SkuRechargeBO skuRechargeBO, ActivitySkuBO activitySkuBO, ActivityBO activityBO, ActivityCountBO activityCountBO) {
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
				.state(OrderStateEnum.completed)
				.outBusinessNo(skuRechargeBO.getOutBusinessNo())
				.build();

		// 构建聚合对象
		return CreateOrderAggregate.builder()
				.userId(skuRechargeBO.getUserId())
				.activityId(activitySkuBO.getActivityId())
				.totalCount(activityCountBO.getTotalCount())
				.dayCount(activityCountBO.getDayCount())
				.monthCount(activityCountBO.getMonthCount())
				.activityOrderBO(activityOrderBO)
				.build();
	}

	@Override
	protected void doSaveOrder(CreateOrderAggregate createOrderAggregate) {
		activityRepository.doSaveOrder(createOrderAggregate);
	}

	/**
	 * 获取活动sku库存消耗队列
	 *
	 * @return 奖品库存Key信息
	 * @throws InterruptedException 异常
	 */
	@Override
	public ActivitySkuStockKeyBO takeQueueValue() throws InterruptedException {
		return null;
	}

	/**
	 * 清空队列
	 */
	@Override
	public void clearQueueValue() {

	}

	/**
	 * 延迟队列 + 任务趋势更新活动sku库存
	 *
	 * @param sku 活动商品
	 */
	@Override
	public void updateActivitySkuStock(Long sku) {

	}

	/**
	 * 缓存库存以消耗完毕，清空数据库库存
	 *
	 * @param sku 活动商品
	 */
	@Override
	public void clearActivitySkuStock(Long sku) {

	}
}
