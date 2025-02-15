package com.yu.market.server.coupon.model.dto;

import lombok.Data;

/**
 * 优惠券模板增加发行量请求参数实体
 * 开发时间：2024-07-29
 */
@Data
public class CouponTemplateNumberDTO {

    /**
     * 优惠券模板id
     */
    private String couponTemplateId;

    /**
     * 增加发行数量
     */
    private Integer number;
}