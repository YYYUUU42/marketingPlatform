package com.yu.market.server.raffle.controller;

import com.yu.market.server.raffle.service.armory.IStrategyArmory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/raffle")
@RequiredArgsConstructor
public class TestController {

	private final IStrategyArmory strategyArmory;

	@PostMapping("/test")
	public void test(){
		boolean b = strategyArmory.assembleLotteryStrategy(100001L);
		log.info("测试结果：{}", b);
	}
}
