package com.yu.market.server.raffle.service.rule.tree.factory;

import com.yu.market.common.exception.ServiceException;
import com.yu.market.server.raffle.model.bo.RuleTreeBO;
import com.yu.market.server.raffle.model.enums.RuleLogicCheckType;
import com.yu.market.server.raffle.service.rule.tree.ILogicTreeNode;
import com.yu.market.server.raffle.service.rule.tree.factory.engine.IDecisionTreeEngine;
import com.yu.market.server.raffle.service.rule.tree.factory.engine.impl.DecisionTreeEngine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DefaultTreeFactory {

	/**
	 * 存储逻辑节点的	key: 节点标识，value: 节点实现
	 */
	private final Map<String, ILogicTreeNode> logicTreeNodeGroup;

	/**
	 * 构造函数，初始化逻辑节点组
	 *
	 * @param logicTreeNodeGroup 逻辑节点映射表
	 */
	public DefaultTreeFactory(Map<String, ILogicTreeNode> logicTreeNodeGroup) {
		this.logicTreeNodeGroup = logicTreeNodeGroup;
	}

	/**
	 * 创建并返回一个决策树引擎实例
	 *
	 * @param ruleTreeVO 规则树的业务对象
	 * @return 决策树引擎实例
	 */
	public IDecisionTreeEngine createDecisionTreeEngine(RuleTreeBO ruleTreeVO) {
		if (ruleTreeVO == null) {
			throw new ServiceException("规则树对象不能为空");
		}
		return new DecisionTreeEngine(logicTreeNodeGroup, ruleTreeVO);
	}

	/**
	 * 封装决策树节点的执行结果
	 */
	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class TreeActionBO {
		private RuleLogicCheckType ruleLogicCheckType;
		private StrategyAwardData strategyAwardData;
	}

	/**
	 * 封装与奖励相关的数据
	 */
	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class StrategyAwardData {
		/**
		 * 抽奖奖品ID - 内部流转使用
		 */
		private Integer awardId;

		/**
		 * 抽奖奖品规则
		 */
		private String awardRuleValue;
	}
}
