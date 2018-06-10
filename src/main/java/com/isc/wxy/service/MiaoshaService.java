package com.isc.wxy.service;

import com.isc.wxy.domain.Order;
import com.isc.wxy.domain.Product;
import com.isc.wxy.domain.User;
import com.isc.wxy.vo.ServerResponse;

import java.awt.image.BufferedImage;

/**
 * Created by XY W on 2018/5/26.
 */
public interface MiaoshaService {
    public ServerResponse miaosha(User user, Integer productId);
    public ServerResponse getMiaoshaResult(Integer userId, Integer productId);
    public ServerResponse checkPath(User user, Integer productId, String path);
    public ServerResponse createMiaoshaPath(User user, Integer productId);
    public ServerResponse createVerifyCode(User user, Integer productId);
    public ServerResponse checkVerifyCode(User user, Integer productId, Integer verifyCode);
}
