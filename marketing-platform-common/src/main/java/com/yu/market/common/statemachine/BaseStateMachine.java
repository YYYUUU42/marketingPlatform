package com.yu.market.common.statemachine;

import com.yu.market.common.exception.ServiceException;

import java.util.HashMap;
import java.util.Map;

import static com.yu.market.common.exception.errorCode.BaseErrorCode.STATE_MACHINE_TRANSITION_FAILED;


public class BaseStateMachine<STATE, EVENT> implements StateMachine<STATE, EVENT> {
	/**
	 * 状态转换表，存储状态-事件-目标状态的映射
	 */
	private final Map<String, STATE> stateTransitions = new HashMap<>();

	/**
	 * 添加状态转换规则
	 *
	 * @param origin 原始状态
	 * @param event  事件
	 * @param target 目标状态
	 */
	protected void putTransition(STATE origin, EVENT event, STATE target) {
		stateTransitions.put(buildKey(origin, event), target);
	}

	/**
	 * 根据当前状态和事件进行状态转换
	 *
	 * @param state 当前状态
	 * @param event 事件
	 * @return 转换后的状态
	 */
	@Override
	public STATE transition(STATE state, EVENT event) {
		STATE target = stateTransitions.get(buildKey(state, event));
		if (target == null) {
			// 如果没有找到目标状态，抛出业务异常
			throw new ServiceException("state = " + state + " , event = " + event, STATE_MACHINE_TRANSITION_FAILED);
		}
		return target;
	}

	/**
	 * 构建状态-事件对的 key
	 */
	private String buildKey(STATE state, EVENT event) {
		return state.toString() + "_" + event.toString();
	}
}


