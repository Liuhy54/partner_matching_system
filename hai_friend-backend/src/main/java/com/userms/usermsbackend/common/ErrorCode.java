package com.userms.usermsbackend.common;

/**
 * 错误码
 *
 * @author haiy
 */
public enum ErrorCode {

    SUCCESS(0, "ok", ""),
    PRAMS_ERROR(40000, "参数错误", ""),
    NULL_ERROR(40001, "请求参数为空", ""),
    NOT_LOGIN(40100, "未登录", ""),
    NOT_PERMISSION(40101, "无权限", ""),
    SYSTEM_ERROR(50000, "系统内部错误", "");

    /**
     * 状态码信息
     */
    private final int code;

    /**
     * 状态码描述信息
     */
    private final String message;
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
