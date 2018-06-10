package com.isc.wxy.enums;

/**
 * Created by XY W on 2018/5/19.
 */
public enum RoleEnum {
    CUSTOM_ROLE(0,"普通用户"),
    ADMIN_ROLE(1,"管理员")
   ;
    RoleEnum(int code ,String role)
    {
        this.code=code;
        this.role=role;
    }
    private int code;
    private String role;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
