package com.app.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 存储权益响应。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Getter
@AllArgsConstructor
public class StorageEntitlementVo {
    /** 权益业务编号。 */
    private String entitlementNo;

    /** 权益来源类型。 */
    private String sourceType;

    /** 权益名称快照。 */
    private String nameSnapshot;

    /** 权益容量，单位为字节。 */
    private Long capacityBytes;

    /** 权益生效时间。 */
    private LocalDateTime effectiveTime;

    /** 权益到期时间。 */
    private LocalDateTime expireTime;

    /** 权益当前状态。 */
    private String status;
}
