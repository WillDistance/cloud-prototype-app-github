package com.app.records;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 。处理Window相关的业务逻辑
 *
 * @param startedAt 方法参数（startedAt）
 * @param count     方法参数（count）
 * @return 处理结果
 */
public record Window(long startedAt, AtomicInteger count) {
}