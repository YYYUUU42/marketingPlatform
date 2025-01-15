package com.yu.market.server.raffle.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yu
 * @description 规则限定枚举量
 * @date 2025-01-15
 */
@Getter
@AllArgsConstructor
public enum RuleLimitType {

    EQUAL(1, "等于"),
    GT(2, "大于"),
    LT(3, "小于"),
    GE(4, "大于等于"),
    LE(5, "小于等于"),
    ENUM(6, "枚举"),
    ;

    private final Integer code;
    private final String info;

}