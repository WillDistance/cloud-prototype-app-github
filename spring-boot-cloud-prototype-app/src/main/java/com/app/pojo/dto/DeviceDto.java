package com.app.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 设备请求数据传输对象
 *
 * @author yanlei
 * @since 2026-09-06
 */
public final class DeviceDto {
    private DeviceDto() {
    }

    /** 设备永久绑定参数。 */
    @Data
    @Accessors(chain = true)
    public static class BindDevice {
        @NotBlank
        @Pattern(regexp = "^CC-[0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$")
        private String deviceId;

        @NotBlank
        @Size(max = 128)
        private String password;
    }
}