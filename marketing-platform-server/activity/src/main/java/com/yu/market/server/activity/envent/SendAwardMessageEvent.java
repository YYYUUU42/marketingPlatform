package com.yu.market.server.activity.envent;

import com.yu.market.common.event.BaseEvent;
import com.yu.market.common.utils.RandomStringUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author yu
 * @description 用户奖品记录事件消息
 * @date 2025-01-26
 */
@Component
public class SendAwardMessageEvent extends BaseEvent<SendAwardMessageEvent.SendAwardMessage> {

    @Value("${mq.topic.send_award}")
    private String topic;

    @Override
    public com.yu.market.common.event.BaseEvent.EventMessage<SendAwardMessage> buildEventMessage(SendAwardMessage data) {
        return EventMessage.<SendAwardMessage>builder()
                .id(RandomStringUtil.randomNumeric(12))
                .timestamp(new Date())
                .data(data)
                .build();
    }

    @Override
    public String topic() {
        return topic;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SendAwardMessage {
        /**
         * 用户ID
         */
        private String userId;

        /**
         * 订单ID
         */
        private String orderId;

        /**
         * 奖品ID
         */
        private Integer awardId;

        /**
         * 奖品标题（名称）
         */
        private String awardTitle;

        /**
         * 奖品配置信息
         */
        private String awardConfig;
    }

}
