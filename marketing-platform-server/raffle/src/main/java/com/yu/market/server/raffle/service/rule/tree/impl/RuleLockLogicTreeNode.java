package com.yu.market.server.raffle.service.rule.tree.impl;

import com.yu.market.server.raffle.model.enums.RuleLogicCheckType;
import com.yu.market.server.raffle.service.rule.tree.ILogicTreeNode;
import com.yu.market.server.raffle.service.rule.tree.factory.DefaultTreeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author yu
 * @description 次数锁节点 - 检查用户是否满足次数限制规则
 * @date 2025-01-16
 */
@Slf4j
@Component("rule_lock")
public class RuleLockLogicTreeNode implements ILogicTreeNode {

    /**
     * 执行次数锁节点逻辑
     *
     * @param userId    用户ID
     * @param strategyId 策略ID
     * @param awardId   奖励ID
     * @return 决策结果
     */
    @Override
    public DefaultTreeFactory.TreeActionBO logic(String userId, Long strategyId, Integer awardId) {
        log.info("执行次数锁节点逻辑 - 用户ID: {}, 策略ID: {}, 奖励ID: {}", userId, strategyId, awardId);

        // TODO: 次数限制逻辑
        boolean isAllowed = true;

        if (isAllowed) {
            log.info("用户通过次数限制规则 - 用户ID: {}, 策略ID: {}", userId, strategyId);
            return DefaultTreeFactory.TreeActionBO.builder()
                    .ruleLogicCheckType(RuleLogicCheckType.ALLOW)
                    .build();
        } else {
            log.info("用户未通过次数限制规则 - 用户ID: {}, 策略ID: {}", userId, strategyId);
            return DefaultTreeFactory.TreeActionBO.builder()
                    .ruleLogicCheckType(RuleLogicCheckType.TAKE_OVER)
                    .build();
        }
    }
}
