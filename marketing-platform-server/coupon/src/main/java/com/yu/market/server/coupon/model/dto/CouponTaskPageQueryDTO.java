package com.yu.market.server.coupon.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yu
 * @description 优惠券推送任务分页查询接口请求参数实体
 * @date 2025-02-15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CouponTaskPageQueryDTO extends Page {

    /**
     * 批次id
     */
    private String batchId;

    /**
     * 优惠券批次任务名称
     */
    private String taskName;

    /**
     * 优惠券模板id
     */
    private String couponTemplateId;

    /**
     * 状态 0：待执行 1：执行中 2：执行失败 3：执行成功 4：取消
     */
    private Integer status;

}
