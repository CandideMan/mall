package com.isc.wxy.enums;

/**
 * Created by XY W on 2018/5/24.
 */
public enum AlipayCallbackEnum {
    TRADE_STATUS_WAIT_BUYER_PAY("WAIT_BUYER_PAY"),
    TRADE_STATUS_TRADE_SUCCESS ("TRADE_SUCCESS"),
    RESPONSE_SUCCESS("success"),
    RESPONSE_FAILED("failed");

    private String msg;

    public String getMsg() {
        return msg;
    }

    AlipayCallbackEnum(String value) {
        this.msg = value;
    }
}
