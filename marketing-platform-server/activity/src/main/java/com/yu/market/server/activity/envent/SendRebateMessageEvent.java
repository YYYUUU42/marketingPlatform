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
 * @description 发送返利消息事件
 * @date 2025-01-26
 */
@Component
public class SendRebateMessageEvent extends BaseEvent<SendRebateMessageEvent.RebateMessage> {

	@Value("${mq.topic.send_rebate}")
	private String topic;

	@Override
	public EventMessage<RebateMessage> buildEventMessage(RebateMessage data) {
		return EventMessage.<RebateMessage>builder()
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
	public static class RebateMessage {
		/**
		 * 用户ID
		 */
		private String userId;

		/**
		 * 返利描述
		 */
		private String rebateDesc;

		/**
		 * 返利类型
		 */
		private String rebateType;

		/**
		 * 返利配置
		 */
		private String rebateConfig;

		/**
		 * 业务ID - 唯一ID，确保幂等
		 */
		private String bizId;
	}

}
