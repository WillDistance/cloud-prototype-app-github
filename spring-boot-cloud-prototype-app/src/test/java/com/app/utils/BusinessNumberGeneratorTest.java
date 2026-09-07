package com.app.utils;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 业务编号生成器测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class BusinessNumberGeneratorTest {

    @Test
    void shouldGenerateStableFormatAndUniqueNumbersWithinSameMillisecond() {
        Clock clock = Clock.fixed(Instant.parse("2026-09-06T01:02:03.456Z"), ZoneOffset.UTC);
        BusinessNumberGenerator generator = new BusinessNumberGenerator(clock);
        Set<String> numbers = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            numbers.add(generator.generate("PO"));
        }

        assertEquals(1000, numbers.size());
        assertTrue(numbers.stream().allMatch(number -> number.matches("PO20260906010203456[A-Z0-9]{12}")));
    }

    @Test
    void shouldNormalizePrefixAndLimitItsLength() {
        BusinessNumberGenerator generator = new BusinessNumberGenerator(
                Clock.fixed(Instant.parse("2026-09-06T01:02:03.456Z"), ZoneOffset.UTC));

        assertTrue(generator.generate("pay-order").startsWith("PAYORDER"));
    }
}
