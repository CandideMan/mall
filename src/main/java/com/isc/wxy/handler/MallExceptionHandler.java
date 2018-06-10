package com.isc.wxy.handler;

import com.isc.wxy.exception.MallException;
import com.isc.wxy.vo.ServerResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by XY W on 2018/5/27.
 */
@ControllerAdvice
public class MallExceptionHandler {

    @ExceptionHandler(value = MallException.class)
    @ResponseBody
    public ServerResponse handlerMallException(MallException e){
        return ServerResponse.createByErrorCodeMessage(e.getCode(),e.getMessage());
    }
}
