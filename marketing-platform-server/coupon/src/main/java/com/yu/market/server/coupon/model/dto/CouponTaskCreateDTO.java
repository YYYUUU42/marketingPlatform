package com.yu.market.server.coupon.model.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author yu
 * @description 优惠券推送任务创建请求参数实体
 * @date 2025-02-15
 */
@Data
public class CouponTaskCreateDTO {

    /**
     * 优惠券批次任务名称
     */
    private String taskName;

    /**
     * 文件地址 - /.../任务推送Excel.xlsx
     */
    private String fileAddress;

    /**
     * 通知方式，可组合使用 0：站内信 1：弹框推送 2：邮箱 3：短信
     */
    private String notifyType;

    /**
     * 优惠券模板id
     */
    private String couponTemplateId;

    /**
     * 发送类型 0：立即发送 1：定时发送
     */
    private Integer sendType;

    /**
     * 发送时间
     */
    private Date sendTime;

}