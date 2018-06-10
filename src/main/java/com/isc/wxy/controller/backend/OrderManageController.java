package com.isc.wxy.controller.backend;

import com.github.pagehelper.PageInfo;
import com.isc.wxy.access.UserContext;
import com.isc.wxy.domain.User;
import com.isc.wxy.enums.ResponseCode;
import com.isc.wxy.service.OrderService;
import com.isc.wxy.vo.OrderVo;
import com.isc.wxy.vo.ServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by XY W on 2018/5/26.
 */
@Controller
@RequestMapping("/manage/order")
public class OrderManageController {

    @Autowired
    OrderService orderService;

    @RequestMapping("/list")
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        return orderService.manageList(user.getId(),pageNum,pageSize);
    }

    @RequestMapping("/detail")
    @ResponseBody
    public ServerResponse<OrderVo> detail(Long orderNo){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        return orderService.manageDetail(orderNo);
    }

    @RequestMapping("/serach")
    @ResponseBody
    public ServerResponse<PageInfo> serach(@RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                          @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize,
                                          Long orderNo){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        return orderService.manageSerach(orderNo,pageNum,pageSize);
    }

    @RequestMapping("/send_goods")
    @ResponseBody
    public ServerResponse<String> sendGoods(Long orderNo){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        return orderService.sendGood(orderNo);
    }

}
