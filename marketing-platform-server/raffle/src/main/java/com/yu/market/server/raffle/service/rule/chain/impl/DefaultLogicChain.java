package com.yu.market.server.raffle.service.rule.chain.impl;

import com.yu.market.server.raffle.service.rule.chain.AbstractLogicChain;
import com.yu.market.server.raffle.service.armory.IStrategyDispatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * @author yu
 * @description 默认的责任链 - 作为最后一个链
 * @date 2025-01-10
 */
@Slf4j
@Component("default")
@RequiredArgsConstructor
public class DefaultLogicChain extends AbstractLogicChain {

    protected final IStrategyDispatch strategyDispatch;

    @Override
    public Integer logic(String userId, Long strategyId) {
        Integer awardId = strategyDispatch.getRandomAwardId(strategyId);
        log.info("抽奖责任链-默认处理 userId: {} strategyId: {} ruleModel: {} awardId: {}", userId, strategyId, getRuleModel(), awardId);
        return awardId;
    }

    @Override
    protected String getRuleModel() {
        return "default";
    }

}
