package com.yu.market.server.coupon.service.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yu.market.server.coupon.model.dto.CouponTaskCreateDTO;
import com.yu.market.server.coupon.model.dto.CouponTaskPageQueryDTO;
import com.yu.market.server.coupon.model.vo.CouponTaskPageQueryVO;
import com.yu.market.server.coupon.model.vo.CouponTaskQueryVO;

/**
 * @author yu
 * @description 优惠券推送业务逻辑层
 * @date 2025-02-15
 */
public interface ICouponTaskService {

    /**
     * 商家创建优惠券推送任务
     *
     * @param requestParam 请求参数
     */
    void createCouponTask(CouponTaskCreateDTO requestParam);

    /**
     * 分页查询商家优惠券推送任务
     *
     * @param requestParam 请求参数
     * @return 商家优惠券推送任务分页数据
     */
    IPage<CouponTaskPageQueryVO> pageQueryCouponTask(CouponTaskPageQueryDTO requestParam);

    /**
     * 查询优惠券推送任务详情
     *
     * @param taskId 推送任务 ID
     * @return 优惠券推送任务详情
     */
    CouponTaskQueryVO findCouponTaskById(String taskId);
}