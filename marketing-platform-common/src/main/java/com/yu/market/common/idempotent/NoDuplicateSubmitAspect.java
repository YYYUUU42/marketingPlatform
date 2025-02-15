package com.yu.market.common.idempotent;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.yu.market.common.idempotent.anno.NoDuplicateSubmit;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.sql.rowset.serial.SerialException;
import java.lang.reflect.Method;

/**
 * @author yu
 * @description 防止用户重复提交表单信息切面控制器
 * @date 2025-02-15
 */
@Aspect
@RequiredArgsConstructor
public final class NoDuplicateSubmitAspect {

	private final RedissonClient redissonClient;

	/**
	 * 增强方法标记 {@link NoDuplicateSubmit} 注解逻辑
	 */
	@Around("@annotation(com.yu.market.common.idempotent.anno.NoDuplicateSubmit)")
	public Object noDuplicateSubmit(ProceedingJoinPoint joinPoint) throws Throwable {
		NoDuplicateSubmit noDuplicateSubmit = getNoDuplicateSubmitAnnotation(joinPoint);
		String lockKey = generateLockKey(joinPoint);
		RLock lock = redissonClient.getLock(lockKey);

		// 尝试获取锁，获取锁失败就意味着已经重复提交，直接抛出异常
		if (!lock.tryLock()) {
			throw new SerialException(noDuplicateSubmit.message());
		}

		Object result;
		try {
			result = joinPoint.proceed();
		} finally {
			lock.unlock();
		}
		return result;
	}

	/**
	 * @return 获取分布式锁标识
	 */
	private String generateLockKey(ProceedingJoinPoint joinPoint) {
		return String.format("no-duplicate-submit:path:%s:currentUserId:%s:md5:%s",
				getServletPath(), getCurrentUserId(), calculateArgsMD5(joinPoint));
	}

	/**
	 * @return 返回自定义防重复提交注解
	 */
	public static NoDuplicateSubmit getNoDuplicateSubmitAnnotation(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		Method targetMethod = joinPoint.getTarget().getClass().getDeclaredMethod(methodSignature.getName(), methodSignature.getMethod().getParameterTypes());
		return targetMethod.getAnnotation(NoDuplicateSubmit.class);
	}

	/**
	 * @return 获取当前线程上下文 ServletPath
	 */
	private String getServletPath() {
		ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		return sra != null ? sra.getRequest().getServletPath() : "";
	}

	/**
	 * @return 当前操作用户 ID
	 */
	private String getCurrentUserId() {
		// todo 模拟用户
		return "test123";
	}

	/**
	 * @return joinPoint md5
	 */
	private String calculateArgsMD5(ProceedingJoinPoint joinPoint) {
		return DigestUtil.md5Hex(JSONUtil.toJsonStr(joinPoint.getArgs()));
	}
}
