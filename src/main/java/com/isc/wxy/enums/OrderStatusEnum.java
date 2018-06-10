package com.isc.wxy.enums;

import com.isc.wxy.exception.MallException;

/**
 * Created by XY W on 2018/5/24.
 */
public enum OrderStatusEnum {
   CANCEL(0,"已取消"),
   No_PAY(10,"未支付"),
   PAID(20,"已付款"),
   SHIPPED(40,"已发货"),
   ORDER_SUCCESS(50,"已完成"),
   ORDER_CLOSE(60,"订单关闭"),
    ;



    private String msg;
    private  Integer code ;

    public String getMsg() {
        return msg;
    }

    public Integer getCode() {
        return code;
    }

    OrderStatusEnum(Integer code,String value ) {
        this.msg = value;
        this.code = code;
    }
    public static OrderStatusEnum codeOf(Integer code){
        for (OrderStatusEnum orderStatusEnum:values()){
            if(orderStatusEnum.getCode()==code){
                return  orderStatusEnum;
            }
        }
        throw new MallException(ResponseCode.ERROR);
    }
}
