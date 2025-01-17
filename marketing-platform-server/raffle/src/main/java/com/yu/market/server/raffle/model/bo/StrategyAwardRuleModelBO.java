package com.yu.market.server.raffle.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


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

}
