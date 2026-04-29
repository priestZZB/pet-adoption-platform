package com.pet.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期工具类
 */
public class DateUtils {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String COMPACT_FORMAT = "yyyyMMddHHmmss";

    /**
     * 获取当前时间字符串 yyyy-MM-dd HH:mm:ss
     */
    public static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIME_FORMAT));
    }

    /**
     * 格式化 LocalDateTime
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化 LocalDate
     */
    public static String format(LocalDate date, String pattern) {
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 解析字符串为 LocalDateTime
     */
    public static LocalDateTime parse(String dateStr, String pattern) {
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 获取紧凑时间戳 yyyyMMddHHmmss（用于订单号）
     */
    public static String getCompactTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(COMPACT_FORMAT));
    }
}