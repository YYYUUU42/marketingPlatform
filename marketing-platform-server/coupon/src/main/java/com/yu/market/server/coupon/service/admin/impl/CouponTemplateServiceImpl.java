package com.yu.market.server.coupon.service.admin.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.yu.market.common.contants.CouponRedisKey;
import com.yu.market.common.exception.ServiceException;
import com.yu.market.common.redis.RedissonService;
import com.yu.market.common.utils.BeanCopyUtil;
import com.yu.market.infrastructure.coupon.mapper.CouponTemplateMapper;
import com.yu.market.infrastructure.coupon.pojo.CouponTemplate;
import com.yu.market.server.coupon.context.UserContext;
import com.yu.market.server.coupon.model.dto.CouponTemplateNumberDTO;
import com.yu.market.server.coupon.model.dto.CouponTemplatePageQueryDTO;
import com.yu.market.server.coupon.model.dto.CouponTemplateSaveDTO;
import com.yu.market.server.coupon.model.enums.CouponTemplateStatusEnum;
import com.yu.market.server.coupon.model.vo.CouponTemplateQueryVO;
import com.yu.market.server.coupon.mq.event.CouponTemplateDelayEvent;
import com.yu.market.server.coupon.mq.producer.CouponTemplateDelayExecuteStatusProducer;
import com.yu.market.server.coupon.service.admin.ICouponTemplateService;
import com.yu.market.server.coupon.service.admin.chain.MerchantAdminChainContext;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.yu.market.server.coupon.model.enums.ChainBizMarkEnum.MERCHANT_ADMIN_CREATE_COUPON_TEMPLATE_KEY;

/**
 * @author yu
 * @description 优惠券模板业务逻辑实现层
 * @date 2025-02-14
 */
@Service
@RequiredArgsConstructor
public class CouponTemplateServiceImpl implements ICouponTemplateService {

	private final CouponTemplateMapper couponTemplateMapper;
	private final MerchantAdminChainContext<CouponTemplateSaveDTO> merchantAdminChainContext;
	private final RedissonService redissonService;
	private final RBloomFilter<String> couponTemplateQueryBloomFilter;
	private final CouponTemplateDelayExecuteStatusProducer couponTemplateDelayExecuteStatusProducer;

