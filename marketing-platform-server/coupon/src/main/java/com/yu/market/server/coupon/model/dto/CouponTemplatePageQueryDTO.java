package com.yu.market.server.coupon.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yu
 * @description 优惠券模板分页查询接口请求参数实体
 * @date 2025-02-15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CouponTemplatePageQueryDTO extends Page {

    /**
     * 优惠券名称
     */
    private String name;

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
}
