package com.yu.market.server.coupon.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author yu
 * @description 优惠券推送任务查询接口返回参数实体
 * @date 2025-02-15
 */
@Data
public class CouponTaskQueryVO {

    /**
     * 批次id
     */
    private String batchId;

    /**
     * 优惠券批次任务名称
     */
    private String taskName;

    /**
     * 发放优惠券数量
     */
    private Integer sendNum;

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

    /**
     * 状态 0：待执行 1：执行中 2：执行失败 3：执行成功 4：取消
     */
    private Integer status;

    /**
     * 完成时间
     */
    private Date completionTime;

    /**
     * 操作人
     */
    private Long operatorId;
}
