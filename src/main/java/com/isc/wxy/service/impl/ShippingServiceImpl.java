package com.isc.wxy.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.isc.wxy.dao.ShippingDao;
import com.isc.wxy.domain.Shipping;
import com.isc.wxy.service.ShippingService;
import com.isc.wxy.vo.ServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by XY W on 2018/5/23.
 */
@Service
public class ShippingServiceImpl implements ShippingService {

    @Autowired
    ShippingDao shippingDao;
    public ServerResponse add(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int res=shippingDao.insert(shipping);
        if(res>0){
            Map result = Maps.newHashMap();
            result.put("shippingId",shipping.getId());
            return ServerResponse.createBySuccessMessage("新建地址成功",result);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }

    public ServerResponse<String> delete(Integer userId,Integer shippingId){
        int rescount=shippingDao.deleteByShippingIdUserId(userId,shippingId);
        if(rescount>0){
            return  ServerResponse.createBySuccessMessage("删除地址成功");
        }
        return ServerResponse.createBySuccessMessage("删除地址失败");
    }

    public ServerResponse<String> update(Integer userId,Shipping shipping){
       shipping.setUserId(userId);
        int rescount=shippingDao.updateByShipping(shipping);
        if(rescount>0){
            return  ServerResponse.createBySuccessMessage("更新地址成功");
        }
        return ServerResponse.createBySuccessMessage("更新地址失败");
    }

    public ServerResponse<Shipping> selectDetail(Integer userId,Integer shippingId){
        Shipping shipping=shippingDao.selectByShippingIdUserId(userId,shippingId);
        if(shipping!=null){
            return  ServerResponse.createBySuccess(shipping);
        }
        return ServerResponse.createBySuccessMessage("无法找到该地址");
    }

    public  ServerResponse<PageInfo>list(Integer userId,Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList=shippingDao.selectByUserId(userId);
        PageInfo pageInfo =new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}

