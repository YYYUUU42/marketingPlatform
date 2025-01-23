package com.yu.market.common.utils;

/**
 * @author yu
 * @description 雪花算法生成分布式ID
 * @date 2025-01-23
 */
public class SnowFlakeUtil {

	/**
	 * 起始的时间戳: 2025-01-23 15:50:26
	 */
	private static final long START_TIMESTAMP = 1737618626436L;

	/**
	 * 每一部分占用的位数
	 */
	private static final long SEQUENCE_BITS = 12; // 序列号占用的位数
	private static final long MACHINE_BITS = 5;  // 机器标识占用的位数
	private static final long DATACENTER_BITS = 5; // 数据中心占用的位数

	/**
	 * 每一部分的最大值
	 */
	private static final long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BITS);
	private static final long MAX_MACHINE_NUM = ~(-1L << MACHINE_BITS);
	private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

	/**
	 * 每一部分向左的位移
	 */
	private static final long MACHINE_SHIFT = SEQUENCE_BITS;
	private static final long DATACENTER_SHIFT = SEQUENCE_BITS + MACHINE_BITS;
	private static final long TIMESTAMP_SHIFT = DATACENTER_SHIFT + DATACENTER_BITS;

	private final long datacenterId;  // 数据中心ID
	private final long machineId;     // 机器ID
	private long sequence = 0L;       // 序列号
	private long lastTimestamp = -1L; // 上一次生成ID的时间戳

	/**
	 * 默认数据中心ID
	 */
	private static final long DEFAULT_DATACENTER_ID = 0;

	/**
	 * 默认机器ID
	 */
	private static final long DEFAULT_MACHINE_ID = 0;

	/**
	 * 无参构造函数，使用默认的数据中心ID和机器ID
	 */
	public SnowFlakeUtil() {
		this.datacenterId = DEFAULT_DATACENTER_ID;
		this.machineId = DEFAULT_MACHINE_ID;
	}

	public SnowFlakeUtil(long datacenterId, long machineId) {
		if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
			throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
		}
		if (machineId > MAX_MACHINE_NUM || machineId < 0) {
			throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
		}
		this.datacenterId = datacenterId;
		this.machineId = machineId;
	}

	/**
	 * 生成下一个唯一ID
	 *
	 * @return 唯一ID
	 */
	public synchronized long nextId() {
		long currentTimestamp = getCurrentTimestamp();

		if (currentTimestamp < lastTimestamp) {
			throw new RuntimeException("Clock moved backwards. Refusing to generate ID");
		}

		if (currentTimestamp == lastTimestamp) {
			// 相同毫秒内，序列号自增
			sequence = (sequence + 1) & MAX_SEQUENCE;
			if (sequence == 0L) {
				// 序列号达到最大值，等待下一毫秒
				currentTimestamp = waitForNextMillis();
			}
		} else {
			// 不同毫秒内，序列号置为0
			sequence = 0L;
		}

		lastTimestamp = currentTimestamp;

		// 生成唯一ID
		return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT) // 时间戳部分
				| (datacenterId << DATACENTER_SHIFT)                    // 数据中心部分
				| (machineId << MACHINE_SHIFT)                          // 机器标识部分
				| sequence;                                             // 序列号部分
	}

	/**
	 * 等待下一毫秒
	 *
	 * @return 当前时间戳
	 */
	private long waitForNextMillis() {
		long timestamp = getCurrentTimestamp();
		while (timestamp <= lastTimestamp) {
			timestamp = getCurrentTimestamp();
		}
		return timestamp;
	}

	/**
	 * 获取当前时间戳
	 *
	 * @return 当前时间戳
	 */
	private long getCurrentTimestamp() {
		return System.currentTimeMillis();
	}

}
