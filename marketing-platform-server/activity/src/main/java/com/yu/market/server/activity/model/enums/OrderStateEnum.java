package com.yu.market.server.activity.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yu
 * @description 订单状态枚
 * @date 2025-01-19
 */
@Getter
@AllArgsConstructor
public enum OrderStateEnum {

    create("create", "创建"),
    completed("completed", "完成");

    private final String code;
    private final String desc;

}
