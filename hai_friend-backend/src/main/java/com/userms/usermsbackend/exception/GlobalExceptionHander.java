package com.userms.usermsbackend.exception;

import com.userms.usermsbackend.common.BaseResponse;
import com.userms.usermsbackend.common.ErrorCode;
import com.userms.usermsbackend.common.ResultUils;
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
