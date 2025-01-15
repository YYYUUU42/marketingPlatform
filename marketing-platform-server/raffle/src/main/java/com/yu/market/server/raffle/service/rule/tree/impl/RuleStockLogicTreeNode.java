package com.yu.market.server.raffle.service.rule.tree.impl;

import com.yu.market.server.raffle.model.enums.RuleLogicCheckType;
import com.yu.market.server.raffle.service.rule.tree.ILogicTreeNode;
import com.yu.market.server.raffle.service.rule.tree.factory.DefaultTreeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author yu
 * @description 库存扣减节点 - 检查并扣减库存
 * @date 2025-01-16
 */
@Slf4j
@Component("rule_stock")
public class RuleStockLogicTreeNode implements ILogicTreeNode {

    /**
     * 执行库存扣减节点逻辑
     *
     * @param userId    用户ID
     * @param strategyId 策略ID
     * @param awardId   奖励ID
     * @return 决策结果
     */
    @Override
    public DefaultTreeFactory.TreeActionBO logic(String userId, Long strategyId, Integer awardId) {
        log.info("执行库存扣减节点逻辑 - 用户ID: {}, 策略ID: {}, 奖励ID: {}", userId, strategyId, awardId);

        // TODO: 库存校验逻辑
        boolean stockAvailable = true;

        if (stockAvailable) {
            log.info("库存扣减成功 - 用户ID: {}, 策略ID: {}", userId, strategyId);
            return DefaultTreeFactory.TreeActionBO.builder()
                    .ruleLogicCheckType(RuleLogicCheckType.TAKE_OVER)
                    .build();
        } else {
            log.warn("库存不足，无法扣减 - 用户ID: {}, 策略ID: {}", userId, strategyId);
            return DefaultTreeFactory.TreeActionBO.builder()
                    .ruleLogicCheckType(RuleLogicCheckType.TAKE_OVER)
                    .build();
        }
    }
}
