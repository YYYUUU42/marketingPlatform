package com.yu.market.common.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yu
 * @description Bean 拷贝工具类
 * @date 2025-01-18
 */
public class BeanCopyUtil {

	/**
	 * 对象属性复制 (浅拷贝)
	 *
	 * @param source 源对象
	 * @param target 目标对象
	 */
	public static void copyProperties(Object source, Object target) {
		if (source == null || target == null) {
			throw new IllegalArgumentException("源对象和目标对象不能为空");
		}

		// 获取源对象和目标对象的类
		Class<?> sourceClass = source.getClass();
		Class<?> targetClass = target.getClass();

		// 获取源对象和目标对象的所有字段
		Field[] sourceFields = sourceClass.getDeclaredFields();
		Field[] targetFields = targetClass.getDeclaredFields();

		try {
			for (Field sourceField : sourceFields) {
				sourceField.setAccessible(true);  // 设置访问权限，允许访问私有字段

				// 查找目标对象是否有与源对象字段同名、同类型的字段
				for (Field targetField : targetFields) {
					targetField.setAccessible(true);
					if (sourceField.getName().equals(targetField.getName()) &&
							sourceField.getType().equals(targetField.getType())) {

						// 复制字段的值
						Object value = sourceField.get(source);
						targetField.set(target, value);
						break;
					}
				}
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException("对象属性复制失败", e);
		}
	}

	/**
	 * 对象属性复制 (深拷贝)
	 *
	 * @param source      源对象
	 * @param targetClass 目标对象类型
	 * @param <T>         泛型类
	 * @return 复制后的新对象
	 */
	public static <T> T copyProperties(Object source, Class<T> targetClass) {
		if (source == null) {
			throw new IllegalArgumentException("源对象不能为空");
		}

		try {
			// 创建目标对象的实例
			T target = targetClass.getDeclaredConstructor().newInstance();
			copyProperties(source, target);
			return target;
		} catch (Exception e) {
			throw new RuntimeException("深拷贝失败", e);
		}
	}

	/**
	 * 深拷贝 List 列表
	 *
	 * @param sourceList  源列表
	 * @param targetClass 目标对象类型
	 * @param <T>         泛型类
	 * @return 复制后的新列表
	 */
	public static <T> List<T> copyListProperties(List<?> sourceList, Class<T> targetClass) {
		List<T> targetList = new ArrayList<>();
		if (sourceList != null) {
			for (Object source : sourceList) {
				T target = copyProperties(source, targetClass);
				targetList.add(target);
			}
		}
		return targetList;
	}
}
