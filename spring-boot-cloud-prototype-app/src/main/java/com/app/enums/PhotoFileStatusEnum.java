package com.app.enums;

/**
 * 照片文件状态
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum PhotoFileStatusEnum {
    URL_ISSUED("URL_ISSUED", "已签发上传链接"),
    ORIGINAL_UPLOADED("ORIGINAL_UPLOADED", "原图上传成功"),
    PROCESSING("PROCESSING", "处理中"),
    COMPLETED("COMPLETED", "处理完成"),
    AVAILABLE("AVAILABLE", "可访问"),
    DELETE_PENDING("DELETE_PENDING", "待删除"),
    DELETE_FAILED("DELETE_FAILED", "删除失败");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    PhotoFileStatusEnum(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
