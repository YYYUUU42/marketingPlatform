package com.yu.market.server.raffle.repository;

import com.yu.market.server.raffle.model.bo.StrategyAwardBO;
import com.yu.market.server.raffle.model.bo.StrategyBO;
import com.yu.market.server.raffle.model.bo.StrategyRuleBO;

import java.util.List;
import java.util.Map;

/**
 * @author yu
 * @description 策略服务仓储接口
 * @date 2025-01-07
 */
public interface IStrategyRepository {

    /**
     * 查询策略奖项列表（优先缓存，次选数据库）
     */
    List<StrategyAwardBO> queryStrategyAwardList(Long strategyId);

    /**
     * 将与某个策略（strategyId）相关的概率查找表和抽奖范围值存储到缓存中，供后续抽奖逻辑使用
     */
    void storeStrategyAwardSearchRateTable(String cacheKey, Integer rateRange, Map<Integer, Integer> strategyAwardSearchRateTable);

    /**
     * 根据策略 ID 和随机数（rateKey），从缓存中获取对应的奖项 ID
     */
    Integer getStrategyAwardAssemble(String cacheKey, Integer rateKey);

    /**
     * 获取某个策略（strategyId）的抽奖范围值（rateRange）
     */
    int getRateRange(String cacheKey);

    /**
     * 查询抽奖策略
     */
    StrategyBO queryStrategyBOByStrategyId(Long strategyId);

    /**
     * 根据策略id和规则模型得到抽奖策略规则
     */
    StrategyRuleBO queryStrategyRule(Long strategyId, String ruleModel);

    /**
     * 查询抽检规则
     */
    String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel);

    /**
     * 根据用户ID、策略ID，查询用户活动账户总使用量
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @return 使用总量
     */
    Integer queryActivityAccountTotalUseCount(String userId, Long strategyId);
}
