package com.yu.market.server.coupon.service.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yu.market.server.coupon.model.dto.CouponTemplateNumberDTO;
import com.yu.market.server.coupon.model.dto.CouponTemplatePageQueryDTO;
import com.yu.market.server.coupon.model.dto.CouponTemplateSaveDTO;
import com.yu.market.server.coupon.model.vo.CouponTemplateQueryVO;

/**
 * @author yu
 * @description 优惠券模板业务逻辑层
 * @date 2025-02-14
 */
public interface ICouponTemplateService {

	/**
	 * 创建商家优惠券模板
	 *
	 * @param requestParam 请求参数
	 */
	void createCouponTemplate(CouponTemplateSaveDTO requestParam);

	/**
	 * 分页查询商家优惠券模板
	 *
	 * @param requestParam 请求参数
	 * @return 商家优惠券模板分页数据
	 */
	IPage<CouponTemplateQueryVO> pageQueryCouponTemplate(CouponTemplatePageQueryDTO requestParam);

	/**
	 * 查询优惠券模板详情
	 * 后管接口并不存在并发，直接查询数据库即可
	 *
	 * @param couponTemplateId 优惠券模板 ID
	 * @return 优惠券模板详情
	 */
	CouponTemplateQueryVO findCouponTemplateById(String couponTemplateId);

	/**
	 * 结束优惠券模板
	 *
	 * @param couponTemplateId 优惠券模板 ID
	 */
	void terminateCouponTemplate(String couponTemplateId);

	/**
	 * 增加优惠券模板发行量
	 *
	 * @param requestParam 请求参数
	 */
	void increaseNumberCouponTemplate(CouponTemplateNumberDTO requestParam);
}
