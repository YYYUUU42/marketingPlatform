package com.yu.market.server.raffle.controller;

import com.yu.market.common.result.ResponseResult;
import com.yu.market.common.utils.BeanCopyUtil;
import com.yu.market.server.raffle.model.bo.RaffleAwardBO;
import com.yu.market.server.raffle.model.bo.RaffleFactorBO;
import com.yu.market.server.raffle.model.bo.StrategyAwardBO;
import com.yu.market.server.raffle.model.dto.RaffleAwardListDTO;
import com.yu.market.server.raffle.model.dto.RaffleDTO;
import com.yu.market.server.raffle.model.vo.RaffleAwardListVO;
import com.yu.market.server.raffle.model.vo.RaffleVO;
import com.yu.market.server.raffle.service.armory.IStrategyArmory;
import com.yu.market.server.raffle.service.raffle.IRaffleAward;
import com.yu.market.server.raffle.service.raffle.IRaffleStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/raffle")
@RequiredArgsConstructor
public class RaffleController {

	private final IStrategyArmory strategyArmory;
	private final IRaffleAward raffleAward;
	private final IRaffleStrategy raffleStrategy;

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
	 * 查询奖品列表
	 */
	@GetMapping("/queryRaffleAwardList")
	public ResponseResult<List<RaffleAwardListVO>> queryRaffleAwardList(@RequestBody RaffleAwardListDTO raffleAwardListDTO) {
		// 查询奖品配置
		List<StrategyAwardBO> strategyAwardBOList = raffleAward.queryRaffleStrategyAwardList(raffleAwardListDTO.getStrategyId());

		List<RaffleAwardListVO> vos = BeanCopyUtil.copyListProperties(strategyAwardBOList, RaffleAwardListVO.class);

		return ResponseResult.success(vos);
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
}
