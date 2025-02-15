package com.yu.market.server.coupon.mq.consumer.distribution;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yu.market.common.contants.CouponRedisKey;
import com.yu.market.common.redis.RedissonService;
import com.yu.market.common.utils.ScriptUtil;
import com.yu.market.infrastructure.coupon.mapper.CouponTaskFailMapper;
import com.yu.market.infrastructure.coupon.mapper.CouponTaskMapper;
import com.yu.market.infrastructure.coupon.mapper.CouponTemplateMapper;
import com.yu.market.infrastructure.coupon.mapper.UserCouponMapper;
import com.yu.market.infrastructure.coupon.pojo.CouponTask;
import com.yu.market.infrastructure.coupon.pojo.CouponTaskFail;
import com.yu.market.infrastructure.coupon.pojo.CouponTemplate;
import com.yu.market.infrastructure.coupon.pojo.UserCoupon;
import com.yu.market.server.coupon.model.enums.CouponSourceEnum;
import com.yu.market.server.coupon.model.enums.CouponStatusEnum;
import com.yu.market.server.coupon.model.enums.CouponTaskStatusEnum;
import com.yu.market.server.coupon.mq.base.MessageWrapper;
import com.yu.market.server.coupon.mq.event.CouponTemplateDistributionEvent;
import com.yu.market.server.coupon.service.distribution.excel.UserCouponTaskFailExcelObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.BatchExecutorException;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.redisson.api.RScript;
import org.redisson.api.RSet;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Paths;
import java.util.*;

import static com.yu.market.common.contants.CouponRocketMQConstant.DistributionRocketMQConstant.TEMPLATE_EXECUTE_DISTRIBUTION_CG_KEY;
import static com.yu.market.common.contants.CouponRocketMQConstant.DistributionRocketMQConstant.TEMPLATE_EXECUTE_DISTRIBUTION_TOPIC_KEY;

/**
 * @author yu
 * @description 优惠券执行分发到用户消费者
 * @date 2025-02-15
 */
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
		topic = TEMPLATE_EXECUTE_DISTRIBUTION_TOPIC_KEY,
		consumerGroup = TEMPLATE_EXECUTE_DISTRIBUTION_CG_KEY
)
@Slf4j(topic = "CouponExecuteDistributionConsumer")
public class CouponExecuteDistributionConsumer implements RocketMQListener<MessageWrapper<CouponTemplateDistributionEvent>> {

	private final UserCouponMapper userCouponMapper;
	private final CouponTemplateMapper couponTemplateMapper;
	private final CouponTaskMapper couponTaskMapper;
	private final CouponTaskFailMapper couponTaskFailMapper;
	private final RedissonService redissonService;

	@Lazy
	private final CouponExecuteDistributionConsumer couponExecuteDistributionConsumer;


