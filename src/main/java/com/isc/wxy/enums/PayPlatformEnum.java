package com.isc.wxy.enums;

/**
 * Created by XY W on 2018/5/24.
 */
public enum PayPlatformEnum {
    ALI_PAY(1,"支付宝"),
    WECHAT_PAY(2,"微信支付")
    ;

    private String msg;
    private Integer code;

    public String getMsg() {
        return msg;
    }

    public  Integer getCode() {
        return code;
    }

    PayPlatformEnum( Integer code,String value ) {
        this.msg = value;
        this.code = code;
    }
}
