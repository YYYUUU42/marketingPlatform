package com.yu.market.server.raffle.repository;

import com.yu.market.common.exception.ServiceException;
import com.yu.market.server.raffle.model.bo.*;
import com.yu.market.server.raffle.model.vo.RuleWeightVO;

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
    String queryStrategyRuleValue(Long strategyId, String ruleModel);

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

    /**
     * 查询规则模型
     */
    StrategyAwardRuleModelBO queryStrategyAwardRuleModelBO(Long strategyId, Integer awardId);

    /**
     * 根据规则树ID，查询树结构信息
     *
     * @param treeId 规则树ID
     * @return 树结构信息
     */
    RuleTreeBO queryRuleTreeBoByTreeId(String treeId);

    /**
     * 缓存奖品库存
     *
     * @param cacheKey   key
     * @param awardCount 库存值
     */
    void cacheStrategyAwardCount(String cacheKey, Integer awardCount);

    /**
     * 缓存key，decr 方式扣减库存
     *
     * @param cacheKey 缓存Key
     * @return 扣减结果
     */
    Boolean subtractionAwardStock(String cacheKey);

    /**
     * 写入奖品库存消费队列
     */
    void awardStockConsumeSendQueue(StrategyAwardStockKeyBO strategyAwardStockKeyVO);

    /**
     * 获取奖品库存消费队列
     */
    StrategyAwardStockKeyBO takeQueueValue() throws ServiceException;

    /**
     * 更新奖品库存消耗
     */
    void updateStrategyAwardStock(Long strategyId, Integer awardId);

    /**
     * 查询策略ID
     *
     * @param activityId 活动ID
     * @return 策略ID
     */
    Long queryStrategyIdByActivityId(Long activityId);

    /**
     * 根据策略ID+奖品ID的唯一值组合，查询奖品信息
     *
     * @param strategyId 策略ID
     * @param awardId    奖品ID
     * @return 奖品信息
     */
    StrategyAwardBO queryStrategyAwardBO(Long strategyId, Integer awardId);

    /**
     * 根据规则树ID集合查询奖品中加锁数量的配置「部分奖品需要抽奖N次解锁」
     *
     * @param treeIds 规则树ID值
     * @return key 规则树，value rule_lock 加锁值
     */
    Map<String, Integer> queryAwardRuleLockCount(String[] treeIds);

    /**
     * 查询奖品权重配置
     *
     * @param strategyId 策略ID
     * @return 权重规则
     */
    List<RuleWeightVO> queryAwardRuleWeight(Long strategyId);

    /**
     * 查询有效活动的奖品配置
     *
     * @return 奖品配置列表
     */
    List<StrategyAwardStockKeyBO> queryOpenActivityStrategyAwardList();
}