	private final static int BATCH_USER_COUPON_SIZE = 5000;
	private static final String BATCH_SAVE_USER_COUPON_LUA_PATH = "lua/batch_user_coupon_list.lua";
	private final String excelPath = Paths.get("").toAbsolutePath() + "/tmp";

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void onMessage(MessageWrapper<CouponTemplateDistributionEvent> messageWrapper) {
		log.info("[消费者] 优惠券任务执行推送@分发到用户账号 - 执行消费逻辑，消息体：{}", JSONUtil.toJsonStr(messageWrapper));

		// 当保存用户优惠券集合达到批量保存数量
		// 这里之所以取余是因为，如果最后结束的消息比批量消费的消息先到达，那就不会执行中间那个批量消息了
		CouponTemplateDistributionEvent event = messageWrapper.getMessage();
		if (!event.getDistributionEndFlag() && event.getBatchUserSetSize() % BATCH_USER_COUPON_SIZE == 0) {
			decrementCouponTemplateStockAndSaveUserCouponList(event);
		}

		// 分发任务结束标识为 TRUE，代表已经没有 Excel 记录了
		if (event.getDistributionEndFlag()) {
			String batchUserSetKey = String.format(CouponRedisKey.DistributionRedisConstant.TEMPLATE_TASK_EXECUTE_BATCH_USER_KEY, event.getCouponTaskId());
			int batchUserIdsSize = 0;
			RSet<Object> batchUserIdsSet = redissonService.getSet(batchUserSetKey);
			if (batchUserIdsSet != null) {
				batchUserIdsSize = batchUserIdsSet.size();
			}
			event.setBatchUserSetSize(batchUserIdsSize);

			decrementCouponTemplateStockAndSaveUserCouponList(event);
			List<String> batchUserMaps = redissonService.popFromSet(batchUserSetKey, Integer.MAX_VALUE);
			// 此时待保存入库用户优惠券列表如果还有值，就意味着可能库存不足引起的
			if (CollUtil.isNotEmpty(batchUserMaps)) {
				// 添加到 t_coupon_task_fail 并标记错误原因，方便后续查看未成功发送的原因和记录
				List<CouponTaskFail> couponTaskFailDOList = new ArrayList<>(batchUserMaps.size());
				for (String batchUserMapStr : batchUserMaps) {
					Map<Object, Object> objectMap = MapUtil.builder()
							.put("rowNum", JSON.parseObject(batchUserMapStr).get("rowNum"))
							.put("cause", "用户已领取该优惠券")
							.build();
					CouponTaskFail couponTaskFailDO = CouponTaskFail.builder()
							.batchId(event.getCouponTaskBatchId())
							.jsonObject(com.alibaba.fastjson.JSON.toJSONString(objectMap))
							.build();
					couponTaskFailDOList.add(couponTaskFailDO);
				}

				// 添加到 t_coupon_task_fail 并标记错误原因
				couponTaskFailMapper.insert(couponTaskFailDOList);
			}

			long initId = 0;
			boolean isFirstIteration = true;  // 用于标识是否为第一次迭代
			String failFileAddress = excelPath + "/用户分发记录失败Excel-" + event.getCouponTaskBatchId() + ".xlsx";

			try (ExcelWriter excelWriter = EasyExcel.write(failFileAddress, UserCouponTaskFailExcelObject.class).build()) {
				WriteSheet writeSheet = EasyExcel.writerSheet("用户分发失败Sheet").build();
				while (true) {
					List<CouponTaskFail> couponTaskFailDOList = listUserCouponTaskFail(event.getCouponTaskBatchId(), initId);
					if (CollUtil.isEmpty(couponTaskFailDOList)) {
						// 如果是第一次迭代且集合为空，则设置 failFileAddress 为 null
						if (isFirstIteration) {
							failFileAddress = null;
						}
						break;
					}

					// 标记第一次迭代已经完成
					isFirstIteration = false;

					// 将失败行数和失败原因写入 Excel 文件
					List<UserCouponTaskFailExcelObject> excelDataList = couponTaskFailDOList.stream()
							.map(each -> JSONObject.parseObject(each.getJsonObject(), UserCouponTaskFailExcelObject.class))
							.toList();
					excelWriter.write(excelDataList, writeSheet);

					// 查询出来的数据如果小于 BATCH_USER_COUPON_SIZE 意味着后面将不再有数据，返回即可
					if (couponTaskFailDOList.size() < BATCH_USER_COUPON_SIZE) {
						break;
					}

					// 更新 initId 为当前列表中最大 ID
					initId = couponTaskFailDOList.stream()
							.mapToLong(CouponTaskFail::getId)
							.max()
							.orElse(initId);
				}
			}

			// 确保所有用户都已经接到优惠券后，设置优惠券推送任务完成时间
			CouponTask couponTaskDO = CouponTask.builder()
					.id(event.getCouponTaskId())
					.status(CouponTaskStatusEnum.SUCCESS.getStatus())
					.failFileAddress(failFileAddress)
					.completionTime(new Date())
					.build();
			couponTaskMapper.updateById(couponTaskDO);
		}

	}

