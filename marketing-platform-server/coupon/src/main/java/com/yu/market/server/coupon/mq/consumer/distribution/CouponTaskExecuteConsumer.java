package com.yu.market.server.coupon.mq.consumer.distribution;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yu.market.common.idempotent.anno.NoMQDuplicateConsume;
import com.yu.market.infrastructure.coupon.mapper.CouponTaskFailMapper;
import com.yu.market.infrastructure.coupon.mapper.CouponTaskMapper;
import com.yu.market.infrastructure.coupon.mapper.CouponTemplateMapper;
import com.yu.market.infrastructure.coupon.pojo.CouponTask;
import com.yu.market.infrastructure.coupon.pojo.CouponTemplate;
import com.yu.market.server.coupon.model.enums.CouponTaskStatusEnum;
import com.yu.market.server.coupon.model.enums.CouponTemplateStatusEnum;
import com.yu.market.server.coupon.mq.base.MessageWrapper;
import com.yu.market.server.coupon.mq.event.CouponTaskExecuteEvent;
import com.yu.market.server.coupon.mq.producer.CouponExecuteDistributionProducer;
import com.yu.market.server.coupon.service.distribution.excel.CouponTaskExcelObject;
import com.yu.market.server.coupon.service.distribution.excel.ReadExcelDistributionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import static com.yu.market.common.contants.CouponRocketMQConstant.DistributionRocketMQConstant.TEMPLATE_TASK_EXECUTE_CG_KEY;
import static com.yu.market.common.contants.CouponRocketMQConstant.MerchantAdminRocketMQConstant.TEMPLATE_TASK_EXECUTE_TOPIC_KEY;

/**
 * @author yu
 * @description 优惠券推送定时执行-真实执行消费者
 * @date 2025-02-15
 */
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
		topic = TEMPLATE_TASK_EXECUTE_TOPIC_KEY,
		consumerGroup = TEMPLATE_TASK_EXECUTE_CG_KEY
)
@Slf4j(topic = "CouponTaskExecuteConsumer")
public class CouponTaskExecuteConsumer implements RocketMQListener<MessageWrapper<CouponTaskExecuteEvent>> {

	private final CouponTaskMapper couponTaskMapper;
	private final CouponTemplateMapper couponTemplateMapper;
	private final CouponTaskFailMapper couponTaskFailMapper;

	private final StringRedisTemplate stringRedisTemplate;
	private final CouponExecuteDistributionProducer couponExecuteDistributionProducer;

	@NoMQDuplicateConsume(
			keyPrefix = "coupon_task_execute:idempotent:",
			key = "#messageWrapper.message.couponTaskId",
			keyTimeout = 120
	)
	@Override
	public void onMessage(MessageWrapper<CouponTaskExecuteEvent> messageWrapper) {
		// 开头打印日志，平常可 Debug 看任务参数，线上可报平安（比如消息是否消费，重新投递时获取参数等）
		log.info("[消费者] 优惠券推送任务正式执行 - 执行消费逻辑，消息体：{}", JSON.toJSONString(messageWrapper));

		Long couponTaskId = messageWrapper.getMessage().getCouponTaskId();
		CouponTask couponTask = couponTaskMapper.selectById(couponTaskId);

		// 判断优惠券模板发送状态是否为执行中，如果不是有可能是被取消状态
		if (ObjectUtil.notEqual(couponTask.getStatus(), CouponTaskStatusEnum.IN_PROGRESS.getStatus())) {
			log.warn("[消费者] 优惠券推送任务正式执行 - 推送任务记录状态异常：{}，已终止推送", couponTask.getStatus());
			return;
		}

		// 判断优惠券状态是否正确
		CouponTemplate couponTemplate = couponTemplateMapper.selectOne(new LambdaQueryWrapper<CouponTemplate>()
						.eq(CouponTemplate::getId, couponTask.getCouponTemplateId())
						.eq(CouponTemplate::getShopNumber, couponTask.getShopNumber()));
		if (couponTemplate == null){
			log.warn("[消费者] 优惠券推送任务正式执行 - 优惠券未找到：{}，已终止推送", couponTask.getStatus());
			return;
		}


		Integer status = couponTemplate.getStatus();
		if (ObjectUtil.notEqual(status, CouponTemplateStatusEnum.ACTIVE.getStatus())) {
			log.error("[消费者] 优惠券推送任务正式执行 - 优惠券ID：{}，优惠券模板状态：{}", couponTask.getCouponTemplateId(), status);
			return;
		}

		// 正式开始执行优惠券推送任务
		ReadExcelDistributionListener readExcelDistributionListener = new ReadExcelDistributionListener(
				couponTask,
				couponTemplate,
				couponTaskFailMapper,
				stringRedisTemplate,
				couponExecuteDistributionProducer
		);

		EasyExcel.read(couponTask.getFileAddress(), CouponTaskExcelObject.class, readExcelDistributionListener)
				.sheet()
				.doRead();
	}
}
