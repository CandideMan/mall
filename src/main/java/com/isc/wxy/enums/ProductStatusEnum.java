package com.isc.wxy.enums;

/**
 * Created by XY W on 2018/5/22.
 */
public enum ProductStatusEnum {
        UP(1,"在售"),
        DOWN(2,"下架"),
        DELETE(3,"删除")
        ;
        private Integer code;
        private  String msg;
        ProductStatusEnum(Integer code,String msg) {
            this.code = code;
            this.msg=msg;
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
}

