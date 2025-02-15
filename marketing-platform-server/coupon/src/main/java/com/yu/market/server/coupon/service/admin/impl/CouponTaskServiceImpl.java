package com.yu.market.server.coupon.service.admin.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.redis.RedissonService;
import com.yu.market.common.utils.BeanCopyUtil;
import com.yu.market.common.utils.SnowFlakeUtil;
import com.yu.market.infrastructure.coupon.mapper.CouponTaskMapper;
import com.yu.market.infrastructure.coupon.pojo.CouponTask;
import com.yu.market.server.coupon.context.UserContext;
import com.yu.market.server.coupon.handler.excel.RowCountListener;
import com.yu.market.server.coupon.model.dto.CouponTaskCreateDTO;
import com.yu.market.server.coupon.model.dto.CouponTaskPageQueryDTO;
import com.yu.market.server.coupon.model.enums.CouponTaskSendTypeEnum;
import com.yu.market.server.coupon.model.enums.CouponTaskStatusEnum;
import com.yu.market.server.coupon.model.vo.CouponTaskPageQueryVO;
import com.yu.market.server.coupon.model.vo.CouponTaskQueryVO;
import com.yu.market.server.coupon.model.vo.CouponTemplateQueryVO;
import com.yu.market.server.coupon.mq.event.CouponTaskExecuteEvent;
import com.yu.market.server.coupon.mq.producer.CouponTaskActualExecuteProducer;
import com.yu.market.server.coupon.service.admin.ICouponTaskService;
import com.yu.market.server.coupon.service.admin.ICouponTemplateService;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author yu
 * @description 优惠券推送业务逻辑实现层
 * @date 2025-02-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponTaskServiceImpl implements ICouponTaskService {

	private final ICouponTemplateService couponTemplateService;
	private final CouponTaskMapper couponTaskMapper;
	private final RedissonService redissonService;
	private final CouponTaskActualExecuteProducer couponTaskActualExecuteProducer;

	/**
	 * 为什么这里拒绝策略使用直接丢弃任务？因为在发送任务时如果遇到发送数量为空，会重新进行统计
	 */
	private final ExecutorService executorService = new ThreadPoolExecutor(
			Runtime.getRuntime().availableProcessors(),
			Runtime.getRuntime().availableProcessors() << 1,
			60,
			TimeUnit.SECONDS,
			new SynchronousQueue<>(),
			new ThreadPoolExecutor.DiscardPolicy()
	);

	/**
	 * 商家创建优惠券推送任务
	 * 商家传入一个 Excel 表格，这个表格就是该优惠卷发放给哪些用户
	 *
	 * @param requestParam 请求参数
	 */
	@Transactional(rollbackFor = Exception.class)
	@Override
	public void createCouponTask(CouponTaskCreateDTO requestParam) {
		CouponTemplateQueryVO couponTemplate = couponTemplateService.findCouponTemplateById(requestParam.getCouponTemplateId());
		if (couponTemplate == null) {
			throw new ServiceException("优惠券模板不存在，请检查提交信息是否正确");
		}

		CouponTask couponTaskDO = BeanUtil.copyProperties(requestParam, CouponTask.class);
		couponTaskDO.setBatchId(new SnowFlakeUtil().nextId());
		couponTaskDO.setOperatorId(Long.parseLong(UserContext.getUserId()));
		couponTaskDO.setShopNumber(UserContext.getShopNumber());
		couponTaskDO.setStatus(Objects.equals(requestParam.getSendType(), CouponTaskSendTypeEnum.IMMEDIATE.getType())
				? CouponTaskStatusEnum.IN_PROGRESS.getStatus() : CouponTaskStatusEnum.PENDING.getStatus()
		);

		// 保存优惠券推送任务记录到数据库
		couponTaskMapper.insert(couponTaskDO);

		// 为什么需要统计行数？因为发送后需要比对所有优惠券是否都已发放到用户账号
		// 100 万数据大概需要 4 秒才能返回前端，如果加上验证将会时间更长，所以这里将最耗时的统计操作异步化
		JSONObject jsonObject = JSONUtil.createObj()
				.set("couponTaskId", couponTaskDO.getId())
				.set("fileAddress", requestParam.getFileAddress());
		executorService.execute(() -> refreshCouponTaskSendNum(jsonObject));

		RBlockingQueue<Object> blockingDeque = redissonService.getBlockingQueue("COUPON_TASK_SEND_NUM_DELAY_QUEUE");
		RDelayedQueue<Object> delayedQueue = redissonService.getDelayedQueue(blockingDeque);
		// 这里延迟时间设置 20 秒
		delayedQueue.offer(jsonObject, 20, TimeUnit.SECONDS);

		// 判断是否是立即发送任务
		boolean isImmediateSend = Objects.equals(requestParam.getSendType(), CouponTaskSendTypeEnum.IMMEDIATE.getType());

		// 如果不是立即发送，计算延迟时间 - 这里是通过 delayTime 是否为空来判断是否立即发送
		Long delayTime = isImmediateSend ? null : requestParam.getSendTime().getTime() - System.currentTimeMillis();

		// 构建优惠券任务执行事件
		CouponTaskExecuteEvent couponTaskExecuteEvent = CouponTaskExecuteEvent.builder()
				.couponTaskId(couponTaskDO.getId())
				.delayTime(delayTime)
				.build();

		// 发送消息到消息队列，执行优惠券推送业务
		couponTaskActualExecuteProducer.sendMessage(couponTaskExecuteEvent);
	}

	/**
	 * 分页查询商家优惠券推送任务
	 *
	 * @param requestParam 请求参数
	 * @return 商家优惠券推送任务分页数据
	 */
	@Override
	public IPage<CouponTaskPageQueryVO> pageQueryCouponTask(CouponTaskPageQueryDTO requestParam) {
		IPage<CouponTask> page = new Page<>(requestParam.getCurrent(), requestParam.getSize());
		LambdaQueryWrapper<CouponTask> queryWrapper = new LambdaQueryWrapper<CouponTask>()
				.eq(CouponTask::getShopNumber, UserContext.getShopNumber())
				.eq(StrUtil.isNotBlank(requestParam.getBatchId()), CouponTask::getBatchId, requestParam.getBatchId())
				.like(StrUtil.isNotBlank(requestParam.getTaskName()), CouponTask::getTaskName, requestParam.getTaskName())
				.eq(StrUtil.isNotBlank(requestParam.getCouponTemplateId()), CouponTask::getCouponTemplateId, requestParam.getCouponTemplateId())
				.eq(Objects.nonNull(requestParam.getStatus()), CouponTask::getStatus, requestParam.getStatus());

		IPage<CouponTask> selectPage = couponTaskMapper.selectPage(page, queryWrapper);

		return selectPage.convert(each -> BeanCopyUtil.copyProperties(each, CouponTaskPageQueryVO.class));
	}

	/**
	 * 查询优惠券推送任务详情
	 *
	 * @param taskId 推送任务 ID
	 * @return 优惠券推送任务详情
	 */
	@Override
	public CouponTaskQueryVO findCouponTaskById(String taskId) {
		CouponTask couponTask = couponTaskMapper.selectOne(new LambdaQueryWrapper<CouponTask>()
				.eq(CouponTask::getId, taskId));
		if (couponTask == null) {
			return null;
		}

		return BeanCopyUtil.copyProperties(couponTask, CouponTaskQueryVO.class);
	}

	/**
	 * 优惠券推送记录中发送行数
	 */
	private void refreshCouponTaskSendNum(JSONObject jsonObject) {
		// 通过 EasyExcel 监听器获取 Excel 中所有行数
		RowCountListener listener = new RowCountListener();
		EasyExcel.read(jsonObject.getStr("fileAddress"), listener).sheet().doRead();
		int totalRows = listener.getRowCount();

		// 刷新优惠券推送记录中发送行数
		CouponTask updateCouponTaskDO = CouponTask.builder()
				.id(jsonObject.getLong("couponTaskId"))
				.sendNum(totalRows)
				.build();

		couponTaskMapper.updateById(updateCouponTaskDO);
	}


	@PostConstruct
	public void init() {
		ExecutorService executorService = Executors.newSingleThreadExecutor(new CustomThreadFactory());
		executorService.execute(this::runQueueConsumer);

	}

	private static class CustomThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(@NonNull Runnable runnable) {
			Thread thread = new Thread(runnable);
			thread.setName("delay_coupon-task_send-num_consumer");
			thread.setDaemon(true);
			return thread;
		}
	}

	private void runQueueConsumer() {
		RBlockingQueue<Object> blockingQueue = redissonService.getBlockingQueue("COUPON_TASK_SEND_NUM_DELAY_QUEUE");
		while (true) {
			try {
				JSONObject delayJsonObject = (JSONObject) blockingQueue.take();
				CouponTask couponTaskDO = couponTaskMapper.selectById(delayJsonObject.getLong("couponTaskId"));
				if (couponTaskDO.getSendNum() == null) {
					this.refreshCouponTaskSendNum(delayJsonObject);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // 恢复中断状态
				break;
			} catch (Exception e) {
				// 记录异常并继续
				log.error("处理优惠券任务时出错: {}", e.getMessage());
			}
		}
	}
}
