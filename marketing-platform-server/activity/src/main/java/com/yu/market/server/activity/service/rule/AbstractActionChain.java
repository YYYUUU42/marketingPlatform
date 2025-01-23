package com.yu.market.server.activity.service.rule;

/**
 * @author yu
 * @description 下单规则责任链抽象类
 * @date 2025-01-23
 */
public abstract class AbstractActionChain implements IActionChain {

    private IActionChain next;

    @Override
    public IActionChain next() {
        return next;
    }

    @Override
    public IActionChain appendNext(IActionChain next) {
        this.next = next;
        return next;
    }

}
