package com.isc.wxy.rabbitmq;

import com.isc.wxy.domain.User;

/**
 * Created by XY W on 2018/5/26.
 */
public class MiaoshaMessage {
    private User user;
    private Integer productId;
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public Integer getProductId() {
        return productId;
    }
    public void setProductId(Integer productId) {
        this.productId = productId;
    }
}
