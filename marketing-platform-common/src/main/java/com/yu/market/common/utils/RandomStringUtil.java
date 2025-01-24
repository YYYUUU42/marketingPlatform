package com.yu.market.common.utils;

import java.security.SecureRandom;

/**
 * @author yu
 * @description 随机字符串工具类
 * @date 2025-01-24
 */
public class RandomStringUtil {

    /**
     * 使用 SecureRandom 以增强随机数的安全性
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成指定长度的随机数字字符串
     *
     * @param length 随机数字字符串的长度
     * @return 生成的随机数字字符串
     * @throws IllegalArgumentException 如果长度小于等于0
     */
    public static String randomNumeric(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }

        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // 生成 0-9 的随机数字
            int digit = RANDOM.nextInt(10);
            result.append(digit);
        }
        return result.toString();
    }

    /**
     * 生成指定长度的随机字母字符串（仅包含大小写英文字母）
     *
     * @param length 随机字母字符串的长度
     * @return 生成的随机字母字符串
     * @throws IllegalArgumentException 如果长度小于等于0
     */
    public static String randomAlphabetic(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }

        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // 生成随机字母（大小写混合）
            char letter = (char) ('A' + RANDOM.nextInt(52));
            if (letter > 'Z') {
                letter = (char) ('a' + (letter - 'Z' - 1));
            }
            result.append(letter);
        }
        return result.toString();
    }

    /**
     * 生成指定长度的随机字母数字字符串（包含大小写英文字母和数字）
     *
     * @param length 随机字母数字字符串的长度
     * @return 生成的随机字母数字字符串
     * @throws IllegalArgumentException 如果长度小于等于0
     */
    public static String randomAlphanumeric(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }

        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int choice = RANDOM.nextInt(3);
            switch (choice) {
                case 0: // 数字
                    result.append(RANDOM.nextInt(10));
                    break;
                case 1: // 大写字母
                    result.append((char) ('A' + RANDOM.nextInt(26)));
                    break;
                case 2: // 小写字母
                    result.append((char) ('a' + RANDOM.nextInt(26)));
                    break;
            }
        }
        return result.toString();
    }

    /**
     * 生成指定长度的随机字符串（从指定字符集中选择）
     *
     * @param length   随机字符串的长度
     * @param charPool 可供选择的字符集
     * @return 生成的随机字符串
     * @throws IllegalArgumentException 如果长度小于等于0或字符集为空
     */
    public static String randomFromCharPool(int length, String charPool) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }
        if (charPool == null || charPool.isEmpty()) {
            throw new IllegalArgumentException("Character pool must not be null or empty");
        }

        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(charPool.length());
            result.append(charPool.charAt(index));
        }
        return result.toString();
    }
}
