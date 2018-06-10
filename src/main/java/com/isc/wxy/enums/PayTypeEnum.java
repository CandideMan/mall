package com.isc.wxy.enums;

import com.isc.wxy.exception.MallException;

/**
 * Created by XY W on 2018/5/25.
 */
public enum PayTypeEnum {
    ONLINE_PAY(1,"在线支付")
            ;

     PayTypeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private int code;
    private String msg;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
    public static PayTypeEnum codeOf(int code){
        for ( PayTypeEnum  payTypeEnum:values()){
            if(payTypeEnum.code==code)
            {
                return payTypeEnum;
            }
        }
        throw new  MallException(ResponseCode.PARAM_ERROR);
    }
}
