package com.yu.market.server.coupon.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 消息发送标识转换枚举
 * 开发时间：2024-07-16
 */
@Getter
@RequiredArgsConstructor
public enum SendMessageMarkCovertEnum {

    /**
     * 站内信
     */
    SITE(0),

    /**
     * 应用推送
     */
    APPLICATION(1),

    /**
     * 邮箱
     */
    EMAIL(2),

    /**
     * 短信
     */
    SMS(3),

    /**
     * 微信
     */
    WECHAT(4);

    private final int type;

    /**
     * 根据 type 找到对应的枚举实例
     *
     * @param type 要查找的类型
     * @return 对应的枚举实例
     */
    public static String fromType(int type) {
        for (SendMessageMarkCovertEnum method : SendMessageMarkCovertEnum.values()) {
            if (method.getType() == type) {
                return method.name();
            }
        }
        throw new IllegalArgumentException("Invalid type: " + type);
    }
}