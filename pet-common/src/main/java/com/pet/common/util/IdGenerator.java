package com.pet.common.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * ID / 订单号生成器
 */
public class IdGenerator {

    private static final String ORDER_PREFIX = "ORD";

    /**
     * 生成订单号：ORD + yyyyMMddHHmmss + 6位随机数
     * 示例：ORD20260427193045123456
     */
    public static String generateOrderNo() {
        return ORDER_PREFIX + DateUtils.getCompactTimestamp() + randomDigits(6);
    }

    /**
     * 生成指定位数的随机数字字符串
     */
    public static String randomDigits(int length) {
        StringBuilder sb = new StringBuilder();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}