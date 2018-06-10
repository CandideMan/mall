package com.isc.wxy.service;

import com.github.pagehelper.PageInfo;
import com.isc.wxy.vo.OrderVo;
import com.isc.wxy.vo.ServerResponse;

import java.util.Map;

/**
 * Created by XY W on 2018/5/24.
 */
public interface OrderService {
    ServerResponse pay(long orderNo, Integer userId, String path);
    ServerResponse alipayCallback(Map<String,String> params);
    ServerResponse queryOrderPayStaus(Integer userId ,Long orderNo);
    ServerResponse createOrder(Integer userId, Integer shippingId);
    ServerResponse<String> cancel(Integer userId,Long orderNo);
    ServerResponse getOrderCartProduct(Integer userId);
    ServerResponse<OrderVo> detail(Integer userId, Long orderNo);
    ServerResponse<PageInfo>list(Integer userId, Integer pageNum, Integer pageSize);
    ServerResponse<PageInfo>manageList(Integer userId,Integer pageNum,Integer pageSize);
    ServerResponse<OrderVo> manageDetail(Long orderNo);
    ServerResponse<PageInfo> manageSerach(Long orderNo,Integer pageNum,Integer pageSize);
    ServerResponse<String> sendGood(Long orderNo);
    ServerResponse createOrderByProductId(Integer userId, Integer productId,Integer quantity);
}
