package com.isc.wxy.redis;

/**
 * Created by XY W on 2018/5/18.
 */
public class UserKey extends BasePrefix {
    public static  final int TOKEN_EXPIRE=3600*24*2;
    public static  final int FORGET_TOKEN_EXPIRE=300;
    public static  final int CURRENT_TOKEN_EXPIRE=300;
    private UserKey(int expireSeconds,String prefix)
    {
        super(expireSeconds,prefix);
    }
    public static UserKey token=new UserKey(TOKEN_EXPIRE, "tk");
    public static  UserKey forgettoken=new UserKey(FORGET_TOKEN_EXPIRE,"forgetTk");
    public static UserKey currentToken=new UserKey(CURRENT_TOKEN_EXPIRE, "currentTk");
}
