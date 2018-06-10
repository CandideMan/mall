package com.isc.wxy.exception;

import com.isc.wxy.enums.ResponseCode;

/**
 * Created by XY W on 2018/5/18.
 */
public class MallException extends  RuntimeException {
    private  Integer code;
    public MallException(ResponseCode responseCode) {
        super(responseCode.getMsg());
        this.code=responseCode.getCode();
    }
    public MallException(Integer code,String msg) {
        super(msg);
        this.code=code;
    }

    public Integer getCode() {
        return code;
    }
}
