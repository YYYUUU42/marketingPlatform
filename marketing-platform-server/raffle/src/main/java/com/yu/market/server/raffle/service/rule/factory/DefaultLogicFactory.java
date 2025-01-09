package com.yu.market.server.raffle.service.rule.factory;

import com.yu.market.server.raffle.model.annotation.LogicStrategy;
import com.yu.market.server.raffle.model.bo.RuleActionBO;
import com.yu.market.server.raffle.service.rule.ILogicFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yu
 * @description 规则工厂
 * @date 2025-01-09
 */
@Service
public class DefaultLogicFactory {

	/**
	 * 存储逻辑过滤器的映射表	key 规则模式 value 为对应的过滤器实例
	 */
	public Map<String, ILogicFilter<?>> logicFilterMap = new ConcurrentHashMap<>();

	/**
	 * 构造函数，通过注解扫描初始化逻辑过滤器映射表
	 */
	public DefaultLogicFactory(List<ILogicFilter<?>> logicFilters) {
		logicFilters.forEach(logic -> {
			LogicStrategy strategy = AnnotationUtils.findAnnotation(logic.getClass(), LogicStrategy.class);
			if (null != strategy) {
				logicFilterMap.put(strategy.logicMode().getCode(), logic);
			}
		});
	}

	/**
	 * 获取逻辑过滤器的映射表
	 */
	@SuppressWarnings("unchecked")
	public <T extends RuleActionBO.RaffleBO> Map<String, ILogicFilter<T>> getLogicFilters() {
		return (Map<String, ILogicFilter<T>>) (Map<?, ?>) logicFilterMap;
	}

	/**
	 * 规则模式枚举类，定义了系统支持的规则模式及其描述信息。
	 */
	@Getter
	@AllArgsConstructor
	public enum LogicModel {

		RULE_WIGHT("rule_weight","[抽奖前规则]根据抽奖权重返回可抽奖范围KEY"),
		RULE_BLACKLIST("rule_blacklist","[抽奖前规则] 黑名单规则过滤，命中黑名单则直接返回"),

		;

		private final String code;
		private final String info;

	}
}
