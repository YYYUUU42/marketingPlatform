package com.yu.market.server.raffle.model.bo;

import cn.hutool.core.util.StrUtil;
import com.yu.market.common.contants.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyBO {
	/**
	 * 抽奖策略ID
	 */
	private Long strategyId;
	/**
	 * 抽奖策略描述
	 */
	private String strategyDesc;
	/**
	 * 抽奖规则模型 rule_weight,rule_blacklist
	 */
	private String ruleModels;

	/**
	 * 该抽奖策略下的规则模型
	 */
	public String[] ruleModels() {
		return StrUtil.isBlank(ruleModels) ? null : ruleModels.split(Constants.SPLIT);
	}

	/**
	 * 判断规则模型中是否有 "rule_weight"
	 */
	public String getRuleWeight() {
		return Arrays.asList(ruleModels()).contains("rule_weight") ? "rule_weight" : null;
	}
}
