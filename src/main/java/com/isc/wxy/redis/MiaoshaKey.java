package com.isc.wxy.redis;

/**
 * Created by XY W on 2018/5/26.
 */
public class MiaoshaKey extends BasePrefix {
    private MiaoshaKey( int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
    public static MiaoshaKey isGoodsOver = new MiaoshaKey(0, "go");
    public static MiaoshaKey getMiaoshaPath = new MiaoshaKey(60, "mp");
    public static MiaoshaKey getMiaoshaVerifyCode = new MiaoshaKey(300, "vc");
}
