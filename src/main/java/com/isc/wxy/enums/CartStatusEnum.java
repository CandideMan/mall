package com.isc.wxy.enums;

/**
 * Created by XY W on 2018/5/22.
 */
public enum CartStatusEnum {
        CHECKED(1,"已勾选"),
        UNCHECKED(0,"未勾选"),
        LIMIT_NUM_FAIL(3,"数量超过限制"),
        LIMIT_NUM_SUCCESS(4,"数量没有超过限制")
        ;
        private Integer code;
        private  String msg;
    CartStatusEnum(Integer code,String msg) {
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

