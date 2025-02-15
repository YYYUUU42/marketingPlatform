package com.yu.market.infrastructure.coupon.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

/**
 * 
 * @TableName t_coupon_task_fail
 */
@TableName(value ="t_coupon_task_fail")
@Data
@Builder
public class CouponTaskFail implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 批次ID
     */
    private Long batchId;

    /**
     * 失败内容
     */
    private String jsonObject;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}