	/**
	 * 扣减库存并保存用户优惠券
	 */
	@SneakyThrows
	private void decrementCouponTemplateStockAndSaveUserCouponList(CouponTemplateDistributionEvent event) {
		// 如果等于 0 意味着已经没有了库存，直接返回即可
		// 如果不为 0 ，couponTemplateStock就是成功扣除库存的数量
		Integer couponTemplateStock = decrementCouponTemplateStock(event, event.getBatchUserSetSize());
		if (couponTemplateStock <= 0) {
			return;
		}

		String batchUserSetKey = String.format(CouponRedisKey.DistributionRedisConstant.TEMPLATE_TASK_EXECUTE_BATCH_USER_KEY, event.getCouponTaskId());
		List<String> batchUserMaps = redissonService.popFromSet(batchUserSetKey, couponTemplateStock);
		List<UserCoupon> userCouponList = getUserCouponList(batchUserMaps, event);

		// 平台优惠券每个用户限领一次 - 批量新增用户优惠券记录
		batchSaveUserCouponList(event.getCouponTemplateId(), event.getCouponTaskBatchId(), userCouponList);

		// 将这些优惠券添加到用户的领券记录中
		List<String> userIdList = userCouponList.stream()
				.map(UserCoupon::getUserId)
				.map(String::valueOf)
				.toList();
		String userIdsJson = new ObjectMapper().writeValueAsString(userIdList);

		List<String> couponIdList = userCouponList.stream()
				.map(each -> StrUtil.builder()
						.append(event.getCouponTemplateId())
						.append("_")
						.append(each.getId())
						.toString())
				.map(String::valueOf)
				.toList();
		String couponIdsJson = new ObjectMapper().writeValueAsString(couponIdList);

		// 调用 Lua 脚本时，传递参数
		List<Object> keys = List.of(CouponRedisKey.EngineRedisConstant.USER_COUPON_TEMPLATE_LIST_KEY);
		List<Object> args = Arrays.asList(userIdsJson, couponIdsJson, String.valueOf(new Date().getTime()));
		String luaScript = Singleton.get("batchSaveUserCouponLuaScript", () -> ScriptUtil.loadLuaScript(BATCH_SAVE_USER_COUPON_LUA_PATH));

		redissonService.executeLuaScript(luaScript, RScript.ReturnType.STATUS, keys, args.toArray());
	}

	/**
	 * 批量插入用户优惠券
	 */
	private void batchSaveUserCouponList(Long couponTemplateId, Long couponTaskBatchId, List<UserCoupon> userCouponDOList) {
		try {
			userCouponMapper.insert(userCouponDOList, userCouponDOList.size());
		} catch (Exception ex) {
			if (ex.getCause() instanceof BatchExecutorException) {
				handleBatchInsertFailure(couponTemplateId, couponTaskBatchId, userCouponDOList);
			} else {
				throw ex;
			}
		}
	}

	/**
	 * 处理批量插入失败
	 */
	private void handleBatchInsertFailure(Long couponTemplateId, Long couponTaskBatchId, List<UserCoupon> userCouponDOList) {
		// 存储失败的记录和需要移除的记录
		List<CouponTaskFail> couponTaskFailDOList = new ArrayList<>();
		List<UserCoupon> toRemove = new ArrayList<>();

		// 逐条尝试插入
		for (UserCoupon userCoupon : userCouponDOList) {
			try {
				userCouponMapper.insert(userCoupon);
			} catch (Exception ignored) {
				processFailedInsert(couponTemplateId, couponTaskBatchId, userCoupon, couponTaskFailDOList, toRemove);
			}
		}

		// 批量保存失败记录到 t_coupon_task_fail 表
		if (!couponTaskFailDOList.isEmpty()) {
			couponTaskFailMapper.insert(couponTaskFailDOList, couponTaskFailDOList.size());
		}

		// 从原列表中移除已存在的记录
		userCouponDOList.removeAll(toRemove);
	}

	/**
	 * 插入失败记录
	 */
	private void processFailedInsert(Long couponTemplateId, Long couponTaskBatchId, UserCoupon userCouponDO,
									 List<CouponTaskFail> couponTaskFailDOList, List<UserCoupon> toRemove) {
		Boolean hasReceived = couponExecuteDistributionConsumer.hasUserReceivedCoupon(couponTemplateId, userCouponDO.getUserId());
		if (Boolean.TRUE.equals(hasReceived)) {
			// 构建失败记录的原因
			Map<Object, Object> failureDetails = MapUtil.builder()
					.put("rowNum", userCouponDO.getRowNum())
					.put("cause", "用户已领取该优惠券")
					.build();

			// 创建失败记录对象
			CouponTaskFail couponTaskFailDO = CouponTaskFail.builder()
					.batchId(couponTaskBatchId)
					.jsonObject(com.alibaba.fastjson.JSON.toJSONString(failureDetails))
					.build();
			couponTaskFailDOList.add(couponTaskFailDO);

			// 标记此记录为需要移除
			toRemove.add(userCouponDO);
		}
	}

