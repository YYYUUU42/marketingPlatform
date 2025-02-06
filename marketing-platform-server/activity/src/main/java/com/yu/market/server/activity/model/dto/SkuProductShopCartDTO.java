package com.yu.market.server.activity.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yu
 * @description 商品购物车请求对象
 * @date 2025-02-06
 */
@Data
public class SkuProductShopCartDTO implements Serializable {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * sku 商品
     */
    private Long sku;

}