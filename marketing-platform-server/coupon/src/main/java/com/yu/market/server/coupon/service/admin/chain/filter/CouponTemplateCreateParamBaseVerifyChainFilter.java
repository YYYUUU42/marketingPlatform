package com.yu.market.server.coupon.service.admin.chain.filter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.server.coupon.model.dto.CouponTemplateSaveDTO;
import com.yu.market.server.coupon.model.enums.DiscountTargetEnum;
import com.yu.market.server.coupon.model.enums.DiscountTypeEnum;
import com.yu.market.server.coupon.service.admin.chain.MerchantAdminAbstractChainHandler;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

import static com.yu.market.server.coupon.model.enums.ChainBizMarkEnum.MERCHANT_ADMIN_CREATE_COUPON_TEMPLATE_KEY;


/**
 * 验证优惠券创建接口参数是否正确责任链｜验证参数基本数据关系是否正确
 * <p>
 * 作者：马丁
 * 加项目群：早加入就是优势！500人内部项目群，分享的知识总有你需要的 <a href="https://t.zsxq.com/cw7b9" />
 * 开发时间：2024-07-09
 */
@Component
public class CouponTemplateCreateParamBaseVerifyChainFilter extends MerchantAdminAbstractChainHandler<CouponTemplateSaveDTO> {

	private final int maxStock = 20000000;

	/**
	 * 处理请求关系
	 * @param requestParam 请求参数
	 */
	@Override
	public void handle(CouponTemplateSaveDTO requestParam) {
		boolean targetAnyMatch = Arrays.stream(DiscountTargetEnum.values())
				.anyMatch(enumConstant -> enumConstant.getType() == requestParam.getTarget());
		if (!targetAnyMatch) {
			// 此处已经基本能判断数据请求属于恶意攻击，可以上报风控中心进行封禁账号
			throw new ServiceException("优惠对象值不存在");
		}
		if (ObjectUtil.equal(requestParam.getTarget(), DiscountTargetEnum.ALL_STORE_GENERAL)
				&& StrUtil.isNotEmpty(requestParam.getGoods())) {
			throw new ServiceException("优惠券全店通用不可设置指定商品");
		}
		if (ObjectUtil.equal(requestParam.getTarget(), DiscountTargetEnum.PRODUCT_SPECIFIC)
				&& StrUtil.isEmpty(requestParam.getGoods())) {
			throw new ServiceException("优惠券商品专属未设置指定商品");
		}

		boolean typeAnyMatch = Arrays.stream(DiscountTypeEnum.values())
				.anyMatch(enumConstant -> enumConstant.getType() == requestParam.getType());
		if (!typeAnyMatch) {
			// 此处已经基本能判断数据请求属于恶意攻击，可以上报风控中心进行封禁账号
			throw new ServiceException("优惠类型不存在");
		}

		Date now = new Date();
		if (requestParam.getValidStartTime().before(now)) {
			// throw new ServiceException("有效期开始时间不能早于当前时间");
		}

		if (requestParam.getStock() <= 0 || requestParam.getStock() > maxStock) {
			throw new ServiceException("库存数量设置异常");
		}

		if (isValidJSON(requestParam.getReceiveRule())) {
			throw new ServiceException("领取规则格式错误");
		}
		if (isValidJSON(requestParam.getConsumeRule())) {
			throw new ServiceException("消耗规则格式错误");
		}
	}

	@Override
	public String mark() {
		return MERCHANT_ADMIN_CREATE_COUPON_TEMPLATE_KEY.name();
	}

	@Override
	public int getOrder() {
		return 10;
	}

	/**
	 * 判断 JSON 字符串是否有效
	 *
	 * @param jsonString 待验证的 JSON 字符串
	 * @return 如果 JSON 有效则返回 true，否则返回 false
	 */
	public static boolean isValidJSON(String jsonString) {
		try {
			// 尝试将字符串解析为 JSON 对象
			JSONUtil.parseObj(jsonString);
			return true;
		} catch (Exception e) {
			// 如果解析过程中出现异常，则认为 JSON 无效
			return false;
		}
	}
}
