package com.yu.market.common.utils;

/**
 * 用户优惠券执行 LUA 脚本返回数据｜通过位移形式提高性能，是个小优化
 * 因为预计每 5000 条记录保存次数据库，2^12 能表示 4096，所以这里采用了 2^13
 */
public class StockDecrementReturnCombinedUtil {

    /**
     * 2^13 > 5000, 所以用 13 位来表示第二个字段
     */
    private static final int SECOND_FIELD_BITS = 13;

    /**
     * 将两个字段组合成一个int
     */
    public static int combineFields(boolean decrementFlag, int userRecord) {
        return (decrementFlag ? 1 : 0) << SECOND_FIELD_BITS | userRecord;
    }

    /**
     * 从组合的int中提取第一个字段（0或1）
     */
    public static boolean extractFirstField(long combined) {
        return (combined >> SECOND_FIELD_BITS) != 0;
    }

    /**
     * 从组合的int中提取第二个字段（1到5000之间的数字）
     */
    public static int extractSecondField(int combined) {
        return combined & ((1 << SECOND_FIELD_BITS) - 1);
    }
}