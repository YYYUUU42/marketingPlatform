package com.yu.market.common.contants;

public class CouponRocketMQConstant {

	/**
	 * @author yu
	 * @description 商家后管优惠券 RocketMQ 常量类
	 * @date 2025-02-15
	 */
	public static class MerchantAdminRocketMQConstant {

		/**
		 * 优惠券推送任务定时执行 Topic Key
		 */
		public static final String TEMPLATE_TASK_DELAY_TOPIC_KEY = "one-coupon_merchant-admin-service_coupon-task-delay_topic${unique-name:}";

		/**
		 * 优惠券推送任务定时执行-变更记录发送状态消费者组 Key
		 */
		public static final String TEMPLATE_TASK_DELAY_STATUS_CG_KEY = "one-coupon_merchant-admin-service_coupon-task-delay-status_cg${unique-name:}";

		/**
		 * 优惠券模板推送定时执行 Topic Key
		 */
		public static final String TEMPLATE_TEMPLATE_DELAY_TOPIC_KEY = "one-coupon_merchant-admin-service_coupon-template-delay_topic${unique-name:}";

		/**
		 * 优惠券模板推送定时执行-变更记录状态消费者组 Key
		 */
		public static final String TEMPLATE_TEMPLATE_DELAY_STATUS_CG_KEY = "one-coupon_merchant-admin-service_coupon-template-delay-status_cg${unique-name:}";

		/**
		 * 优惠券模板推送执行 Topic Key
		 * 负责扫描优惠券 Excel 并将里面的记录进行推送
		 */
		public static final String TEMPLATE_TASK_EXECUTE_TOPIC_KEY = "one-coupon_distribution-service_coupon-task-execute_topic${unique-name:}";
	}

	/**
	 * @author yu
	 * @description 分发优惠券服务 RocketMQ 常量类
	 * @date 2025-02-15
	 */
	public static class DistributionRocketMQConstant{
		/**
		 * 优惠券模板推送执行 Topic Key
		 * 负责扫描优惠券 Excel 并将里面的记录进行推送
		 */
		public static final String TEMPLATE_TASK_EXECUTE_TOPIC_KEY = "one-coupon_distribution-service_coupon-task-execute_topic${unique-name:}";

		/**
		 * 优惠券模板推送执行-执行消费者组 Key
		 */
		public static final String TEMPLATE_TASK_EXECUTE_CG_KEY = "one-coupon_distribution-service_coupon-task-execute_cg${unique-name:}";

		/**
		 * 优惠券模板推送执行 Topic Key
		 * 负责执行将优惠券发放给具体用户逻辑
		 */
		public static final String TEMPLATE_EXECUTE_DISTRIBUTION_TOPIC_KEY = "one-coupon_distribution-service_coupon-execute-distribution_topic${unique-name:}";

		/**
		 * 优惠券模板推送执行-执行消费者组 Key
		 */
		public static final String TEMPLATE_EXECUTE_DISTRIBUTION_CG_KEY = "one-coupon_distribution-service_coupon-execute-distribution_cg${unique-name:}";

		/**
		 * 优惠券模板推送用户通知-执行消费者组 Key
		 */
		public static final String TEMPLATE_EXECUTE_SEND_MESSAGE_CG_KEY = "one-coupon_distribution-service_coupon-execute-send-message_cg${unique-name:}";
	}
}
