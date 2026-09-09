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
    /** 设备记录主键ID。 */
    private Long id;

    /** 设备业务编号。 */
    private String deviceId;

    /** 设备型号。 */
    private String model;

    /** 设备当前状态。 */
    private String status;

    /** 设备永久绑定时间。 */
    private LocalDateTime bindTime;

    /** 设备绑定赠送容量，单位为字节。 */
    private Long giftCapacityBytes;

    /** 设备绑定赠送权益到期时间。 */
    private LocalDateTime giftExpireTime;
}
