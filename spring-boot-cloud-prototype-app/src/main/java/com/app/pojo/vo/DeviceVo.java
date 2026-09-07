package com.app.pojo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 设备响应视图对象
 *
 * @author yanlei
 * @since 2026-09-06
 */
public final class DeviceVo {
    private DeviceVo() {
    }

    /** 当前用户设备信息。 */
    @Data
    @Accessors(chain = true)
    public static class MyDevice {
        private Long bindingId;
        private String deviceId;
        private String model;
        private String status;
        private LocalDateTime bindTime;
    }

    /** 设备绑定结果。 */
    @Data
    @Accessors(chain = true)
    public static class BindResult extends MyDevice {
        private Long giftCapacityBytes;
        private LocalDateTime giftExpireTime;
    }
}