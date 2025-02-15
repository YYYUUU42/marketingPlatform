package com.yu.market.common.contants;

/**
 * @author yu
 * @description 优惠券 Redis 常量类
 * @date 2025-02-15
 */
public final class CouponRedisKey {
	/**
	 * 优惠券模板缓存 Key
	 */
	public static final String COUPON_TEMPLATE_KEY = "one-coupon_engine:template:%s";

	/**
	 * 优惠券模板推送执行进度 Key
	 */
	public static final String TEMPLATE_TASK_EXECUTE_PROGRESS_KEY = "one-coupon_distribution:template-task-execute-progress:%s";

	/**
	 * 批量保存领取用户券用户 Key
	 */
	public static final String TEMPLATE_TASK_EXECUTE_BATCH_USER_KEY = "one-coupon_distribution:template-task-execute-batch-user:%s";

	/**
	 * @author yu
	 * @description 分发优惠券服务 Redis 缓存常量类
	 * @date 2025-02-15
	 */
	public static class DistributionRedisConstant {

		/**
		 * 优惠券模板推送执行进度 Key
		 */
		public static final String TEMPLATE_TASK_EXECUTE_PROGRESS_KEY = "one-coupon_distribution:template-task-execute-progress:%s";

		/**
		 * 批量保存领取用户券用户 Key
		 */
		public static final String TEMPLATE_TASK_EXECUTE_BATCH_USER_KEY = "one-coupon_distribution:template-task-execute-batch-user:%s";
	}

	/**
	 * @author yu
	 * @description 优惠券模板 Redis 缓存常量类
	 * @date 2025-02-15
	 */
	public static class EngineRedisConstant {

		/**
		 * 优惠券模板缓存 Key
		 */
		public static final String COUPON_TEMPLATE_KEY = "one-coupon_engine:template:%s";

		/**
		 * 用户已领取优惠券列表模板 Key
		 */
		public static final String USER_COUPON_TEMPLATE_LIST_KEY = "one-coupon_engine:user-template-list:";
	}

}
