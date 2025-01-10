package com.yu.market.server.raffle.service.rule.chain;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yu
 * @description 抽奖策略责任链，判断走那种抽奖策略。如；默认抽象、权重抽奖、黑名单抽奖
 * @date 2025-01-10
 */
@Slf4j
public abstract class AbstractLogicChain implements ILogicChain {

    private ILogicChain next;

    @Override
    public ILogicChain next() {
        return next;
    }

    @Override
    public ILogicChain appendNext(ILogicChain next) {
        this.next = next;
        return next;
    }

    protected abstract String getRuleModel();

}