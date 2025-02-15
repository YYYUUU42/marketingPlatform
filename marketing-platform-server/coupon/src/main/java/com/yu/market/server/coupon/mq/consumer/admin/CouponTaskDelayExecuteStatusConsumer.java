package com.yu.market.server.coupon.mq.consumer.admin;

import com.alibaba.fastjson2.JSON;
import com.yu.market.infrastructure.coupon.mapper.CouponTaskMapper;
import com.yu.market.infrastructure.coupon.pojo.CouponTask;
import com.yu.market.server.coupon.mq.base.MessageWrapper;
import com.yu.market.server.coupon.mq.event.CouponTaskDelayEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import static com.yu.market.common.contants.CouponRocketMQConstant.MerchantAdminRocketMQConstant.TEMPLATE_TASK_DELAY_STATUS_CG_KEY;
import static com.yu.market.common.contants.CouponRocketMQConstant.MerchantAdminRocketMQConstant.TEMPLATE_TASK_DELAY_TOPIC_KEY;

/**
 * 优惠券推送延迟执行-变更记录发送状态消费者
 * 开发时间：2024-07-13
 */
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = TEMPLATE_TASK_DELAY_TOPIC_KEY,
        consumerGroup = TEMPLATE_TASK_DELAY_STATUS_CG_KEY
)
@Slf4j(topic = "CouponTaskDelayExecuteStatusConsumer")
public class CouponTaskDelayExecuteStatusConsumer implements RocketMQListener<MessageWrapper<CouponTaskDelayEvent>> {

    private final CouponTaskMapper couponTaskMapper;

    @Override
    public void onMessage(MessageWrapper<CouponTaskDelayEvent> messageWrapper) {
        // 开头打印日志，平常可 Debug 看任务参数，线上可报平安（比如消息是否消费，重新投递时获取参数等）
        log.info("[消费者] 优惠券推送定时执行@变更记录发送状态 - 执行消费逻辑，消息体：{}", JSON.toJSONString(messageWrapper));

        // 修改延时执行推送任务任务状态为执行中
        CouponTaskDelayEvent message = messageWrapper.getMessage();
        CouponTask couponTask = CouponTask.builder()
                .id(message.getCouponTaskId())
                .status(message.getStatus())
                .build();
        couponTaskMapper.updateById(couponTask);
    }
}
