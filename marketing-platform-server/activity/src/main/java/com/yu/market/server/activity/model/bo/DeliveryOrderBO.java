package com.yu.market.server.activity.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yu
 * @description 出货单实体对象
 * @date 2025-02-10
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryOrderBO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 业务仿重ID - 外部透传。返利、行为等唯一标识
     */
    private String outBusinessNo;

}