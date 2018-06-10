package com.isc.wxy.service;

import com.github.pagehelper.PageInfo;
import com.isc.wxy.domain.Shipping;
import com.isc.wxy.vo.ServerResponse;

/**
 * Created by XY W on 2018/5/23.
 */
public interface ShippingService {
    ServerResponse add(Integer userId, Shipping shipping);
    ServerResponse<String> delete(Integer userId,Integer shippingId);
    ServerResponse<String> update(Integer userId,Shipping shipping);
    ServerResponse<Shipping> selectDetail(Integer userId,Integer shippingId);
    ServerResponse<PageInfo>list(Integer userId, Integer pageNum, Integer pageSize);
}
