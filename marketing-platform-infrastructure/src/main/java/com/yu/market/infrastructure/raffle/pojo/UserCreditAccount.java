package com.yu.market.infrastructure.raffle.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 用户积分账户
 * @TableName user_credit_account
 */
@TableName(value ="user_credit_account")
@Data
public class UserCreditAccount implements Serializable {
    /**
     * 自增ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 总积分，显示总账户值，记得一个人获得的总积分
     */
    private BigDecimal totalAmount;

    /**
     * 可用积分，每次扣减的值
     */
    private BigDecimal availableAmount;

    /**
     * 账户状态【open - 可用，close - 冻结】
     */
    private String accountStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}