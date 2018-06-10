package com.isc.wxy.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.isc.wxy.enums.ResponseCode;

/**
 * Created by XY W on 2018/5/18.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerResponse<T> {
    //错误码
    private Integer status;
    //提示信息
    private String msg;
    //具体内容
    private T data;

    private ServerResponse(int status)
    {
        this.status=status;
    }
    private ServerResponse(int status,T data)
    {
        this.status=status;
        this.data=data;
    }
    private ServerResponse(int status,String msg,T data)
    {
        this.status=status;
        this.data=data;
        this.msg=msg;
    }
    private ServerResponse(int status,String msg)
    {
        this.status=status;
        this.msg=msg;
    }
    @JsonIgnore
    public boolean isSuccess(){
        return this.status== ResponseCode.SUCCESS.getCode();
    }
    public static <T> ServerResponse<T> createBySuccess()
    {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }
    public static  <T>ServerResponse<T> createBySuccessMessage(String msg)
    {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg);
    }
    public static  <T>ServerResponse<T> createBySuccess(T data)
    {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),data);
    }
    public static  <T>ServerResponse<T> createBySuccessMessage(String msg,T data)
    {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg,data);
    }
    public static <T> ServerResponse<T> createByError()
    {
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getMsg());
    }
    public static <T> ServerResponse<T> createByErrorMessage(String message){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),message);
    }
    public static <T> ServerResponse<T> createByErrorCodeMessage(Integer errCode,String msg)
    {
        return new ServerResponse<T>(errCode,msg);
    }


    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
