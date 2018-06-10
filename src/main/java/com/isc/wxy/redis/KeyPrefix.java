package com.isc.wxy.redis;

/**
 * Created by XY W on 2018/5/18.
 */
public interface KeyPrefix {
    public int expireSeconds();
    public  String getPrefix();
}
