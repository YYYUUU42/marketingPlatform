package com.yu.market.server.activity.service.rule;

/**
 * @author yu
 * @description 下单规则过滤装配
 * @date 2025-01-23
 */
public interface IActionChainArmory {

    /**
     * 下一个
     */
    IActionChain next();

    /**
     * 添加下一个
     */
    IActionChain appendNext(IActionChain next);

}
