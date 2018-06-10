package com.isc.wxy.enums;

/**
 * Created by XY W on 2018/5/18.
 */
public enum ResponseCode {
    PRODUCT_NOT_EXIST(10,"商品不存在"),
    PRODUCT_STOCK_ERROR(11,"库存不正确"),
    ORDER_NOT_EXIST(12,"订单不存在"),
    ORDERDETAIL_NOT_EXIST(13,"订单详情不存在"),
    ORDER_STATUS_ERROR(14,"订单状态异常"),
    ORDER_UPDATE_FAIL(15,"订单更新失败"),
    ORDER_DETAIL_EMPTY(16,"订单详情为空"),
    ORDER_PAY_STATUS_ERROR(17,"订单支付状态不正确"),
    PARAM_ERROR(2,"参数不正确"),
    CART_EMPTY(18,"购物车为空"),
    ORDER_OWNER_ERROR(19,"该订单不属于当前用户"),
    WX_MP_ERROR(20,"微信公众账号错误"),
    ORDER_CANCEL_SUCCESS(25,"取消成功"),
    ORDER_FINNISH_SUCCESS(26,"完结成功"),
    PRODUCT_STATUS_ERROR(21,"商品状态不正确"),
    ON_SALE_SUCCESS(22,"上架成功"),
    OFF_SALE_SUCCESS(23,"上架成功"),
    NEED_LOGIN(3,"需要登录"),
    SUCCESS(0,"成功"),
    ERROR(1,"ERROR"),
    REDIS_PARAM_ERROR(30,"redis 参数错误"),
    PASSWORD_ERROR(4,"密码错误"),
    USER_NOT_EXIST(5,"用户不存在"),
    PLEASE_WAIT(100,"请稍后再试")
    ;

    private Integer code;
    private  String msg;
    ResponseCode(Integer code, String msg)
    {
        this.code=code;
        this.msg=msg;
    }
    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
