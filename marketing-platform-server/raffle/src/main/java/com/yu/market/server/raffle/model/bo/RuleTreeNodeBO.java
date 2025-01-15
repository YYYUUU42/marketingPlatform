package com.yu.market.server.raffle.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yu
 * @description 规则树节点对象
 * @date 2025-01-15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleTreeNodeBO {

	/**
	 * 规则树ID
	 */
	private Integer treeId;

	/**
	 * 规则Key
	 */
	private String ruleKey;

	/**
	 * 规则描述
	 */
	private String ruleDesc;

	/**
	 * 规则比值
	 */
	private String ruleValue;


	/**
	 * 规则连线
	 */
	private List<RuleTreeNodeLineBO> treeNodeLineBOList;

}
