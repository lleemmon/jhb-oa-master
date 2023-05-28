package com.jhb.common.config.exception;

import com.jhb.common.result.Result;
import com.jhb.common.result.ResultCodeEnum;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice
public class GlobalException {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(){
        return Result.fail().message("发生了全局异常......");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public Result accessDeniedError(){
        return Result.build(null, ResultCodeEnum.PERMISSION);
    }

    //自定义异常处理
    @ExceptionHandler(GuiguException.class)
    @ResponseBody
    public Result error(GuiguException e) {
        e.printStackTrace();
        return Result.fail().code(e.getCode()).message(e.getMsg());
    }
}
