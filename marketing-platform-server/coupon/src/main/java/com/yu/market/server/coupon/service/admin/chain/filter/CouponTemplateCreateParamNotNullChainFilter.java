package com.yu.market.server.coupon.service.admin.chain.filter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.server.coupon.model.dto.CouponTemplateSaveDTO;
import com.yu.market.server.coupon.service.admin.chain.MerchantAdminAbstractChainHandler;
import org.springframework.stereotype.Component;

import static com.yu.market.server.coupon.model.enums.ChainBizMarkEnum.MERCHANT_ADMIN_CREATE_COUPON_TEMPLATE_KEY;


/**
 * @author yu
 * @description 验证优惠券创建接口参数是否正确责任链｜验证必填参数是否为空或空的字符串
 * @date 2025-02-14
 */
@Component
public class CouponTemplateCreateParamNotNullChainFilter extends MerchantAdminAbstractChainHandler<CouponTemplateSaveDTO> {

    @Override
    public void handle(CouponTemplateSaveDTO requestParam) {
        if (StrUtil.isEmpty(requestParam.getName())) {
            throw new ServiceException("优惠券名称不能为空");
        }

        if (ObjectUtil.isEmpty(requestParam.getSource())) {
            throw new ServiceException("优惠券来源不能为空");
        }

        if (ObjectUtil.isEmpty(requestParam.getTarget())) {
            throw new ServiceException("优惠对象不能为空");
        }

        if (ObjectUtil.isEmpty(requestParam.getType())) {
            throw new ServiceException("优惠类型不能为空");
        }

        if (ObjectUtil.isEmpty(requestParam.getValidStartTime())) {
            throw new ServiceException("有效期开始时间不能为空");
        }

        if (ObjectUtil.isEmpty(requestParam.getValidEndTime())) {
            throw new ServiceException("有效期结束时间不能为空");
        }

        if (ObjectUtil.isEmpty(requestParam.getStock())) {
            throw new ServiceException("库存不能为空");
        }

        if (StrUtil.isEmpty(requestParam.getReceiveRule())) {
            throw new ServiceException("领取规则不能为空");
        }

        if (StrUtil.isEmpty(requestParam.getConsumeRule())) {
            throw new ServiceException("消耗规则不能为空");
        }
    }

    @Override
    public String mark() {
        return MERCHANT_ADMIN_CREATE_COUPON_TEMPLATE_KEY.name();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
