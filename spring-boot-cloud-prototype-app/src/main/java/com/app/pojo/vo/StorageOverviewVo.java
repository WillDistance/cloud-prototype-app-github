package com.app.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 存储容量概览响应。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Getter
@AllArgsConstructor
public class StorageOverviewVo {
    /** 当前有效权益提供的总容量，单位为字节。 */
    private Long totalCapacityBytes;

    /** 已确认使用容量，单位为字节。 */
    private Long usedBytes;

    /** 上传处理中预留容量，单位为字节。 */
    private Long reservedBytes;

    /** 当前剩余可用容量，单位为字节。 */
    private Long remainingBytes;
}
