package com.app.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 统一错误码
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Getter
public enum ErrorCodeEnum {
    E00000("E00000", "success"),
    E00001("E00001", "文件上传、下载失败"),
    E00002("E00002", "文件不存在"),
    E00003("E00003", "添加分布式锁失败"),
    E00004("E00004", "图片写入字节流失败"),
    OSS("OSS10002", "对象存储异常"),

    AUTH_INVALID_CREDENTIALS("A10001", "邮箱或密码错误"),
    AUTH_CODE_INVALID("A10002", "验证码无效或已过期"),
    AUTH_REQUIRED("A10003", "请先登录"),
    AUTH_TOKEN_EXPIRED("A10004", "登录状态已过期"),
    AUTH_ACCESS_DENIED("A10005", "无权访问该资源"),
    AUTH_EMAIL_ALREADY_EXISTS("A10006", "邮箱已注册"),
    AUTH_CODE_SEND_TOO_FREQUENT("A10007", "验证码发送过于频繁"),
    AUTH_CODE_ATTEMPTS_EXCEEDED("A10008", "验证码尝试次数已超限"),
    AUTH_CODE_NOT_VERIFIED("A10009", "验证码尚未验证或已被使用"),
    AUTH_TIME_ZONE_INVALID("A10010", "时区缺失或不是有效IANA时区"),
    AUTH_PASSWORD_MISMATCH("A10011", "两次输入的密码不一致"),

    USER_LANGUAGE_UNSUPPORTED("U20001", "不支持的语言偏好"),

    DEVICE_NOT_FOUND("D20001", "设备不存在"),
    DEVICE_CREDENTIAL_INVALID("D20002", "设备凭证错误"),
    DEVICE_ALREADY_BOUND("D20003", "设备已绑定"),
    DEVICE_DISABLED("D20004", "设备已禁用"),
    DEVICE_BINDING_NOT_FOUND("D20005", "设备未绑定"),
    DEVICE_ID_INVALID("D20006", "设备ID格式错误"),
    USER_ALREADY_BOUND_DEVICE("D20007", "当前用户已绑定设备"),
    DEVICE_BINDING_CONFLICT("D20008", "设备或用户绑定状态冲突"),
    DEVICE_PLATFORM_CONFIG_NOT_FOUND("D20009", "当前平台赠送配置不可用"),

    STORAGE_PLAN_NOT_FOUND("ST30001", "存储套餐不存在"),
    STORAGE_CAPACITY_INSUFFICIENT("ST30002", "存储空间不足"),
    STORAGE_ENTITLEMENT_EXPIRED("ST30003", "存储权益已过期"),
    STORAGE_ACCOUNT_NOT_FOUND("ST30004", "存储账户不存在"),

    PHOTO_NOT_FOUND("PH40001", "照片不存在"),
    PHOTO_UNAVAILABLE("PH40002", "照片暂不可用"),
    PHOTO_UPLOAD_SESSION_INVALID("PH40003", "上传会话无效或已过期"),
    PHOTO_FILE_INVALID("PH40004", "照片文件不符合要求"),
    PHOTO_PROCESSING_FAILED("PH40005", "照片处理失败"),

    PAYMENT_PLAN_INVALID("P50001", "支付套餐无效"),
    PAYMENT_ORDER_NOT_FOUND("P50002", "支付订单不存在"),
    PAYMENT_ORDER_STATUS_INVALID("P50003", "支付订单状态无效"),
    PAYMENT_SIGNATURE_INVALID("P50004", "支付签名无效"),
    PAYMENT_AMOUNT_MISMATCH("P50005", "支付金额不一致"),
    PAYMENT_GATEWAY_UNAVAILABLE("P50006", "支付服务暂不可用"),
    PAYMENT_CURRENCY_UNSUPPORTED("P50007", "当前币种暂不支持"),
    PAYMENT_WEBHOOK_INVALID("P50008", "支付回调无效"),
    PAYMENT_PROVIDER_ORDER_INVALID("P50009", "支付平台订单无效"),

    INVALID_REQUEST_PARAMETER("S40000", "请求参数错误"),
    SYSTEM_BUSY("S50000", "系统繁忙，请稍后重试"),
    SYSTEM_DEPENDENCY_UNAVAILABLE("S50001", "依赖服务暂不可用"),
    SYSTEM_OPERATION_CONFLICT("S50002", "请求与当前状态冲突"),
    E99999("E99999", "error");

    private final String errorCode;
    private final String errorMag;

    @JsonValue
    public String getErrorCode() {
        return errorCode;
    }

    ErrorCodeEnum(String errorCode, String errorMag) {
        this.errorCode = errorCode;
        this.errorMag = errorMag;
    }
}
