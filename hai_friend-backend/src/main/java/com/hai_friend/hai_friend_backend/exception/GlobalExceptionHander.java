package com.hai_friend.hai_friend_backend.exception;

import com.hai_friend.hai_friend_backend.common.BaseResponse;
import com.hai_friend.hai_friend_backend.common.ErrorCode;
import com.hai_friend.hai_friend_backend.common.ResultUils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author haiy
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHander {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandle(BusinessException e){
        log.error("businessException"+e.getMessage(), e);
        return ResultUils.error(e.getCode(), e.getMessage(),e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandle(RuntimeException e){
        log.error("runtimeExceptionHandle", e);
        return ResultUils.error(ErrorCode.SYSTEM_ERROR, e.getMessage(),"");
    }
}
