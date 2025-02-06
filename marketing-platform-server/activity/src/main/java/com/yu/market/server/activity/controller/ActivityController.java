package com.yu.market.server.activity.controller;

import cn.hutool.core.util.StrUtil;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.common.result.ResponseResult;
import com.yu.market.server.activity.model.bo.BehaviorBO;
import com.yu.market.server.activity.model.bo.UserAwardRecordBO;
import com.yu.market.server.activity.model.bo.UserRaffleOrderBO;
import com.yu.market.server.activity.model.dto.ActivityRaffleDTO;
import com.yu.market.server.activity.model.enums.AwardStateEnum;
import com.yu.market.server.activity.model.enums.BehaviorTypeEnum;
import com.yu.market.server.activity.model.vo.ActivityRaffleVO;
import com.yu.market.server.activity.service.IRaffleActivityPartakeService;
import com.yu.market.server.activity.service.armory.IActivityArmory;
import com.yu.market.server.activity.service.award.IAwardService;
import com.yu.market.server.raffle.model.bo.RaffleAwardBO;
import com.yu.market.server.raffle.model.bo.RaffleFactorBO;
import com.yu.market.server.raffle.service.armory.IStrategyArmory;
import com.yu.market.server.raffle.service.raffle.IRaffleStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@RestController
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/activity")
@RequiredArgsConstructor
public class ActivityController {

	private final IActivityArmory activityArmory;
	private final IStrategyArmory strategyArmory;
	private final IRaffleActivityPartakeService raffleActivityPartakeService;
	private final IRaffleStrategy raffleStrategy;
	private final IAwardService awardService;

	private final SimpleDateFormat dateFormatDay = new SimpleDateFormat("yyyyMMdd");

	/**
	 * 活动装配 - 数据预热 - 把活动配置的对应的 sku 一起装配
	 */
	@PostMapping("/armory")
	public ResponseResult<Boolean> armory(@RequestParam Long activityId){
		log.info("活动装配，数据预热，开始 activityId:{}", activityId);

		// 活动装配
		boolean armory1 = activityArmory.assembleActivitySkuByActivityId(activityId);
		// 策略装配
		boolean armory2 = strategyArmory.assembleLotteryStrategyByActivityId(activityId);

		if (armory1 && armory2) {
			return ResponseResult.success();
		}

		return ResponseResult.error(BaseErrorCode.SERVICE_ERROR);
	}

	/**
	 * 抽奖接口
	 */
	@PostMapping("/activityRaffle")
	public ResponseResult<ActivityRaffleVO> activityRaffle(@RequestBody ActivityRaffleDTO dto) {
		log.info("活动抽奖开始 userId:{} activityId:{}", dto.getUserId(), dto.getActivityId());

		// 参与活动 - 创建参与记录订单
		UserRaffleOrderBO userRaffleOrderBO = raffleActivityPartakeService.createOrder(dto.getUserId(), dto.getActivityId());
		log.info("活动抽奖，创建订单 userId:{} activityId:{} orderId:{}", dto.getUserId(), dto.getActivityId(), userRaffleOrderBO.getOrderId());

		// 抽奖策略 - 执行抽奖
		RaffleAwardBO raffleAwardBO = raffleStrategy.performRaffle(RaffleFactorBO.builder()
				.userId(userRaffleOrderBO.getUserId())
				.strategyId(userRaffleOrderBO.getStrategyId())
				.endDateTime(userRaffleOrderBO.getEndDateTime())
				.build());

		UserAwardRecordBO userAwardRecordBO = UserAwardRecordBO.builder()
				.userId(userRaffleOrderBO.getUserId())
				.activityId(userRaffleOrderBO.getActivityId())
				.strategyId(userRaffleOrderBO.getStrategyId())
				.orderId(userRaffleOrderBO.getOrderId())
				.awardId(raffleAwardBO.getAwardId())
				.awardTitle(raffleAwardBO.getAwardTitle())
				.awardTime(new Date())
				.awardState(AwardStateEnum.create)
				.awardConfig(raffleAwardBO.getAwardConfig())
				.build();

		awardService.saveUserAwardRecord(userAwardRecordBO);

		ActivityRaffleVO vo = ActivityRaffleVO.builder()
				.awardId(raffleAwardBO.getAwardId())
				.awardTitle(raffleAwardBO.getAwardTitle())
				.awardIndex(raffleAwardBO.getSort())
				.build();

		return ResponseResult.success(vo);

	}

}
