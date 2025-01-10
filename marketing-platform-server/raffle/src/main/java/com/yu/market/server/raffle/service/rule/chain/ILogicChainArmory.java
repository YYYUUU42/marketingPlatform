package com.yu.market.server.raffle.service.rule.chain;

/**
 * @author yu
 * @description 责任链装配
 * @date 2025-01-10
 */
public interface ILogicChainArmory {

    /**
     * 责任链的下一个
     */
    ILogicChain next();

    /**
     * 责任链中添加下一个
     */
    ILogicChain appendNext(ILogicChain next);

}
