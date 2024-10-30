package com.userms.usermsbackend.common;

/**
 * 返回工具类
 *
 * @author haiy
 */
public class ResultUils {

    /**
     * 成功返回
     * @param data
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败返回
     * @param errorCode
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode);

    }

    /**
     * 失败返回
     * @param code
     * @param message
     * @param description
     * @return
     */
    public static BaseResponse error(int code, String message, String description){
        return new BaseResponse<>(code, null, message, description);
    }

    /**
     * 失败返回
     * @param errorCode
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode, String message, String description){
        return new BaseResponse<>(errorCode.getCode(), null, message, description);
    }

    /**
     * 失败返回
     * @param errorCode
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode, String description){
        return new BaseResponse<>(errorCode.getCode(), errorCode.getMessage(), description);
    }
}
