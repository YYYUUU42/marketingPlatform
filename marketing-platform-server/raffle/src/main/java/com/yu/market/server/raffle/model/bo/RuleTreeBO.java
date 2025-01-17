package com.yu.market.server.raffle.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author yu
 * @description 规则树对象【注意；不具有唯一ID，不需要改变数据库结果的对象，可以被定义为值对象】
 * @date 2025-01-15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleTreeBO {

	/**
	 * 规则树ID
	 */
	private String treeId;

	/**
	 * 规则树名称
	 */
	private String treeName;

	/**
	 * 规则树描述
	 */
	private String treeDesc;

	/**
	 * 规则根节点
	 */
	private String treeRootRuleNode;

	/**
	 * 规则节点
	 */
	private Map<String, RuleTreeNodeBO> treeNodeMap;

}
