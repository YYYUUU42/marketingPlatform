package com.yu.market.server.coupon.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author yu
 * @description 优惠券模板详情查询接口返回参数实体
 * @date 2025-02-15
 */
@Data
public class CouponTemplateQueryVO {


    /**
     * 优惠券id
     */
    private String id;

    /**
     * 优惠券名称
     */
    private String name;

    /**
     * 店铺编号
     */
    private String shopNumber;

    /**
     * 优惠券来源 0：店铺券 1：平台券
     */
    private Integer source;

    /**
     * 优惠对象 0：商品专属 1：全店通用
     */
    private Integer target;

    /**
     * 优惠商品编码
     */
    private String goods;

    /**
     * 优惠类型 0：立减券 1：满减券 2：折扣券
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
     * 领取规则
     */
    private String receiveRule;

    /**
     * 消耗规则
     */
    private String consumeRule;

    /**
     * 优惠券状态 0：生效中 1：已结束
     */
    private Integer status;
}
