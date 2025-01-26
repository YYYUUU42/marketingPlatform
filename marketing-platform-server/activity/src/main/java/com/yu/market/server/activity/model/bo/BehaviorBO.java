package com.yu.market.server.activity.model.bo;

import com.yu.market.server.activity.model.enums.BehaviorTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yu
 * @description 行为实体对象
 * @date 2025-01-26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorBO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 行为类型；sign 签到、pay 支付
     */
    private BehaviorTypeEnum behaviorTypeEnum;

    /**
     * 业务ID；签到则是日期字符串，支付则是外部的业务ID
     */
    private String outBusinessNo;

}
