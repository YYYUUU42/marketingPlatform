package com.yu.market.server.coupon.service.distribution.basics;

import com.yu.market.common.exception.ServiceException;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author yu
 * @description 分发服务策略模式选择器
 * @date 2025-02-15
 */
@Component
public class DistributionStrategyChoose implements ApplicationContextAware, CommandLineRunner {

	/**
	 * 应用上下文，我们这里通过 Spring IOC 获取 Bean 实例
	 */
	private ApplicationContext applicationContext;

	/**
	 * 执行策略集合
	 */
	private final Map<String, DistributionExecuteStrategy> abstractExecuteStrategyMap = new HashMap<>();

	/**
	 * 根据 mark 查询具体策略
	 *
	 * @param mark 策略标识
	 * @return 实际执行策略
	 */
	public DistributionExecuteStrategy choose(String mark) {
		return Optional.ofNullable(abstractExecuteStrategyMap.get(mark)).orElseThrow(() -> new ServiceException(String.format("[%s] 策略未定义", mark)));
	}

	/**
	 * 根据 mark 查询具体策略并执行
	 *
	 * @param mark         策略标识
	 * @param requestParam 执行策略入参
	 * @param <REQUEST>    执行策略入参范型
	 */
	public <REQUEST> void chooseAndExecute(String mark, REQUEST requestParam) {
		DistributionExecuteStrategy executeStrategy = choose(mark);
		executeStrategy.execute(requestParam);
	}

	/**
	 * 根据 mark 查询具体策略并执行，带返回结果
	 *
	 * @param mark         策略标识
	 * @param requestParam 执行策略入参
	 * @param <REQUEST>    执行策略入参范型
	 * @param <RESPONSE>   执行策略出参范型
	 */
	public <REQUEST, RESPONSE> RESPONSE chooseAndExecuteResp(String mark, REQUEST requestParam) {
		DistributionExecuteStrategy executeStrategy = choose(mark);
		return (RESPONSE) executeStrategy.executeResp(requestParam);
	}

	@Override
	public void run(String... args) throws Exception {
		Map<String, DistributionExecuteStrategy> actual = applicationContext.getBeansOfType(DistributionExecuteStrategy.class);
		actual.forEach((beanName, bean) -> {
			DistributionExecuteStrategy beanExist = abstractExecuteStrategyMap.get(bean.mark());
			if (beanExist != null) {
				throw new ServiceException(String.format("[%s] Duplicate execution policy", bean.mark()));
			}
			abstractExecuteStrategyMap.put(bean.mark(), bean);
		});
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}