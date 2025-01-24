package com.yu.market.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author yu
 * @description 基础事件
 * @date 2025-01-24
 */
@Data
public abstract class BaseEvent<T> {

	public abstract EventMessage<T> buildEventMessage(T data);

	public abstract String topic();

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class EventMessage<T> {
		private String id;
		private Date timestamp;
		private T data;
	}

}
