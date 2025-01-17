package com.yu.market.server.raffle.service.rule.tree.factory.engine.impl;

import com.yu.market.common.exception.ServiceException;
import com.yu.market.server.raffle.model.bo.RuleTreeBO;
import com.yu.market.server.raffle.model.bo.RuleTreeNodeBO;
import com.yu.market.server.raffle.model.bo.RuleTreeNodeLineBO;
import com.yu.market.server.raffle.model.enums.RuleLogicCheckType;
import com.yu.market.server.raffle.service.rule.tree.ILogicTreeNode;
import com.yu.market.server.raffle.service.rule.tree.factory.DefaultTreeFactory;
import com.yu.market.server.raffle.service.rule.tree.factory.engine.IDecisionTreeEngine;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @author yu
 * @description 决策树引擎
 * @date 2025-01-15
 */
@Slf4j
public class DecisionTreeEngine implements IDecisionTreeEngine {

	/**
	 * 存储决策树的逻辑节点实现
	 * key: 节点标识
	 * value: 节点实现
	 */
	private final Map<String, ILogicTreeNode> logicTreeNodeGroup;

	/**
	 * 决策树的配置和数据
	 */
	private final RuleTreeBO ruleTreeBO;

	/**
	 * 构造函数
	 *
	 * @param logicTreeNodeGroup 决策树节点实现集合
	 * @param ruleTreeBO         决策树配置对象
	 */
	public DecisionTreeEngine(Map<String, ILogicTreeNode> logicTreeNodeGroup, RuleTreeBO ruleTreeBO) {
		this.logicTreeNodeGroup = logicTreeNodeGroup;
		this.ruleTreeBO = ruleTreeBO;
	}

	/**
	 * 执行决策树逻辑，遍历规则节点并返回最终的奖励结果
	 */
	@Override
	public DefaultTreeFactory.StrategyAward process(String userId, Long strategyId, Integer awardId) {
		DefaultTreeFactory.StrategyAward strategyAwardData = null;

		// 获取决策树的初始节点（根节点）
		String currentNodeKey = ruleTreeBO.getTreeRootRuleNode();
		Map<String, RuleTreeNodeBO> treeNodeMap = ruleTreeBO.getTreeNodeMap();

		// 校验根节点是否存在
		if (currentNodeKey == null || !treeNodeMap.containsKey(currentNodeKey)) {
			throw new ServiceException("决策树引擎初始化失败：根节点不存在！");
		}

		// 获取起始节点「根节点记录了第一个要执行的规则」
		RuleTreeNodeBO currentNode = treeNodeMap.get(currentNodeKey);

		while (currentNode != null) {
			// 获取当前节点对应的逻辑实现
			ILogicTreeNode logicTreeNode = logicTreeNodeGroup.get(currentNode.getRuleKey());
			if (logicTreeNode == null) {
				throw new IllegalStateException(String.format("决策树引擎错误：未找到节点逻辑实现，节点Key=%s", currentNode.getRuleKey()));
			}

			// 执行逻辑节点的决策计算
			DefaultTreeFactory.TreeActionBO logicBO = logicTreeNode.logic(userId, strategyId, awardId);
			RuleLogicCheckType ruleLogicCheckTypeBO = logicBO.getRuleLogicCheckType();
			strategyAwardData = logicBO.getStrategyAwardData();

			log.info("决策树引擎【{}】treeId:{} node:{} code:{}", ruleTreeBO.getTreeName(), ruleTreeBO.getTreeId(), currentNode, ruleLogicCheckTypeBO.getCode());

			// 根据决策结果获取下一个节点
			currentNodeKey = getNextNodeKey(ruleLogicCheckTypeBO.getCode(), currentNode.getTreeNodeLineBOList());
			currentNode = currentNodeKey != null ? treeNodeMap.get(currentNodeKey) : null;
		}

		// 返回最终结果
		return strategyAwardData;
	}

	/**
	 * 根据决策结果获取下一个节点的Key
	 *
	 * @param decisionValue 当前节点的决策值
	 * @param nodeLineList 当前节点的连线信息
	 * @return 下一个节点的Key，如果没有匹配的节点则返回null
	 */
	private String getNextNodeKey(String decisionValue, List<RuleTreeNodeLineBO> nodeLineList) {
		if (nodeLineList == null || nodeLineList.isEmpty()) {
			return null;
		}

		for (RuleTreeNodeLineBO nodeLine : nodeLineList) {
			if (isDecisionMatch(decisionValue, nodeLine)) {
				return nodeLine.getRuleNodeTo();
			}
		}

		throw new ServiceException(String.format("决策树引擎错误：未找到匹配的下一个节点，决策值=%s", decisionValue));
	}

	public boolean isDecisionMatch(String matterValue, RuleTreeNodeLineBO nodeLine) {
		return switch (nodeLine.getRuleLimitType()) {
			case EQUAL -> matterValue.equals(nodeLine.getRuleLimitValue().getCode());
			// 其他规则暂时不需要实现
			default -> false;
		};
	}

}
