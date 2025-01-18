package com.yu.market.server.raffle.service.rule.tree.impl;

import com.yu.market.common.contants.Constants;
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
    public DefaultTreeFactory.TreeActionBO logic(String userId, Long strategyId, Integer awardId, String ruleValue) {
        log.info("规则过滤-兜底奖品 userId:{} strategyId:{} awardId:{} ruleValue:{}", userId, strategyId, awardId, ruleValue);
        String[] split = ruleValue.split(Constants.COLON);
        if (split.length == 0) {
            log.error("规则过滤-兜底奖品，兜底奖品未配置告警 userId:{} strategyId:{} awardId:{}", userId, strategyId, awardId);
            throw new RuntimeException("兜底奖品未配置 " + ruleValue);
        }
        // 兜底奖励配置
        Integer luckAwardId = Integer.valueOf(split[0]);
        String awardRuleValue = split.length > 1 ? split[1] : "";

        // 返回兜底奖品
        log.info("规则过滤-兜底奖品 userId:{} strategyId:{} awardId:{} awardRuleValue:{}", userId, strategyId, luckAwardId, awardRuleValue);
        return DefaultTreeFactory.TreeActionBO.builder()
                .ruleLogicCheckType(RuleLogicCheckType.TAKE_OVER)
                .strategyAward(DefaultTreeFactory.StrategyAward.builder()
                        .awardId(luckAwardId)
                        .awardRuleValue(awardRuleValue)
                        .build())
                .build();
    }
}
