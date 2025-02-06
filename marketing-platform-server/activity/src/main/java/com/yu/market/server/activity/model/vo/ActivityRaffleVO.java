package com.yu.market.server.activity.model.vo;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityRaffleVO implements Serializable {

	/**
	 * 奖品ID
	 */
	private Integer awardId;

	/**
	 * 奖品标题
	 */
	private String awardTitle;

	/**
	 * 排序编号 - 策略奖品配置的奖品顺序编号
	 */
	private Integer awardIndex;

}