	/**
	 * 构建 userCouponDOList 用户优惠券批量数组
	 */
	private List<UserCoupon> getUserCouponList(List<String> batchUserMaps, CouponTemplateDistributionEvent event) {
		List<UserCoupon> userCouponList = new ArrayList<>(Objects.requireNonNull(batchUserMaps).size());
		Date now = new Date();

		for (String each : batchUserMaps) {
			JSONObject userIdAndRowNumJsonObject = JSON.parseObject(each);
			DateTime validEndTime = DateUtil.offsetHour(now, JSON.parseObject(event.getCouponTemplateConsumeRule()).getInteger("validityPeriod"));

			UserCoupon userCouponDO = UserCoupon.builder()
					.id(IdUtil.getSnowflakeNextId())
					.couponTemplateId(event.getCouponTemplateId())
					.rowNum(userIdAndRowNumJsonObject.getInteger("rowNum"))
					.userId(userIdAndRowNumJsonObject.getLong("userId"))
					.receiveTime(now)
					.receiveCount(1) // 代表第一次领取该优惠券
					.validStartTime(now)
					.validEndTime(validEndTime)
					.source(CouponSourceEnum.PLATFORM.getType())
					.status(CouponStatusEnum.EFFECTIVE.getType())
					.createTime(new Date())
					.updateTime(new Date())
					.delFlag(0)
					.build();

			userCouponList.add(userCouponDO);
		}

		return userCouponList;
	}


	/**
	 * 减少优惠券库存，这里就是递归，把这个优惠券的库存减少为零
	 *
	 * @param event              优惠卷模版的信息
	 * @param decrementStockSize 需要减少库存的数量
	 * @return 成功扣减的库存数量
	 */
	private Integer decrementCouponTemplateStock(CouponTemplateDistributionEvent event, Integer decrementStockSize) {
		// 通过条件自减优惠券库存记录
		Long couponTemplateId = event.getCouponTemplateId();
		int decremented = couponTemplateMapper.decrementCouponTemplateStock(event.getShopNumber(), couponTemplateId, decrementStockSize);

		// 如果修改记录失败，意味着优惠券库存已不足，需要重试获取到可自减的库存数值
		if (!SqlHelper.retBool(decremented)) {
			CouponTemplate couponTemplateDO = couponTemplateMapper.selectOne(new LambdaQueryWrapper<CouponTemplate>()
					.eq(CouponTemplate::getShopNumber, event.getShopNumber())
					.eq(CouponTemplate::getId, couponTemplateId));

			return decrementCouponTemplateStock(event, couponTemplateDO.getStock());
		}

		return decrementStockSize;
	}


	/**
	 * 查询用户是否已经领取过优惠券
	 *
	 * @param couponTemplateId 优惠券模板 ID
	 * @param userId           用户 ID
	 * @return 用户优惠券模板领取信息是否已存在
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
	public Boolean hasUserReceivedCoupon(Long couponTemplateId, Long userId) {
		LambdaQueryWrapper<UserCoupon> queryWrapper = Wrappers.lambdaQuery(UserCoupon.class)
				.eq(UserCoupon::getUserId, userId)
				.eq(UserCoupon::getCouponTemplateId, couponTemplateId);
		return userCouponMapper.selectOne(queryWrapper) != null;
	}

	/**
	 * 查询用户分发任务失败记录
	 *
	 * @param batchId 分发任务批次 ID
	 * @param maxId   上次读取最大 ID
	 * @return 用户分发任务失败记录集合
	 */
	private List<CouponTaskFail> listUserCouponTaskFail(Long batchId, Long maxId) {
		LambdaQueryWrapper<CouponTaskFail> queryWrapper = Wrappers.lambdaQuery(CouponTaskFail.class)
				.eq(CouponTaskFail::getBatchId, batchId)
				.gt(CouponTaskFail::getId, maxId)
				.last("LIMIT " + BATCH_USER_COUPON_SIZE);
		return couponTaskFailMapper.selectList(queryWrapper);
	}
}
