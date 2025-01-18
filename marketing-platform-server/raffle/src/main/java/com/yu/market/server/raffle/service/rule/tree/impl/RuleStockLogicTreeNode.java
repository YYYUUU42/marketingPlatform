package com.yu.market.server.raffle.service.rule.tree.impl;

import com.yu.market.server.raffle.model.bo.StrategyAwardStockKeyBO;
import com.yu.market.server.raffle.model.enums.RuleLogicCheckType;
import com.yu.market.server.raffle.repository.IStrategyRepository;
import com.yu.market.server.raffle.service.armory.IStrategyDispatch;
import com.yu.market.server.raffle.service.rule.tree.ILogicTreeNode;
import com.yu.market.server.raffle.service.rule.tree.factory.DefaultTreeFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author yu
 * @description 库存扣减节点 - 检查并扣减库存
 * @date 2025-01-16
 */
@Slf4j
@Component("rule_stock")
@RequiredArgsConstructor
public class RuleStockLogicTreeNode implements ILogicTreeNode {

    private final IStrategyDispatch strategyDispatch;
    private final IStrategyRepository strategyRepository;

    /**
     * 执行库存扣减节点逻辑
     *
     * @param userId    用户ID
     * @param strategyId 策略ID
     * @param awardId   奖励ID
     * @return 决策结果
     */
    @Override
    public DefaultTreeFactory.TreeActionBO logic(String userId, Long strategyId, Integer awardId, String ruleValue) {
        log.info("规则过滤-库存扣减 userId:{} strategyId:{} awardId:{}", userId, strategyId, awardId);
        // 扣减库存
        Boolean status = strategyDispatch.subtractionAwardStock(strategyId, awardId);
        // true；库存扣减成功，TAKE_OVER 规则节点接管，返回奖品ID，奖品规则配置
        if (status) {
            log.info("规则过滤-库存扣减-成功 userId:{} strategyId:{} awardId:{}", userId, strategyId, awardId);

            // 写入延迟队列，延迟消费更新数据库记录。【在trigger的job；UpdateAwardStockJob 下消费队列，更新数据库记录】
            strategyRepository.awardStockConsumeSendQueue(StrategyAwardStockKeyBO.builder()
                    .strategyId(strategyId)
                    .awardId(awardId)
                    .build());

            return DefaultTreeFactory.TreeActionBO.builder()
                    .ruleLogicCheckType(RuleLogicCheckType.TAKE_OVER)
                    .strategyAward(DefaultTreeFactory.StrategyAward.builder()
                            .awardId(awardId)
                            .awardRuleValue(ruleValue)
                            .build())
                    .build();
        }

        // 如果库存不足，则直接返回放行
        log.warn("规则过滤-库存扣减-告警，库存不足。userId:{} strategyId:{} awardId:{}", userId, strategyId, awardId);
        return DefaultTreeFactory.TreeActionBO.builder()
                .ruleLogicCheckType(RuleLogicCheckType.ALLOW)
                .build();
    }
}
