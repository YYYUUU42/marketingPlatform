package com.yu.market.server.activity.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yu
 * @description 活动状态
 * @date 2025-01-19
 */
@Getter
@AllArgsConstructor
public enum ActivityStateEnum {

    create("create", "创建"),
    open("open", "开启"),
    close("close", "关闭"),
    ;

    private final String code;
    private final String desc;

}
