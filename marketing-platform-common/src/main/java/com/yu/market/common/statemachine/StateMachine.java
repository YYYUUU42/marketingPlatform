package com.yu.market.common.statemachine;

/**
 * @author yu
 * @description 状态机接口
 * @date 2025-01-19
 */
public interface StateMachine<STATE, EVENT> {

    /**
     * 状态机转移
     *
     * @param state 当前状态
     * @param event 触发状态转换的事件
     * @return 转换后的状态
     */
    STATE transition(STATE state, EVENT event);
}

