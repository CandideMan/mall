package com.isc.wxy.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.isc.wxy.access.UserContext;
import com.isc.wxy.domain.Order;
import com.isc.wxy.domain.User;
import com.isc.wxy.enums.AlipayCallbackEnum;
import com.isc.wxy.enums.ResponseCode;
import com.isc.wxy.service.OrderService;
import com.isc.wxy.service.impl.OrderServiceImpl;
import com.isc.wxy.vo.ServerResponse;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by XY W on 2018/5/24.
 */
@Controller
@RequestMapping("/order")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    OrderService orderService;

    @RequestMapping("/create")
    @ResponseBody
    public ServerResponse create(Integer shippingId){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        return orderService.createOrder(user.getId(),shippingId);
    }

    @RequestMapping("/cancel")
    @ResponseBody
    public ServerResponse cancel(Long orderNo){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        return orderService.cancel(user.getId(),orderNo);
    }

    @RequestMapping("/get_order_cart_product")
    @ResponseBody
    public ServerResponse getOrderCartProduct(){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        return orderService.getOrderCartProduct(user.getId());
    }


    @RequestMapping("/detail")
    @ResponseBody
    public ServerResponse detail(Long orderNo){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        return orderService.detail(user.getId(),orderNo);
    }

    @RequestMapping("/list")
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        return orderService.list(user.getId(),pageNum,pageSize);
    }














    @RequestMapping("/pay")
    @ResponseBody
    public ServerResponse pay(Long orderNo, HttpServletRequest request){
         User user= UserContext.getUser();
        if(user==null){
           return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        String path = request.getSession().getServletContext().getRealPath("upload");
         return orderService.pay(orderNo,user.getId(),path);
    }

    @RequestMapping("/alipay_callback")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request){
        Map<String,String>params= Maps.newHashMap();
      Map requestParams=  request.getParameterMap();
      for (Iterator iter=requestParams.keySet().iterator();iter.hasNext();){
          String name=(String)iter.next();
          String []values=(String[])requestParams.get(name);
          String valueStr="";
          for (int i=0;i<values.length;i++){
              valueStr=(i==values.length-1)?valueStr+values[i]:valueStr+values[i]+",";
          }
          params.put(name,valueStr);
      }
      logger.info("支付宝回调,sign:{},trade_status:{},参数:{}",params.get("sign"),params.get("trade_status"),params.toString());
        //拿到回调信息后的步骤：

        //1验证回调正确性，是不是支付宝发的
        params.remove("sign_type");
        try {
            boolean alipayRSA2CheckedV2= AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            if(!(alipayRSA2CheckedV2)){
                return AlipayCallbackEnum.RESPONSE_FAILED.getMsg();
            }
        } catch (AlipayApiException e) {
            logger.info("支付宝验证回调异常",e);
        }
            ServerResponse serverResponse=orderService.alipayCallback(params);
        if(serverResponse.isSuccess()){
            return AlipayCallbackEnum.RESPONSE_SUCCESS.getMsg();
        }
            return AlipayCallbackEnum.RESPONSE_FAILED.getMsg();

    }

    @RequestMapping("/query_order_pay_status")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(Long orderNo){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        ServerResponse serverResponse=orderService.queryOrderPayStaus(user.getId(),orderNo);
        if(serverResponse.isSuccess()){
            return  ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }

}
