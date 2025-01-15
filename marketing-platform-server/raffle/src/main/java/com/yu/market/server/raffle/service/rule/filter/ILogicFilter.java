package com.yu.market.server.raffle.service.rule.filter;


import com.yu.market.server.raffle.model.bo.RuleActionBO;
import com.yu.market.server.raffle.model.bo.RuleMatterBO;

/**
 * @author yu
 * @description 抽奖规则过滤接口
 * @date 2025-01-09
 */
public interface ILogicFilter<T extends RuleActionBO.RaffleBO> {

    RuleActionBO<T> filter(RuleMatterBO ruleMatterBO);

}
