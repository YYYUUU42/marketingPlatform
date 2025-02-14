package com.yu.market.infrastructure.raffle.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 规则表-树节点连线
 * @TableName rule_tree_node_line
 */
@TableName(value ="rule_tree_node_line")
@Data
public class RuleTreeNodeLine implements Serializable {
    /**
     * 自增ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 规则树ID
     */
    private String treeId;

    /**
     * 规则Key节点 From
     */
    private String ruleNodeFrom;

    /**
     * 规则Key节点 To
     */
    private String ruleNodeTo;

    /**
     * 限定类型；1:=;2:>;3:<;4:>=;5<=;6:enum[枚举范围];
     */
    private String ruleLimitType;

    /**
     * 限定值（到下个节点）
     */
    private String ruleLimitValue;

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