	/**
	 * 创建商家优惠券模板
	 *
	 * @param requestParam 请求参数
	 */
	@Override
	public void createCouponTemplate(CouponTemplateSaveDTO requestParam) {

		// 通过责任链验证请求参数是否正确
		merchantAdminChainContext.handler(MERCHANT_ADMIN_CREATE_COUPON_TEMPLATE_KEY.name(), requestParam);

		// 新增优惠券模板信息到数据库
		CouponTemplate couponTemplate = BeanCopyUtil.copyProperties(requestParam, CouponTemplate.class);
		couponTemplate.setStatus(CouponTemplateStatusEnum.ACTIVE.getStatus());
		couponTemplate.setShopNumber(UserContext.getShopNumber());

		// 缓存预热：通过将数据库的记录序列化成 JSON 字符串放入 Redis 缓存
		CouponTemplateQueryVO actualRespDTO = BeanUtil.toBean(couponTemplate, CouponTemplateQueryVO.class);
		Map<String, Object> cacheTargetMap = BeanUtil.beanToMap(actualRespDTO, false, true);
		Map<String, String> actualCacheTargetMap = cacheTargetMap.entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						entry -> entry.getValue() != null ? entry.getValue().toString() : ""
				));
		String couponTemplateCacheKey = String.format(CouponRedisKey.COUPON_TEMPLATE_KEY, couponTemplate.getId());

		// 通过 LUA 脚本执行设置 Hash 数据以及设置过期时间
		String luaScript = "redis.call('HMSET', KEYS[1], unpack(ARGV, 1, #ARGV - 1)) " +
				"redis.call('EXPIREAT', KEYS[1], ARGV[#ARGV])";

		List<Object> keys = Collections.singletonList(couponTemplateCacheKey);
		List<Object> args = new ArrayList<>(actualCacheTargetMap.size() * 2 + 1);
		actualCacheTargetMap.forEach((key, value) -> {
			args.add(key);
			args.add(value);
		});

		// 执行 LUA 脚本
		redissonService.executeLuaScript(luaScript, RScript.ReturnType.STATUS, keys, args.toArray());

		// 发送延时消息事件，优惠券活动到期修改优惠券模板状态
		CouponTemplateDelayEvent templateDelayEvent = CouponTemplateDelayEvent.builder()
				.shopNumber(UserContext.getShopNumber())
				.couponTemplateId(couponTemplate.getId())
				.delayTime(couponTemplate.getValidEndTime().getTime())
				.build();

		couponTemplateDelayExecuteStatusProducer.sendMessage(templateDelayEvent);


		// 添加优惠券模板 ID 到布隆过滤器
		couponTemplateQueryBloomFilter.add(String.valueOf(couponTemplate.getId()));
	}

	/**
	 * 分页查询商家优惠券模板
	 *
	 * @param requestParam 请求参数
	 * @return 商家优惠券模板分页数据
	 */
	@Override
	public IPage<CouponTemplateQueryVO> pageQueryCouponTemplate(CouponTemplatePageQueryDTO requestParam) {
		IPage<CouponTemplate> page = new Page<>(requestParam.getCurrent(), requestParam.getSize());
		LambdaQueryWrapper<CouponTemplate> queryWrapper = new LambdaQueryWrapper<CouponTemplate>()
				.eq(CouponTemplate::getShopNumber, UserContext.getShopNumber())
				.like(StrUtil.isNotBlank(requestParam.getName()), CouponTemplate::getName, requestParam.getName())
				.like(StrUtil.isNotBlank(requestParam.getGoods()), CouponTemplate::getGoods, requestParam.getGoods())
				.eq(Objects.nonNull(requestParam.getType()), CouponTemplate::getType, requestParam.getType())
				.eq(Objects.nonNull(requestParam.getTarget()), CouponTemplate::getTarget, requestParam.getTarget());

		IPage<CouponTemplate> selectPage = couponTemplateMapper.selectPage(page, queryWrapper);

		return selectPage.convert(each -> BeanCopyUtil.copyProperties(each, CouponTemplateQueryVO.class));
	}

	/**
	 * 查询优惠券模板详情
	 * 后管接口并不存在并发，直接查询数据库即可
	 *
	 * @param couponTemplateId 优惠券模板 ID
	 * @return 优惠券模板详情
	 */
	@Override
	public CouponTemplateQueryVO findCouponTemplateById(String couponTemplateId) {
		CouponTemplate couponTemplate = couponTemplateMapper.selectOne(new LambdaQueryWrapper<CouponTemplate>()
				.eq(CouponTemplate::getShopNumber, UserContext.getShopNumber())
				.eq(CouponTemplate::getId, couponTemplateId));
		if (couponTemplate == null) {
			return null;
		}

		return BeanCopyUtil.copyProperties(couponTemplate, CouponTemplateQueryVO.class);
	}

	/**
	 * 结束优惠券模板
	 *
	 * @param couponTemplateId 优惠券模板 ID
	 */
	@Override
	public void terminateCouponTemplate(String couponTemplateId) {
		CouponTemplate couponTemplate = couponTemplateMapper.selectOne(new LambdaQueryWrapper<CouponTemplate>()
				.eq(CouponTemplate::getShopNumber, UserContext.getShopNumber())
				.eq(CouponTemplate::getId, couponTemplateId));
		if (couponTemplate == null) {
			// 一旦查询优惠券不存在，基本可判定横向越权，可上报该异常行为，次数多了后执行封号等处理
			throw new ServiceException("优惠券模板异常，请检查操作是否正确...");
		}

		// 验证优惠券模板是否正常
		if (ObjectUtil.notEqual(couponTemplate.getStatus(), CouponTemplateStatusEnum.ACTIVE.getStatus())) {
			throw new ServiceException("优惠券模板已结束");
		}

		couponTemplate.setStatus(CouponTemplateStatusEnum.ENDED.getStatus());
		LambdaUpdateWrapper<CouponTemplate> updateWrapper = new LambdaUpdateWrapper<CouponTemplate>()
				.eq(CouponTemplate::getId, couponTemplate.getId())
				.eq(CouponTemplate::getShopNumber, UserContext.getShopNumber());
		couponTemplateMapper.update(couponTemplate, updateWrapper);

		String couponTemplateCacheKey = String.format(CouponRedisKey.COUPON_TEMPLATE_KEY, couponTemplateId);
		redissonService.addToMap(couponTemplateCacheKey, "status", String.valueOf(CouponTemplateStatusEnum.ENDED.getStatus()));
	}

	/**
	 * 增加优惠券模板发行量
	 *
	 * @param requestParam 请求参数
	 */
	@Override
	public void increaseNumberCouponTemplate(CouponTemplateNumberDTO requestParam) {
		CouponTemplate couponTemplate = couponTemplateMapper.selectOne(new LambdaQueryWrapper<CouponTemplate>()
				.eq(CouponTemplate::getShopNumber, UserContext.getShopNumber())
				.eq(CouponTemplate::getId, requestParam.getCouponTemplateId()));
		if (couponTemplate == null) {
			// 一旦查询优惠券不存在，基本可判定横向越权，可上报该异常行为，次数多了后执行封号等处理
			throw new ServiceException("优惠券模板异常，请检查操作是否正确...");
		}

		// 验证优惠券模板是否正常
		if (ObjectUtil.notEqual(couponTemplate.getStatus(), CouponTemplateStatusEnum.ACTIVE.getStatus())) {
			throw new ServiceException("优惠券模板已结束");
		}

		// 设置数据库优惠券模板增加库存发行量
		int increased = couponTemplateMapper.increaseNumberCouponTemplate(UserContext.getShopNumber(), requestParam.getCouponTemplateId(), requestParam.getNumber());
		if (!SqlHelper.retBool(increased)) {
			throw new ServiceException("优惠券模板增加发行量失败");
		}

		// 增加优惠券模板缓存库存发行量
		String couponTemplateCacheKey = String.format(CouponRedisKey.COUPON_TEMPLATE_KEY, requestParam.getCouponTemplateId());
		redissonService.incrementHashValue(couponTemplateCacheKey, "stock", requestParam.getNumber());
	}
}
