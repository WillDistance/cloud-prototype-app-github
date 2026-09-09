package com.app.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 可销售存储套餐响应。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Getter
@AllArgsConstructor
public class StoragePlanVo {
    /** 套餐编码。 */
    private String planCode;

    /** 套餐版本。 */
    private Integer planVersion;

    /** 套餐名称。 */
    private String planName;

    /** 套餐提供容量，单位为字节。 */
    private Long capacityBytes;

    /** 套餐有效期数值。 */
    private Integer durationValue;

    /** 套餐有效期单位。 */
    private String durationUnit;

    /** 套餐销售金额，单位为货币最小单位。 */
    private Long priceCent;

    /** 套餐币种。 */
    private String currency;

    /** 是否为推荐套餐。 */
    private Boolean recommended;
}
