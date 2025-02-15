package com.yu.market.server.coupon.mq.producer;

import cn.hutool.core.util.StrUtil;
import com.yu.market.server.coupon.mq.base.BaseSendExtendDTO;
import com.yu.market.server.coupon.mq.base.MessageWrapper;
import com.yu.market.server.coupon.mq.event.CouponTaskExecuteEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.yu.market.common.contants.CouponRocketMQConstant.MerchantAdminRocketMQConstant.TEMPLATE_TASK_EXECUTE_TOPIC_KEY;

/**
 * @author yu
 * @description 优惠券推送任务执行生产者
 * @date 2025-02-15
 */
@Slf4j
@Component
public class CouponTaskActualExecuteProducer extends AbstractCommonSendProduceTemplate<CouponTaskExecuteEvent> {

    private final ConfigurableEnvironment environment;

    public CouponTaskActualExecuteProducer(@Autowired RocketMQTemplate rocketMQTemplate, @Autowired ConfigurableEnvironment environment) {
        super(rocketMQTemplate);
        this.environment = environment;
    }

    @Override
    protected BaseSendExtendDTO buildBaseSendExtendParam(CouponTaskExecuteEvent messageSendEvent) {
        return BaseSendExtendDTO.builder()
                .eventName("优惠券推送执行")
                .keys(String.valueOf(messageSendEvent.getCouponTaskId()))
                .delayTime(messageSendEvent.getDelayTime())
                .topic(environment.resolvePlaceholders(TEMPLATE_TASK_EXECUTE_TOPIC_KEY))
                .sentTimeout(2000L)
                .build();
    }

    @Override
    protected Message<?> buildMessage(CouponTaskExecuteEvent messageSendEvent, BaseSendExtendDTO requestParam) {
        String keys = StrUtil.isEmpty(requestParam.getKeys()) ? UUID.randomUUID().toString() : requestParam.getKeys();
        return MessageBuilder
                .withPayload(new MessageWrapper(keys, messageSendEvent))
                .setHeader(MessageConst.PROPERTY_KEYS, keys)
                .setHeader(MessageConst.PROPERTY_TAGS, requestParam.getTag())
                .build();
    }
}
