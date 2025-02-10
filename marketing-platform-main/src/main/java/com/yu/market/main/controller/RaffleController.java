package com.yu.market.main.controller;

import cn.hutool.core.util.StrUtil;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.exception.errorCode.BaseErrorCode;
import com.yu.market.common.result.ResponseResult;
import com.yu.market.common.utils.BeanCopyUtil;
import com.yu.market.server.activity.service.IRaffleActivityAccountQuotaService;
import com.yu.market.server.raffle.model.bo.RaffleAwardBO;
import com.yu.market.server.raffle.model.bo.RaffleFactorBO;
import com.yu.market.server.raffle.model.bo.StrategyAwardBO;
import com.yu.market.server.raffle.model.dto.RaffleAwardListDTO;
import com.yu.market.server.raffle.model.dto.RaffleDTO;
import com.yu.market.server.raffle.model.dto.RaffleStrategyRuleWeightDTO;
import com.yu.market.server.raffle.model.vo.RaffleAwardListVO;
import com.yu.market.server.raffle.model.vo.RaffleStrategyRuleWeightVO;
import com.yu.market.server.raffle.model.vo.RaffleVO;
import com.yu.market.server.raffle.model.vo.RuleWeightVO;
import com.yu.market.server.raffle.service.armory.IStrategyArmory;
import com.yu.market.server.raffle.service.raffle.IRaffleAward;
import com.yu.market.server.raffle.service.raffle.IRaffleRule;
import com.yu.market.server.raffle.service.raffle.IRaffleStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/raffle")
@RequiredArgsConstructor
public class RaffleController {

	private final IStrategyArmory strategyArmory;
	private final IRaffleAward raffleAward;
	private final IRaffleRule raffleRule;
	private final IRaffleStrategy raffleStrategy;
	private final IRaffleActivityAccountQuotaService raffleActivityAccountQuotaService;

	/**
	 * 策略装配，将策略信息装配到缓存中
	 */
	@GetMapping("/strategyArmory")
	public ResponseResult<Boolean> strategyArmory(@RequestParam Long strategyId) {
		boolean armoryStatus = strategyArmory.assembleLotteryStrategy(strategyId);
		log.info("抽奖策略装配完成 strategyId：{}", strategyId);

		return ResponseResult.success(armoryStatus);
	}

	/**
	 * 随机抽奖接口
	 */
	@GetMapping("/randomRaffle")
	public ResponseResult<RaffleVO> randomRaffle(@RequestBody RaffleDTO requestDTO) {
		log.info("随机抽奖开始 strategyId: {}", requestDTO.getStrategyId());

		// 调用抽奖接口
		RaffleFactorBO raffleFactorBO = RaffleFactorBO.builder()
				.userId("test")
				.strategyId(requestDTO.getStrategyId())
				.build();
		RaffleAwardBO raffleAwardBO = raffleStrategy.performRaffle(raffleFactorBO);

		RaffleVO vo = RaffleVO.builder()
				.awardId(raffleAwardBO.getAwardId())
				.awardIndex(raffleAwardBO.getSort())
				.build();

		return ResponseResult.success(vo);
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

	/**
	 * 查询奖品列表
	 */
	@GetMapping("/queryRaffleAwardList")
	public ResponseResult<List<RaffleAwardListVO>> queryRaffleAwardList(@RequestBody RaffleAwardListDTO dto) {
		log.info("查询抽奖奖品列表配开始 userId:{} activityId：{}", dto.getUserId(), dto.getActivityId());
		if (StrUtil.isBlank(dto.getUserId()) || dto.getActivityId() == null) {
			throw new ServiceException(BaseErrorCode.ILLEGAL_PARAMETER);
		}

		// 查询奖品配置
		List<StrategyAwardBO> strategyAwardBOList = raffleAward.queryRaffleStrategyAwardListByActivityId(dto.getActivityId());

		// 获取规则配置
		String[] treeIds = strategyAwardBOList.stream()
				.map(StrategyAwardBO::getRuleModels)
				.filter(StrUtil::isBlank)
				.toArray(String[]::new);

		// 查询规则配置 - 获取奖品的解锁限制，抽奖N次后解锁
		Map<String, Integer> ruleLockCountMap = raffleRule.queryAwardRuleLockCount(treeIds);

		// 查询抽奖次数 - 用户已经参与的抽奖次数
		Integer dayPartakeCount = raffleActivityAccountQuotaService.queryRaffleActivityAccountDayPartakeCount(dto.getActivityId(), dto.getUserId());

		// 遍历填充数据
		List<RaffleAwardListVO> vos = new ArrayList<>(strategyAwardBOList.size());
		for (StrategyAwardBO strategyAwardBO : strategyAwardBOList) {
			Integer awardRuleLockCount = ruleLockCountMap.get(strategyAwardBO.getRuleModels());

			RaffleAwardListVO raffleAwardListVO = BeanCopyUtil.copyProperties(strategyAwardBO, RaffleAwardListVO.class);
			raffleAwardListVO.setAwardRuleLockCount(awardRuleLockCount);
			raffleAwardListVO.setIsAwardUnlock(awardRuleLockCount == null || dayPartakeCount >= awardRuleLockCount);
			raffleAwardListVO.setWaitUnLockCount(awardRuleLockCount == null || dayPartakeCount > awardRuleLockCount ? 0 : awardRuleLockCount - dayPartakeCount);
			vos.add(raffleAwardListVO);
		}

		return ResponseResult.success(vos);
	}
}
