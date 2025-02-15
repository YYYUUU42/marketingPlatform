package com.yu.market.server.coupon.service.admin.chain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author yu
 * @description 商家后管责任链上下文容器
 * @date 2025-02-14
 */
@Slf4j
@Component
public class MerchantAdminChainContext<T> implements ApplicationContextAware, CommandLineRunner {

	/**
	 * 应用上下文，我们这里通过 Spring IOC 获取 Bean 实例
	 */
	private ApplicationContext applicationContext;

	/**
	 * 保存商家后管责任链实现类
	 */
	private final Map<String, List<MerchantAdminAbstractChainHandler<T>>> chainHandlerContainer = new HashMap<>();


	@Override
	public void run(String... args) throws Exception {
		// 从 Spring 容器中获取所有 MerchantAdminAbstractChainHandler 实例
		Map<String, MerchantAdminAbstractChainHandler> chainHandlerMap = applicationContext.getBeansOfType(MerchantAdminAbstractChainHandler.class);

		chainHandlerMap.forEach((beanName, bean) -> {
			MerchantAdminAbstractChainHandler<T> handler = (MerchantAdminAbstractChainHandler<T>) bean;
			// 根据 Mark 将责任链处理器添加到对应的集合中
			chainHandlerContainer.computeIfAbsent(handler.mark(), k -> new ArrayList<>()).add(handler);
		});

		// 对每个 Mark 对应的责任链实现类集合进行排序，优先级小的在前
		chainHandlerContainer.forEach((mark, handlers) -> handlers.sort(Comparator.comparing(Ordered::getOrder)));
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * 责任链组件执行
	 *
	 * @param mark         责任链组件标识
	 * @param requestParam 请求参数
	 */
	public void handler(String mark, T requestParam) {
		// 获取并执行对应标识的责任链处理器集合
		List<MerchantAdminAbstractChainHandler<T>> chainHandlers = chainHandlerContainer.get(mark);
		if (CollectionUtils.isEmpty(chainHandlers)) {
			throw new RuntimeException(String.format("未定义标识为 [%s] 的责任链.", mark));
		}
		chainHandlers.forEach(handler -> handler.handle(requestParam));
	}
}
