package com.yu.market.server.activity.service;

import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.server.activity.model.aggregate.CreateOrderAggregate;
import com.yu.market.server.activity.model.bo.ActivityBO;
import com.yu.market.server.activity.model.bo.ActivityCountBO;
import com.yu.market.server.activity.model.bo.ActivitySkuBO;
import com.yu.market.server.activity.model.bo.SkuRechargeBO;
import com.yu.market.server.activity.respository.IActivityRepository;
import com.yu.market.server.activity.service.rule.IActionChain;
import com.yu.market.server.activity.service.rule.factory.DefaultActivityChainFactory;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.util.StrUtil;

/**
 * @author yu
 * @description 抽奖活动抽象类，定义标准的流程
 * @date 2025-01-23
 */
@Slf4j
public abstract class AbstractRaffleActivity extends RaffleActivitySupport implements IRaffleOrder {

    public AbstractRaffleActivity(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory) {
        super(activityRepository, defaultActivityChainFactory);
    }

    @Override
    public String createSkuRechargeOrder(SkuRechargeBO skuRechargeBO) {
        // 1. 参数校验
        String userId = skuRechargeBO.getUserId();
        Long sku = skuRechargeBO.getSku();
        String outBusinessNo = skuRechargeBO.getOutBusinessNo();
        if (null == sku || StrUtil.isBlank(userId) || StrUtil.isBlank(outBusinessNo)) {
            throw new ServiceException(BaseErrorCode.ILLEGAL_PARAMETER);
        }

        // 2. 查询基础信息
        // 2.1 通过sku查询活动信息
        ActivitySkuBO activitySkuBO = queryActivitySku(sku);
        // 2.2 查询活动信息
        ActivityBO activityBO = queryRaffleActivityByActivityId(activitySkuBO.getActivityId());
        // 2.3 查询次数信息（用户在活动上可参与的次数）
        ActivityCountBO activityCountBO = queryRaffleActivityCountByActivityCountId(activitySkuBO.getActivityCountId());

        // 3. 活动动作规则校验 todo 后续处理规则过滤流程，暂时也不处理责任链结果
        IActionChain actionChain = defaultActivityChainFactory.openActionChain();
        boolean success = actionChain.action(activitySkuBO, activityBO, activityCountBO);

        // 4. 构建订单聚合对象
        CreateOrderAggregate createOrderAggregate = buildOrderAggregate(skuRechargeBO, activitySkuBO, activityBO, activityCountBO);

        // 5. 保存订单
        doSaveOrder(createOrderAggregate);

        // 6. 返回单号
        return createOrderAggregate.getActivityOrderBO().getOrderId();
    }

    protected abstract CreateOrderAggregate buildOrderAggregate(SkuRechargeBO skuRechargeBO, ActivitySkuBO activitySkuBO, ActivityBO activityBO, ActivityCountBO activityCountBO);

    protected abstract void doSaveOrder(CreateOrderAggregate createOrderAggregate);

}
