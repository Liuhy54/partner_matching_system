package com.userms.usermsbackend.exception;

import com.userms.usermsbackend.common.ErrorCode;

/**
 * 自定义异常类
 *
 * @author haiy
 */
public class BusinessException extends RuntimeException{

    //给原本的异常类扩充了两个字段
    private final int code;

    private final String description;

    //提供了几个构造函数，可以根据需要选择使用
    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
