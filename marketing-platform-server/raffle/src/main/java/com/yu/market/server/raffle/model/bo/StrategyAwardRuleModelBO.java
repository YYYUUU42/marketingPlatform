package com.yu.market.server.raffle.model.bo;

import com.yu.market.common.contants.Constants;
import com.yu.market.server.raffle.service.rule.filter.factory.DefaultLogicFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yu
 * @description 抽奖策略规则规则值对象；值对象，没有唯一ID，仅限于从数据库查询对象
 * @date 2025-01-10
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyAwardRuleModelBO {
	private String ruleModels;

	/**
	 * 获取抽奖中规则
	 */
	public String[] raffleCenterRuleModelList() {
		List<String> ruleModelList = new ArrayList<>();
		String[] ruleModelValues = ruleModels.split(Constants.SPLIT);
		for (String ruleModelValue : ruleModelValues) {
			if (DefaultLogicFactory.LogicModel.isCenter(ruleModelValue)) {
				ruleModelList.add(ruleModelValue);
			}
		}
		return ruleModelList.toArray(new String[0]);
	}

	/**
	 * 获取抽奖后规则
	 */
	public String[] raffleAfterRuleModelList() {
		List<String> ruleModelList = new ArrayList<>();
		String[] ruleModelValues = ruleModels.split(Constants.SPLIT);
		for (String ruleModelValue : ruleModelValues) {
			if (DefaultLogicFactory.LogicModel.isAfter(ruleModelValue)) {
				ruleModelList.add(ruleModelValue);
			}
		}
		return ruleModelList.toArray(new String[0]);
	}
}
