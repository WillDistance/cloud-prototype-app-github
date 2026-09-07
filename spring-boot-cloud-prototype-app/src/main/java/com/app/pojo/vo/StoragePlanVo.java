package com.app.pojo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 存储套餐响应模型
 *
 * @author yanlei
 * @since 2026-09-06
 */
public final class StoragePlanVo {
    private StoragePlanVo() {
    }

    /** 当前生效套餐。 */
    @Data
    @Accessors(chain = true)
    public static class ActivePlan {
        private String planCode;
        private Integer planVersion;
        private String planName;
        private Long capacityBytes;
        private Integer durationValue;
        private String durationUnit;
        private Long priceCent;
        private Boolean recommended;
        private LocalDateTime effectiveTime;
    }
}
