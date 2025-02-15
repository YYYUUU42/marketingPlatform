package com.yu.market.infrastructure.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.market.infrastructure.coupon.pojo.CouponTemplate;
import org.apache.ibatis.annotations.Param;

/**
* @description 针对表【t_coupon_template(优惠券模板表)】的数据库操作Mapper
* @Entity com.yu.market.infrastructure.coupon.pojo.CouponTemplate
*/
public interface CouponTemplateMapper extends BaseMapper<CouponTemplate> {

	/**
	 * 增加优惠券模板发行量
	 *
	 * @param shopNumber       店铺编号
	 * @param couponTemplateId 优惠券模板 ID
	 * @param number           增加发行数量
	 */
	int increaseNumberCouponTemplate(@Param("shopNumber") Long shopNumber, @Param("couponTemplateId") String couponTemplateId, @Param("number") Integer number);
}




