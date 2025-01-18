package com.yu.market.server.raffle.repository;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yu.market.common.contants.Constants;
import com.yu.market.common.contants.RedisKey;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.redis.IRedisService;
import com.yu.market.server.raffle.mapper.*;
import com.yu.market.server.raffle.model.bo.*;
import com.yu.market.server.raffle.model.enums.RuleLimitType;
import com.yu.market.server.raffle.model.enums.RuleLogicCheckType;
import com.yu.market.server.raffle.model.pojo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yu.market.common.exception.errorCode.BaseErrorCode.UN_ASSEMBLED_STRATEGY_ARMORY;

/**
 * @author yu
 * @description 策略服务仓储实现类
 * @date 2025-01-07
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class StrategyRepository implements IStrategyRepository {

	private final StrategyMapper strategyMapper;
	private final StrategyAwardMapper strategyAwardMapper;
	private final StrategyRuleMapper strategyRuleMapper;
	private final RaffleActivityMapper raffleActivityMapper;
	private final RaffleActivityAccountMapper raffleActivityAccountMapper;
	private final IRedisService redisService;
	private final RuleTreeMapper ruleTreeMapper;
	private final RuleTreeNodeLineMapper ruleTreeNodeLineMapper;
	private final RuleTreeNodeMapper ruleTreeNodeMapper;

	/**
	 * 查询策略奖项列表（优先缓存，次选数据库）
	 */
	@Override
	public List<StrategyAwardBO> queryStrategyAwardList(Long strategyId) {
		// 优先缓存
		String cacheKey = RedisKey.STRATEGY_AWARD_KEY + strategyId;
		List<StrategyAwardBO> strategyAwardBoList = redisService.getValue(cacheKey);
		if (!CollectionUtil.isEmpty(strategyAwardBoList)) {
			return strategyAwardBoList;
		}

		// 从数据库中取出数据
		List<StrategyAward> strategyAwardList = strategyAwardMapper.selectList(new LambdaQueryWrapper<StrategyAward>()
				.eq(StrategyAward::getStrategyId, strategyId));
		if (CollectionUtil.isEmpty(strategyAwardList)) {
			return List.of();
		}

		// 将数据库对象转换为业务实体
		strategyAwardBoList = strategyAwardList.stream()
				.map(strategyAward -> StrategyAwardBO.builder()
						.strategyId(strategyAward.getStrategyId())
						.awardId(strategyAward.getAwardId())
						.awardCount(strategyAward.getAwardCount())
						.awardCountSurplus(strategyAward.getAwardCountSurplus())
						.awardRate(strategyAward.getAwardRate())
						.build())
				.toList();

		redisService.setValue(cacheKey, strategyAwardBoList);

		return strategyAwardBoList;
	}

	/**
	 * 将与某个策略（strategyId）相关的概率查找表和抽奖范围值存储到缓存中，供后续抽奖逻辑使用
	 */
	@Override
	public void storeStrategyAwardSearchRateTable(String cacheKey, Integer rateRange, Map<Integer, Integer> strategyAwardSearchRateTable) {
		// 存储抽奖策略范围值
		redisService.setValue(RedisKey.STRATEGY_RATE_RANGE_KEY + cacheKey, rateRange);

		// 存储概率查找表
		Map<Integer, Integer> cacheRateTable = redisService.getMap(RedisKey.STRATEGY_RATE_TABLE_KEY + cacheKey);
		cacheRateTable.putAll(strategyAwardSearchRateTable);
	}

	/**
	 * 根据策略 ID 和随机数（rateKey），从缓存中获取对应的奖项 ID
	 */
	@Override
	public Integer getStrategyAwardAssemble(String cacheKey, Integer rateKey) {
		return redisService.getFromMap(RedisKey.STRATEGY_RATE_TABLE_KEY + cacheKey, rateKey);
	}

	/**
	 * 获取某个策略（strategyId）的抽奖范围值（rateRange）
	 */
	@Override
	public int getRateRange(String cacheKey) {
		cacheKey = RedisKey.STRATEGY_RATE_RANGE_KEY + cacheKey;
		if (!redisService.isExists(cacheKey)) {
			throw new ServiceException(UN_ASSEMBLED_STRATEGY_ARMORY);
		}
		return redisService.getValue(cacheKey);
	}

	/**
	 * 查询抽奖策略
	 */
	@Override
	public StrategyBO queryStrategyBOByStrategyId(Long strategyId) {
		// 优先缓存
		String cacheKey = RedisKey.STRATEGY_KEY + strategyId;
		StrategyBO strategyBO = redisService.getValue(cacheKey);
		if (strategyBO != null) {
			return strategyBO;
		}

		// 查询数据库
		Strategy strategy = strategyMapper.selectOne(new LambdaQueryWrapper<Strategy>()
				.eq(Strategy::getStrategyId, strategyId));
		strategyBO = StrategyBO.builder()
				.strategyId(strategy.getStrategyId())
				.strategyDesc(strategy.getStrategyDesc())
				.ruleModels(strategy.getRuleModels())
				.build();

		redisService.setValue(cacheKey, strategyBO);

		return strategyBO;
	}

	/**
	 * 根据策略id和规则模型得到抽奖策略规则
	 */
	@Override
	public StrategyRuleBO queryStrategyRule(Long strategyId, String ruleModel) {
		StrategyRule strategyRule = strategyRuleMapper.selectOne(new LambdaQueryWrapper<StrategyRule>()
				.eq(StrategyRule::getStrategyId, strategyId)
				.eq(StrategyRule::getRuleModel, ruleModel));

		return StrategyRuleBO.builder()
				.strategyId(strategyRule.getStrategyId())
				.awardId(strategyRule.getAwardId())
				.ruleType(strategyRule.getRuleType())
				.ruleModel(strategyRule.getRuleModel())
				.ruleValue(strategyRule.getRuleValue())
				.ruleDesc(strategyRule.getRuleDesc())
				.build();
	}

	@Override
	public String queryStrategyRuleValue(Long strategyId, String ruleModel) {
		return queryStrategyRuleValue(strategyId, null, ruleModel);
	}

	/**
	 * 查询抽检规则
	 */
	@Override
	public String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel) {
		StrategyRule strategyRule = strategyRuleMapper.selectOne(new LambdaQueryWrapper<StrategyRule>()
				.eq(StrategyRule::getStrategyId, strategyId)
				.eq(awardId != null, StrategyRule::getAwardId, awardId)
				.eq(StrategyRule::getRuleModel, ruleModel));

		return strategyRule != null ? strategyRule.getRuleValue() : null;
	}

	/**
	 * 根据用户ID、策略ID，查询用户活动账户总使用量
	 *
	 * @param userId     用户ID
	 * @param strategyId 策略ID
	 * @return 使用总量
	 */
	@Override
	public Integer queryActivityAccountTotalUseCount(String userId, Long strategyId) {
		RaffleActivity raffleActivity = raffleActivityMapper.selectOne(new LambdaQueryWrapper<RaffleActivity>()
				.eq(RaffleActivity::getStrategyId, strategyId));
		if (raffleActivity == null) {
			throw new ServiceException("未查询到该策略Id 活动: " + strategyId);
		}

		Long activityId = raffleActivity.getActivityId();
		RaffleActivityAccount raffleActivityAccount = raffleActivityAccountMapper.selectOne(new LambdaQueryWrapper<RaffleActivityAccount>()
				.eq(RaffleActivityAccount::getUserId, userId)
				.eq(RaffleActivityAccount::getActivityId, activityId));
		if (raffleActivityAccount == null) {
			throw new ServiceException("未查询到该用户在该活动的行为 userId: " + userId + "activityId: " + activityId);
		}

		// 返回计算使用量
		return raffleActivityAccount.getTotalCount() - raffleActivityAccount.getTotalCountSurplus();
	}

	/**
	 * 查询规则模型
	 */
	@Override
	public StrategyAwardRuleModelBO queryStrategyAwardRuleModelBO(Long strategyId, Integer awardId) {
		StrategyAward strategyAward = strategyAwardMapper.selectOne(new LambdaQueryWrapper<StrategyAward>()
				.eq(StrategyAward::getStrategyId, strategyId)
				.eq(StrategyAward::getAwardId, awardId));

		String ruleModels = "";
		if (strategyAward != null) {
			ruleModels = strategyAward.getRuleModels();
		}

		return StrategyAwardRuleModelBO.builder()
				.ruleModels(ruleModels)
				.build();
	}

	/**
	 * 根据规则树ID，查询树结构信息
	 *
	 * @param treeId 规则树ID
	 * @return 树结构信息
	 */
	@Override
	public RuleTreeBO queryRuleTreeBoByTreeId(String treeId) {
		// 优先缓存
		String cacheKey = RedisKey.RULE_TREE_BO_KEY + treeId;
		RuleTreeBO ruleTreeBoCache = redisService.getValue(cacheKey);
		if (ruleTreeBoCache != null) {
			return ruleTreeBoCache;
		}

		// 查数据库
		RuleTree ruleTree = ruleTreeMapper.selectOne(new LambdaQueryWrapper<RuleTree>().eq(RuleTree::getTreeId, treeId));
		List<RuleTreeNode> ruleTreeNodes = ruleTreeNodeMapper.selectList(new LambdaQueryWrapper<RuleTreeNode>().eq(RuleTreeNode::getTreeId, treeId));
		List<RuleTreeNodeLine> ruleTreeNodeLines = ruleTreeNodeLineMapper.selectList(new LambdaQueryWrapper<RuleTreeNodeLine>().eq(RuleTreeNodeLine::getTreeId, treeId));

		// tree node line 转换 Map 结构
		Map<String, List<RuleTreeNodeLineBO>> ruleTreeNodeLineMap = ruleTreeNodeLines.stream()
				.map(line -> RuleTreeNodeLineBO.builder()
						.treeId(line.getTreeId())
						.ruleNodeFrom(line.getRuleNodeFrom())
						.ruleNodeTo(line.getRuleNodeTo())
						.ruleLimitType(RuleLimitType.valueOf(line.getRuleLimitType()))
						.ruleLimitValue(RuleLogicCheckType.valueOf(line.getRuleLimitValue()))
						.build())
				.collect(Collectors.groupingBy(RuleTreeNodeLineBO::getRuleNodeFrom));

		// tree node 转换为 Map 结构
		Map<String, RuleTreeNodeBO> treeNodeMap = ruleTreeNodes.stream()
				.map(node -> RuleTreeNodeBO.builder()
						.treeId(node.getTreeId())
						.ruleKey(node.getRuleKey())
						.ruleDesc(node.getRuleDesc())
						.ruleValue(node.getRuleValue())
						.treeNodeLineBOList(ruleTreeNodeLineMap.get(node.getRuleKey()))
						.build())
				.collect(Collectors.toMap(RuleTreeNodeBO::getRuleKey, nodeBO -> nodeBO));

		// 构建规则树对象
		RuleTreeBO ruleTreeBO = RuleTreeBO.builder()
				.treeId(ruleTree.getTreeId())
				.treeName(ruleTree.getTreeName())
				.treeDesc(ruleTree.getTreeDesc())
				.treeRootRuleNode(ruleTree.getTreeNodeRuleKey())
				.treeNodeMap(treeNodeMap)
				.build();

		redisService.setValue(cacheKey, ruleTreeBO);

		return ruleTreeBO;
	}

	/**
	 * 缓存奖品库存
	 *
	 * @param cacheKey   key
	 * @param awardCount 库存值
	 */
	@Override
	public void cacheStrategyAwardCount(String cacheKey, Integer awardCount) {
		if (!redisService.isExists(cacheKey)) {
			redisService.setAtomicLong(cacheKey, awardCount);
		}
	}

	/**
	 * 缓存 key，decr 方式扣减库存
	 *
	 * @param cacheKey 缓存Key
	 * @return 扣减结果
	 */
	@Override
	public Boolean subtractionAwardStock(String cacheKey) {
		// 扣减库存
		long stock = redisService.decr(cacheKey);

		// 如果库存小于0，恢复库存为0并返回 false
		if (stock < 0) {
			redisService.setValue(cacheKey, 0);
			log.warn("库存小于0，已恢复库存为0，缓存Key: {}", cacheKey);
			return false;
		}

		// 生成库存锁的 key
		String lockKey = cacheKey + Constants.UNDERLINE + stock;

		// 尝试加锁，如果加锁失败，返回 false
		Boolean lock = redisService.setNx(lockKey);
		if (!lock) {
			log.info("策略奖品库存加锁失败，lockKey: {}", lockKey);
			return false;
		}

		log.info("成功加锁，lockKey: {}", lockKey);
		return true;
	}

	/**
	 * 写入奖品库存消费队列
	 */
	@Override
	public void awardStockConsumeSendQueue(StrategyAwardStockKeyBO strategyAwardStockKeyVO) {
		// 获取 Redis 阻塞队列
		String cacheKey = RedisKey.STRATEGY_AWARD_COUNT_QUERY_KEY;
		RBlockingQueue<StrategyAwardStockKeyBO> blockingQueue = redisService.getBlockingQueue(cacheKey);
		if (blockingQueue == null) {
			log.error("获取 Redis 阻塞队列失败，cacheKey: {}", cacheKey);
			return;
		}

		// 获取 Redis 延时队列，并设置延迟时间为3秒
		RDelayedQueue<StrategyAwardStockKeyBO> delayedQueue = redisService.getDelayedQueue(blockingQueue);
		delayedQueue.offer(strategyAwardStockKeyVO, 3, TimeUnit.SECONDS);
	}

	/**
	 * 获取奖品库存消费队列
	 */
	@Override
	public StrategyAwardStockKeyBO takeQueueValue() {

		// 获取 Redis 阻塞队列
		String cacheKey = RedisKey.STRATEGY_AWARD_COUNT_QUERY_KEY;
		RBlockingQueue<StrategyAwardStockKeyBO> destinationQueue = redisService.getBlockingQueue(cacheKey);
		if (destinationQueue == null) {
			log.error("获取 Redis 阻塞队列失败，cacheKey: {}", cacheKey);
			return null;
		}

		// 从队列中取出数据
		StrategyAwardStockKeyBO strategyAwardStockKeyVO = destinationQueue.poll();
		if (strategyAwardStockKeyVO == null) {
			log.warn("队列为空，无法获取奖品库存数据，cacheKey: {}", cacheKey);
		} else {
			log.info("成功从队列中取出奖品库存数据，cacheKey: {}", cacheKey);
		}

		return strategyAwardStockKeyVO;
	}

	/**
	 * 更新奖品库存消耗
	 */
	@Override
	public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
		StrategyAward strategyAward = new StrategyAward();
		strategyAward.setStrategyId(strategyId);
		strategyAward.setAwardId(awardId);

		strategyAwardMapper.updateStrategyAwardStock(strategyAward);
	}
}
