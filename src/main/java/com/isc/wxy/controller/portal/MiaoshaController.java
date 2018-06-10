package com.isc.wxy.controller.portal;

import com.isc.wxy.access.UserContext;
import com.isc.wxy.dao.OrderItemDao;
import com.isc.wxy.rabbitmq.MQSender;
import com.isc.wxy.rabbitmq.MiaoshaMessage;
import com.isc.wxy.domain.OrderItem;
import com.isc.wxy.domain.User;
import com.isc.wxy.enums.ResponseCode;
import com.isc.wxy.redis.ProductKey;
import com.isc.wxy.redis.RedisService;
import com.isc.wxy.service.MiaoshaService;
import com.isc.wxy.vo.ServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Created by XY W on 2018/5/26.
 */
@Controller
@RequestMapping("/miaosha")
public class MiaoshaController {
    @Autowired
    MiaoshaService miaoshaService;
    @Autowired
    RedisService redisService;
    @Autowired
    OrderItemDao orderItemDao;
    @Autowired
    MQSender sender;

    private HashMap<Integer, Boolean> localOverMap =  new HashMap<Integer, Boolean>();

    @RequestMapping(value = "/{path}/do_miaosha", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse miaosha(@RequestParam("productId") Integer productId, @PathVariable("path") String path) {
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        ServerResponse serverResponse=miaoshaService.checkPath(user,productId,path);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        boolean over=localOverMap.get(productId);
        if(over){
            return  ServerResponse.createByErrorMessage("秒杀结束");
        }
        long stock=redisService.decr(ProductKey.getMiaoshaGoodsStock,""+productId);
        if(stock<0){
            localOverMap.put(productId,true);
            return  ServerResponse.createByErrorMessage("秒杀结束");
        }
        OrderItem orderItem=orderItemDao.getByProductIdUserId(productId,user.getId());
        if(orderItem!=null){
            return  ServerResponse.createByErrorMessage("重复秒杀");
        }
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setUser(user);
        mm.setProductId(productId);
        sender.sendMiaoshaMessage(mm);
        return  ServerResponse.createBySuccessMessage("排队中");
    }

    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse miaoshaResult(@RequestParam("productId") Integer productId) {
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        return miaoshaService.getMiaoshaResult(user.getId(),productId);
    }

    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> getMiaoshaPath(@RequestParam("productId")Integer productId,
                                         @RequestParam(value = "verifyCode", defaultValue = "0") Integer verifyCode) {
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        ServerResponse serverResponse=miaoshaService.checkVerifyCode(user,productId,verifyCode);
        if(!serverResponse.isSuccess())
        {
            return  serverResponse;
        }
       return serverResponse=miaoshaService.createMiaoshaPath(user,productId);
    }

    @RequestMapping(value="/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public ServerResponse getMiaoshaVerifyCod(HttpServletResponse response, User user,
                                              @RequestParam("productId")Integer productId){
        try {
            ServerResponse serverResponse=miaoshaService.createVerifyCode(user,productId);
            if(serverResponse.isSuccess()) {
                BufferedImage image =(BufferedImage)serverResponse.getData();
                OutputStream out = response.getOutputStream();
                ImageIO.write(image, "JPEG", out);
                out.flush();
                out.close();
                return  serverResponse;
            }
            else
            {
                return ServerResponse.createByErrorMessage("生成验证码失败");
            }
        }catch(Exception e) {
            e.printStackTrace();
            return ServerResponse.createByErrorMessage("生成验证码失败");
        }
    }

}