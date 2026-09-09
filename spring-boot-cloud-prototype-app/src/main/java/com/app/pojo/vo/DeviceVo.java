package com.app.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 当前用户设备资料响应。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Getter
@AllArgsConstructor
public class DeviceVo {
    private Long id;
    private String deviceId;
    private String model;
    private String status;
    private LocalDateTime bindTime;
    private Long giftCapacityBytes;
    private LocalDateTime giftExpireTime;
}
