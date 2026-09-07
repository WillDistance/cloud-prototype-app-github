package com.app.pojo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 存储信息响应模型
 *
 * @author yanlei
 * @since 2026-09-06
 */
public final class StorageVo {
    private StorageVo() {
    }

    /** 存储容量概览。 */
    @Data
    @Accessors(chain = true)
    public static class Overview {
        private Long totalCapacityBytes;
        private Long usedBytes;
        private Long reservedBytes;
        private Long remainingBytes;
        private LocalDateTime nearestExpireTime;
    }

    /** 有效存储权益明细。 */
    @Data
    @Accessors(chain = true)
    public static class Entitlement {
        private String entitlementNo;
        private String sourceType;
        private String nameSnapshot;
        private Long capacityBytes;
        private LocalDateTime effectiveTime;
        private LocalDateTime expireTime;
    }
}
