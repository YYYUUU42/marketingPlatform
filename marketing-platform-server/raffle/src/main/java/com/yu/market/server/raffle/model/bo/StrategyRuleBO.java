package com.yu.market.server.raffle.model.bo;

import cn.hutool.core.util.StrUtil;
import com.yu.market.common.contants.Constants;
import com.yu.market.common.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyRuleBO {
	/**
	 * 抽奖策略ID
	 */
	private Long strategyId;

	/**
	 * 抽奖奖品ID【规则类型为策略，则不需要奖品ID】
	 */
	private Integer awardId;

	/**
	 * 抽象规则类型；1-策略规则、2-奖品规则
	 */
	private Integer ruleType;

	/**
	 * 抽奖规则类型【rule_random - 随机值计算、rule_lock - 抽奖几次后解锁、rule_luck_award - 幸运奖(兜底奖品)】
	 */
	private String ruleModel;

	/**
	 * 抽奖规则比值
	 */
	private String ruleValue;

	/**
	 * 抽奖规则描述
	 */
	private String ruleDesc;

	/**
	 * 获取权重值
	 * 数据案例：4000:102,103,104,105 5000:102,103,104,105,106,107 6000:102,103,104,105,106,107,108,109
	 * @return Map： key 为多少积分	value 为该积分可以得到的奖品
	 */
	public Map<String, List<Integer>> getRuleWeightValues() {
		// 如果 ruleModel 不是 "rule_weight"，直接返回空 Map
		if (!"rule_weight".equals(ruleModel)) {
			return Collections.emptyMap();
		}

		if (StrUtil.isBlank(ruleValue)) {
			return Collections.emptyMap();
		}

		Map<String, List<Integer>> resultMap = new HashMap<>();
		// 按空格拆分 ruleValue 为多个组
		String[] ruleValueGroups = ruleValue.split(Constants.SPACE);
		for (String ruleValueGroup : ruleValueGroups) {
			if (StrUtil.isBlank(ruleValueGroup)) {
				continue;
			}

			// 按冒号分割键值对
			String[] parts = ruleValueGroup.split(Constants.COLON);
			if (parts.length != 2) {
				throw new ServiceException("rule_weight 无效" + ruleValueGroup);
			}

			String key = parts[0];
			String[] valueStrings = parts[1].split(Constants.SPLIT);

			List<Integer> values = Arrays.stream(valueStrings)
					.map(Integer::parseInt)
					.toList();

			resultMap.put(key, values);
		}

		return resultMap;
	}
}
