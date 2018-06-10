package com.isc.wxy.service;

import com.isc.wxy.vo.CartVo;
import com.isc.wxy.vo.ServerResponse;

/**
 * Created by XY W on 2018/5/22.
 */
public interface CartService {
    ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count);
    ServerResponse<CartVo>update(Integer userId,Integer productId,Integer count);
    ServerResponse<CartVo>deleProduct(Integer userId,String productIds);
    ServerResponse<CartVo>list(Integer userId);
    ServerResponse<CartVo>selectOrUnselect(Integer userId,Integer productId,Integer checked);
    ServerResponse<Integer> getCartProductCount(Integer userId);
}
