package com.yu.market.server.coupon.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author yu
 * @description 优惠券优惠类型
 * @date 2025-02-14
 */
@Getter
@RequiredArgsConstructor
public enum DiscountTypeEnum {

    /**
     * 立减券
     */
    FIXED_DISCOUNT(0, "立减券"),

    /**
     * 满减券
     */
    THRESHOLD_DISCOUNT(1, "满减券"),

    /**
     * 折扣券
     */
    DISCOUNT_COUPON(2, "折扣券");

    private final int type;

    private final String value;

    /**
     * 根据 type 找到对应的 value
     *
     * @param type 要查找的类型代码
     * @return 对应的描述值，如果没有找到抛异常
     */
    public static String findValueByType(int type) {
        for (DiscountTypeEnum target : DiscountTypeEnum.values()) {
            if (target.getType() == type) {
                return target.getValue();
            }
        }
        throw new IllegalArgumentException();
    }
}
