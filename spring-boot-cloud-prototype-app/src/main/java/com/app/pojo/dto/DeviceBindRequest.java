package com.app.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 设备绑定请求。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Data
public class DeviceBindRequest {
    /** 设备业务编号。 */
    @NotBlank
    private String deviceId;

    /** 设备初始绑定密码。 */
    @NotBlank
    private String password;
}
