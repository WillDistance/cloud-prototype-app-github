package com.app.utils;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * 业务编号生成器
 *
 * @author yanlei
 * @since 2026-09-06
 */
public class BusinessNumberGenerator {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").withZone(ZoneOffset.UTC);
    private static final char[] RANDOM_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int RANDOM_LENGTH = 12;

    private static final Clock clock = Clock.systemUTC();
    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generate(String prefix) {
        String normalizedPrefix = normalizePrefix(prefix);
        StringBuilder number = new StringBuilder(normalizedPrefix)
                .append(TIME_FORMATTER.format(clock.instant()));
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            number.append(RANDOM_CHARS[secureRandom.nextInt(RANDOM_CHARS.length)]);
        }
        return number.toString();
    }

    /**
     * 处理normalizePrefix相关的业务逻辑。
     *
     * @param prefix 方法参数（prefix）
     * @return 处理结果
     */
    private static String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("业务编号前缀不能为空");
        }
        String normalized = prefix.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        if (normalized.isEmpty() || normalized.length() > 12) {
            throw new IllegalArgumentException("业务编号前缀必须为1到12位字母或数字");
        }
        return normalized;
    }
}
