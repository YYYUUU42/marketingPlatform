package com.yu.market.server.raffle.model.bo;

import com.yu.market.server.raffle.model.enums.RuleLimitType;
import com.yu.market.server.raffle.model.enums.RuleLogicCheckType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yu
 * @description 规则树节点指向线对象。用于衔接 from->to 节点链路关系
 * @date 2025-01-15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleTreeNodeLineBO {

	/**
	 * 规则树ID
	 */
	private String treeId;

	/**
	 * 规则Key节点 From
	 */
	private String ruleNodeFrom;

	/**
	 * 规则Key节点 To
	 */
	private String ruleNodeTo;

	/**
	 * 限定类型
	 */
	private RuleLimitType ruleLimitType;

	/**
	 * 限定值（到下个节点）
	 */
	private RuleLogicCheckType ruleLimitValue;

}
