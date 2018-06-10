package com.isc.wxy.controller.portal;

import com.github.pagehelper.PageInfo;
import com.isc.wxy.access.UserContext;
import com.isc.wxy.domain.Shipping;
import com.isc.wxy.domain.User;
import com.isc.wxy.enums.ResponseCode;
import com.isc.wxy.service.ShippingService;
import com.isc.wxy.vo.ServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by XY W on 2018/5/23.
 */
@RestController
@RequestMapping("/shipping")
public class ShippingController {
        @Autowired
    ShippingService shippingService;

        @RequestMapping("/add")
        public ServerResponse add(Shipping shipping){
            User user= UserContext.getUser();
            if(user==null){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
            }
            return  shippingService.add(user.getId(),shipping);
        }

    @RequestMapping("/delete")
    public ServerResponse delete(Integer shippingId){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        return  shippingService.delete(user.getId(),shippingId);
    }

    @RequestMapping("/update")
    public ServerResponse update(Shipping shipping){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        return  shippingService.update(user.getId(),shipping);
    }

    @RequestMapping("/select_detail")
    public ServerResponse<Shipping> update(Integer shippingId){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        return  shippingService.selectDetail(user.getId(),shippingId);
    }

    @RequestMapping("/list")
    public ServerResponse<PageInfo> list(@RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize){
        User user=UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
            return  shippingService.list(user.getId(),pageNum,pageSize);
    }
}
