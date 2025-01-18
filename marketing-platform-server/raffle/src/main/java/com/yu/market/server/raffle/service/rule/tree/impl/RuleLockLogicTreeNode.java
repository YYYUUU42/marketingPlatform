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
     * @return 决策结果
     */
    @Override
    public DefaultTreeFactory.TreeActionBO logic(String userId, Long strategyId, Integer awardId, String ruleValue) {
        log.info("执行次数锁节点逻辑 - 用户ID: {}, 策略ID: {}, 奖励ID: {}", userId, strategyId, awardId);

        long raffleCount = 0L;
        try {
            raffleCount = Long.parseLong(ruleValue);
        } catch (Exception e) {
            throw new RuntimeException("规则过滤-次数锁异常 ruleValue: " + ruleValue + " 配置不正确");
        }


		// todo 用户抽奖次数
		long userRaffleCount = 10L;
		if (userRaffleCount >= raffleCount) {
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
