package com.cetc28s.minichatjava.exception;


import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理自定义业务异常
    @ExceptionHandler(BusinessException.class)
    public CommonResult<Void> handleBusinessException(BusinessException e) {
        return CommonResult.error(e.getCode(), e.getMessage());
    }

    // 处理参数校验异常（如 @Valid 校验失败）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<Void> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return CommonResult.error(ErrorCodeEnum.INVALID_PARAM.getCode(), message);
    }

    // 处理其他未预料的异常（如空指针）
    @ExceptionHandler(Exception.class)
    public CommonResult<Void> handleException(Exception e) {
        e.printStackTrace();
        // 实际生产环境应记录日志，而不是返回堆栈信息
        return CommonResult.error(500, "系统繁忙，请稍后再试");
    }
}