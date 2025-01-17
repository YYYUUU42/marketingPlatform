package com.yu.market.server.raffle.service.rule.chain.impl;

import com.yu.market.server.raffle.service.rule.chain.AbstractLogicChain;
import com.yu.market.server.raffle.service.armory.IStrategyDispatch;
import com.yu.market.server.raffle.service.rule.chain.factory.DefaultChainFactory;
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
    public DefaultChainFactory.StrategyAward logic(String userId, Long strategyId) {
        Integer awardId = strategyDispatch.getRandomAwardId(strategyId);
        log.info("抽奖责任链 - 默认处理 userId: {} strategyId: {} ruleModel: {} awardId: {}", userId, strategyId, getRuleModel(), awardId);
        return DefaultChainFactory.StrategyAward.builder()
                .awardId(awardId)
                .logicModel(getRuleModel())
                .build();
    }

    @Override
    protected String getRuleModel() {
        return DefaultChainFactory.LogicModel.RULE_DEFAULT.getCode();
    }

}
