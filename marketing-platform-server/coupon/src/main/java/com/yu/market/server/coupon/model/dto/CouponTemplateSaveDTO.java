package com.yu.market.server.coupon.model.dto;

import lombok.Data;

import java.util.Date;


/**
 * @author yu
 * @description 优惠券模板新增参数
 * @date 2025-02-14
 */
@Data
public class CouponTemplateSaveDTO {

    /**
     * 优惠券名称 - 用户下单满10减3特大优惠
     */
    private String name;

    /**
     * 优惠券来源 - 0：店铺券 1：平台券
     */
    private Integer source;

    /**
     * 优惠对象 - 0：商品专属 1：全店通用
     */
    private Integer target;

    /**
     * 优惠商品编码
     */
    private String goods;

    /**
     * 优惠类型 - 0：立减券 1：满减券 2：折扣券
     */
    private Integer type;

    /**
     * 有效期开始时间
     */
    private Date validStartTime;

    /**
     * 有效期结束时间
     */
    private Date validEndTime;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 领取规则 - {"limitPerPerson":1,"usageInstructions":"3"}
     */
    private String receiveRule;

    /**
     * 消耗规则 -
     */
    private String consumeRule;

}
