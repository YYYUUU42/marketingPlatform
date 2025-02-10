package com.yu.market.server.activity.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.common.result.ResponseResult;
import com.yu.market.common.utils.BeanCopyUtil;
import com.yu.market.common.utils.RandomStringUtil;
import com.yu.market.server.activity.model.bo.*;
import com.yu.market.server.activity.model.dto.ActivityRaffleDTO;
import com.yu.market.server.activity.model.dto.SkuProductShopCartDTO;
import com.yu.market.server.activity.model.dto.UserActivityAccountDTO;
import com.yu.market.server.activity.model.enums.*;
import com.yu.market.server.activity.model.vo.ActivityRaffleVO;
import com.yu.market.server.activity.model.vo.SkuProductVO;
import com.yu.market.server.activity.model.vo.UserActivityAccountVO;
import com.yu.market.server.activity.service.IRaffleActivityAccountQuotaService;
import com.yu.market.server.activity.service.IRaffleActivityPartakeService;
import com.yu.market.server.activity.service.IRaffleActivitySkuProductService;
import com.yu.market.server.activity.service.armory.IActivityArmory;
import com.yu.market.server.activity.service.award.IAwardService;
import com.yu.market.server.activity.service.credit.ICreditAdjustService;
import com.yu.market.server.activity.service.rebate.IBehaviorRebateService;
import com.yu.market.server.raffle.model.bo.RaffleAwardBO;
import com.yu.market.server.raffle.model.bo.RaffleFactorBO;
import com.yu.market.server.raffle.model.bo.StrategyAwardBO;
import com.yu.market.server.raffle.model.dto.RaffleAwardListDTO;
import com.yu.market.server.raffle.model.dto.RaffleStrategyRuleWeightDTO;
import com.yu.market.server.raffle.model.vo.RaffleAwardListVO;
import com.yu.market.server.raffle.model.vo.RaffleStrategyRuleWeightVO;
import com.yu.market.server.raffle.model.vo.RuleWeightVO;
import com.yu.market.server.raffle.service.armory.IStrategyArmory;
import com.yu.market.server.raffle.service.raffle.IRaffleAward;
import com.yu.market.server.raffle.service.raffle.IRaffleRule;
import com.yu.market.server.raffle.service.raffle.IRaffleStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	private final IBehaviorRebateService behaviorRebateService;
	private final IRaffleActivitySkuProductService raffleActivitySkuProductService;
	private final IRaffleActivityAccountQuotaService raffleActivityAccountQuotaService;
	private final ICreditAdjustService creditAdjustService;
	private final IRaffleAward raffleAward;
	private final IRaffleRule raffleRule;

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

	/**
	 * 日历签到返利
	 */
	@PostMapping("/calendarSignRebate")
	public ResponseResult<Boolean> calendarSignRebate(@RequestParam String userId) {
		log.info("日历签到返利开始 userId:{}", userId);
		if (StrUtil.isBlank(userId)) {
			throw new ServiceException(BaseErrorCode.ILLEGAL_PARAMETER);
		}

		BehaviorBO behaviorBO = BehaviorBO.builder()
				.userId(userId)
				.behaviorTypeEnum(BehaviorTypeEnum.SIGN)
				.outBusinessNo(dateFormatDay.format(new Date()))
				.build();

		List<String> orderIds = behaviorRebateService.createOrder(behaviorBO);
		log.info("日历签到返利完成 userId:{} orderIds: {}", userId, JSONUtil.toJsonStr(orderIds));

		return ResponseResult.success(true);
	}

	/**
	 * 判断是否签到
	 */
	@GetMapping("/isCalendarSignRebate")
	public ResponseResult<Boolean> isCalendarSignRebate(@RequestParam String userId) {
		log.info("查询用户是否完成日历签到返利开始 userId:{}", userId);
		if (StrUtil.isBlank(userId)) {
			throw new ServiceException(BaseErrorCode.ILLEGAL_PARAMETER);
		}

		String outBusinessNo = dateFormatDay.format(new Date());
		List<BehaviorRebateOrderBO> behaviorRebateOrderBOList = behaviorRebateService.queryOrderByOutBusinessNo(userId, outBusinessNo);
		log.info("查询用户是否完成日历签到返利完成 userId:{} orders.size:{}", userId, behaviorRebateOrderBOList.size());

		return ResponseResult.success(!CollectionUtil.isEmpty(behaviorRebateOrderBOList));
	}

	/**
	 * 查询sku商品集合
	 */
	@GetMapping("/querySkuProductListByActivityId")
	public ResponseResult<List<SkuProductVO>> querySkuProductListByActivityId(@RequestParam Long activityId) {
		log.info("查询sku商品集合开始 activityId:{}", activityId);
		if (activityId ==null){
			throw new ServiceException(BaseErrorCode.ILLEGAL_PARAMETER);
		}

		// 查询商品并封装数据
		List<SkuProductBO> skuProductBOList = raffleActivitySkuProductService.querySkuProductBOListByActivityId(activityId);
		List<SkuProductVO> skuProductVOS = BeanCopyUtil.copyListProperties(skuProductBOList, SkuProductVO.class);

		log.info("查询sku商品集合完成 activityId:{} skuProductResponseDTOS:{}", activityId, JSONUtil.toJsonStr(skuProductVOS));

		return ResponseResult.success(skuProductVOS);
	}

	/**
	 * 查询账户额度
	 */
	@GetMapping("/queryUserActivityAccount")
	public ResponseResult<UserActivityAccountVO> queryUserActivityAccount(@RequestBody UserActivityAccountDTO dto) {
		log.info("查询用户活动账户开始 userId:{} activityId:{}", dto.getUserId(), dto.getActivityId());
		if (StrUtil.isBlank(dto.getUserId()) || dto.getActivityId() == null) {
			throw new ServiceException(BaseErrorCode.ILLEGAL_PARAMETER);
		}

		ActivityAccountBO activityAccountBO = raffleActivityAccountQuotaService.queryActivityAccountBO(dto.getActivityId(), dto.getUserId());
		UserActivityAccountVO userActivityAccountVO = BeanCopyUtil.copyProperties(activityAccountBO, UserActivityAccountVO.class);

		return ResponseResult.success(userActivityAccountVO);
	}

	/**
	 * 查询用户积分值
	 */
	@GetMapping("/queryUserCreditAccount")
	public ResponseResult<BigDecimal> queryUserCreditAccount(@RequestParam String userId){
		log.info("查询用户积分值开始 userId:{}", userId);
		if (StrUtil.isBlank(userId)){
			throw new ServiceException(BaseErrorCode.ILLEGAL_PARAMETER);
		}

		CreditAccountBO creditAccountBO = creditAdjustService.queryUserCreditAccount(userId);

		log.info("查询用户积分值完成 userId:{} adjustAmount:{}", userId, creditAccountBO.getAdjustAmount());

		return ResponseResult.success(creditAccountBO.getAdjustAmount());
	}

	/**
	 * 积分兑换商品
	 */
	@PostMapping("/creditPayExchangeSku")
	public ResponseResult<Boolean> creditPayExchangeSku(@RequestBody SkuProductShopCartDTO dto) {
		log.info("积分兑换商品开始 userId:{} sku:{}", dto.getUserId(), dto.getSku());
		if (StrUtil.isBlank(dto.getUserId()) || dto.getSku() == null) {
			throw new ServiceException(BaseErrorCode.ILLEGAL_PARAMETER);
		}


		// 创建兑换商品sku订单，outBusinessNo 每次创建出一个单
		SkuRechargeBO skuRechargeBO = SkuRechargeBO.builder()
				.userId(dto.getUserId())
				.sku(dto.getSku())
				.outBusinessNo(RandomStringUtil.randomNumeric(12))
				.orderTradeType(OrderTradeTypeEnum.credit_pay_trade)
				.build();
		UnpaidActivityOrderBO unpaidActivityOrderBO = raffleActivityAccountQuotaService.createOrder(skuRechargeBO);
		log.info("积分兑换商品，创建订单完成 userId:{} sku:{} outBusinessNo:{}", dto.getUserId(), dto.getSku(), unpaidActivityOrderBO.getOutBusinessNo());

		// 支付兑换商品
		TradeBO tradeBO = TradeBO.builder()
				.userId(unpaidActivityOrderBO.getUserId())
				.tradeName(TradeNameEnum.CONVERT_SKU)
				.tradeType(TradeTypeEnum.REVERSE)
				.amount(unpaidActivityOrderBO.getPayAmount().negate())
				.outBusinessNo(unpaidActivityOrderBO.getOutBusinessNo())
				.build();
		String orderId = creditAdjustService.createOrder(tradeBO);
		log.info("积分兑换商品，支付订单完成  userId:{} sku:{} orderId:{}", dto.getUserId(), dto.getSku(), orderId);

		return ResponseResult.success(true);
	}

	/**
	 * 查询权重规则
	 */
	@GetMapping("/queryRaffleStrategyRuleWeight")
	public ResponseResult<List<RaffleStrategyRuleWeightVO>> queryRaffleStrategyRuleWeight(@RequestBody RaffleStrategyRuleWeightDTO dto) {
		log.info("查询抽奖策略权重规则配置开始 userId:{} activityId：{}", dto.getUserId(), dto.getActivityId());
		if (StrUtil.isBlank(dto.getUserId()) || dto.getActivityId() == null) {
			throw new ServiceException(BaseErrorCode.ILLEGAL_PARAMETER);
		}

		// 查询用户抽奖总次数
		Integer userActivityAccountTotalUseCount = raffleActivityAccountQuotaService.queryRaffleActivityAccountPartakeCount(dto.getActivityId(), dto.getUserId());

		// 查询规则
		List<RuleWeightVO> ruleWeightVOList = raffleRule.queryAwardRuleWeightByActivityId(dto.getActivityId());

		List<RaffleStrategyRuleWeightVO> raffleStrategyRuleWeightVOList = new ArrayList<>();
		for (RuleWeightVO ruleWeightVO : ruleWeightVOList) {
			List<RaffleStrategyRuleWeightVO.StrategyAward> strategyAwards = new ArrayList<>();
			List<RuleWeightVO.Award> awardList = ruleWeightVO.getAwardList();

			for (RuleWeightVO.Award award : awardList) {
				RaffleStrategyRuleWeightVO.StrategyAward strategyAward = new RaffleStrategyRuleWeightVO.StrategyAward();
				strategyAward.setAwardId(award.getAwardId());
				strategyAward.setAwardTitle(award.getAwardTitle());
				strategyAwards.add(strategyAward);
			}

			RaffleStrategyRuleWeightVO raffleStrategyRuleWeightVO = new RaffleStrategyRuleWeightVO();
			raffleStrategyRuleWeightVO.setRuleWeightCount(ruleWeightVO.getWeight());
			raffleStrategyRuleWeightVO.setStrategyAwards(strategyAwards);
			raffleStrategyRuleWeightVO.setUserActivityAccountTotalUseCount(userActivityAccountTotalUseCount);

			raffleStrategyRuleWeightVOList.add(raffleStrategyRuleWeightVO);
		}

		return ResponseResult.success(raffleStrategyRuleWeightVOList);
	}


}
