package com.isc.wxy.redis;

/**
 * Created by XY W on 2018/5/26.
 */
public class ProductKey extends BasePrefix{
    private ProductKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
    public static ProductKey getMiaoshaGoodsStock= new ProductKey(0, "gs");
}
