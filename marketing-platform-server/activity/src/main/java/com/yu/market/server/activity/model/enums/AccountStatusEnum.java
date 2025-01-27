package com.yu.market.server.activity.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yu
 * @description 账户状态枚举
 * @date 2025-01-27
 */
@Getter
@AllArgsConstructor
public enum AccountStatusEnum {

    open("open", "开启"),
    close("close", "冻结"),
    ;

    private final String code;
    private final String desc;

}
