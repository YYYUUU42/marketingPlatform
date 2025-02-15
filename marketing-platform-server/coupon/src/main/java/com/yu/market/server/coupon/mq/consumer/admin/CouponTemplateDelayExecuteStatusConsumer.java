package com.yu.market.server.coupon.mq.consumer.admin;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yu.market.infrastructure.coupon.mapper.CouponTemplateMapper;
import com.yu.market.infrastructure.coupon.pojo.CouponTemplate;
import com.yu.market.server.coupon.model.enums.CouponTemplateStatusEnum;
import com.yu.market.server.coupon.mq.base.MessageWrapper;
import com.yu.market.server.coupon.mq.event.CouponTemplateDelayEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import static com.yu.market.common.contants.CouponRocketMQConstant.MerchantAdminRocketMQConstant.TEMPLATE_TEMPLATE_DELAY_STATUS_CG_KEY;
import static com.yu.market.common.contants.CouponRocketMQConstant.MerchantAdminRocketMQConstant.TEMPLATE_TEMPLATE_DELAY_TOPIC_KEY;

/**
 * 优惠券模板推送延迟执行-变更记录状态消费者
 * 开发时间：2024-08-21
 */
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = TEMPLATE_TEMPLATE_DELAY_TOPIC_KEY,
        consumerGroup = TEMPLATE_TEMPLATE_DELAY_STATUS_CG_KEY
)
@Slf4j(topic = "CouponTemplateDelayExecuteStatusConsumer")
public class CouponTemplateDelayExecuteStatusConsumer implements RocketMQListener<MessageWrapper<CouponTemplateDelayEvent>> {

    private final CouponTemplateMapper couponTemplateMapper;

    @Override
    public void onMessage(MessageWrapper<CouponTemplateDelayEvent> messageWrapper) {
        // 开头打印日志，平常可 Debug 看任务参数，线上可报平安（比如消息是否消费，重新投递时获取参数等）
        log.info("[消费者] 优惠券模板定时执行@变更模板表状态 - 执行消费逻辑，消息体：{}", JSON.toJSONString(messageWrapper));

        // 修改指定优惠券模板状态为已结束
        CouponTemplateDelayEvent message = messageWrapper.getMessage();
        LambdaUpdateWrapper<CouponTemplate> updateWrapper = Wrappers.lambdaUpdate(CouponTemplate.class)
                .eq(CouponTemplate::getShopNumber, message.getShopNumber())
                .eq(CouponTemplate::getId, message.getCouponTemplateId());
        CouponTemplate couponTemplateDO = CouponTemplate.builder()
                .status(CouponTemplateStatusEnum.ENDED.getStatus())
                .build();

        couponTemplateMapper.update(couponTemplateDO, updateWrapper);
    }
}
