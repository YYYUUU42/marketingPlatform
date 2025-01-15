package com.yu.market.server.raffle.service.rule.tree.impl;

import com.yu.market.server.raffle.model.enums.RuleLogicCheckType;
import com.yu.market.server.raffle.service.rule.tree.ILogicTreeNode;
import com.yu.market.server.raffle.service.rule.tree.factory.DefaultTreeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author yu
 * @description 兜底奖励节点 - 当其他规则未通过时，提供默认奖励
 * @date 2025-01-16
 */
@Slf4j
@Component("rule_luck_award")
public class RuleLuckAwardLogicTreeNode implements ILogicTreeNode {

    /**
     * 执行兜底奖励节点逻辑
     *
     * @param userId    用户ID
     * @param strategyId 策略ID
     * @param awardId   奖励ID
     * @return 决策结果
     */
    @Override
    public DefaultTreeFactory.TreeActionBO logic(String userId, Long strategyId, Integer awardId) {
        log.info("执行兜底奖励节点逻辑 - 用户ID: {}, 策略ID: {}, 奖励ID: {}", userId, strategyId, awardId);

        // 兜底奖励逻辑，提供默认奖励
        DefaultTreeFactory.StrategyAwardData awardData = DefaultTreeFactory.StrategyAwardData.builder()
                .awardId(101) // 默认奖励ID
                .awardRuleValue("1,100") // 默认奖励规则
                .build();

        log.info("兜底奖励生成 - 奖励ID: {}, 奖励规则: {}", awardData.getAwardId(), awardData.getAwardRuleValue());

        return DefaultTreeFactory.TreeActionBO.builder()
                .ruleLogicCheckType(RuleLogicCheckType.TAKE_OVER)
                .strategyAwardData(awardData)
                .build();
    }
}
