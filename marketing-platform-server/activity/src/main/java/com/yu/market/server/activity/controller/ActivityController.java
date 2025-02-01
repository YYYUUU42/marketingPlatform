package com.yu.market.server.activity.controller;

import com.yu.market.common.result.ResponseResult;
import com.yu.market.server.activity.service.armory.IActivityArmory;
import com.yu.market.server.raffle.service.armory.IStrategyArmory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/activity")
@RequiredArgsConstructor
public class ActivityController {

	private final IActivityArmory activityArmory;
	private final IStrategyArmory strategyArmory;

	/**
	 * 活动装配 - 数据预热 - 把活动配置的对应的 sku 一起装配
	 */
	@PostMapping
	public ResponseResult<Boolean> armory(@RequestParam Long activityId){
		log.info("活动装配，数据预热，开始 activityId:{}", activityId);

		// 活动装配
		activityArmory.assembleActivitySkuByActivityId(activityId);
		// 策略装配
		strategyArmory.assembleLotteryStrategyByActivityId(activityId);

		return ResponseResult.success();
	}
}
