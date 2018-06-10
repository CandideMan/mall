package com.isc.wxy.service.impl;

import com.isc.wxy.dao.OrderDao;
import com.isc.wxy.dao.OrderItemDao;
import com.isc.wxy.domain.Order;
import com.isc.wxy.domain.OrderItem;
import com.isc.wxy.domain.Product;
import com.isc.wxy.domain.User;
import com.isc.wxy.enums.ResponseCode;
import com.isc.wxy.redis.MiaoshaKey;
import com.isc.wxy.redis.RedisService;
import com.isc.wxy.service.MiaoshaService;
import com.isc.wxy.service.OrderService;
import com.isc.wxy.utils.MD5Util;
import com.isc.wxy.utils.UUIDUtil;
import com.isc.wxy.vo.ServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Created by XY W on 2018/5/26.
 */
public class MiaoshaServiceImpl implements MiaoshaService {
    @Autowired
    RedisService redisService;
   @Autowired
   OrderItemDao orderItemDao;
   @Autowired
    OrderService orderService;

    @Override
    @Transactional
    public ServerResponse miaosha(User user,Integer productId) {
        OrderItem orderItem=orderItemDao.getByProductIdUserId(productId,user.getId());
        if(orderItem!=null){
            return  ServerResponse.createByErrorMessage("重复秒杀");
        }
        ServerResponse serverResponse=orderService.createOrderByProductId(user.getId(),productId,1);
        if(serverResponse.isSuccess()){
            return serverResponse;
        }
        else
        {
         setGoodsOver(productId);
         return ServerResponse.createByErrorMessage("秒杀结束");
        }
    }

    @Override
    public ServerResponse getMiaoshaResult(Integer userId, Integer productId) {
        OrderItem orderItem=orderItemDao.getByProductIdUserId(productId,userId);
        if(orderItem!=null){
            return ServerResponse.createBySuccessMessage("秒杀成功",orderItem.getOrderNo());
        }
       else {
            if(getGoodsOver(productId)){
                return  ServerResponse.createByErrorMessage("秒杀已结束");
            }
            else{
                return  ServerResponse.createByErrorMessage("正在秒杀");
            }
        }
    }

    private void setGoodsOver(Integer goodsId) {
        redisService.set(MiaoshaKey.isGoodsOver, ""+goodsId, true);
    }

    private boolean getGoodsOver(Integer goodsId) {
        return redisService.exists(MiaoshaKey.isGoodsOver, ""+goodsId);
    }

    @Override
    public ServerResponse checkPath(User user, Integer productId, String path) {
        if(user==null||productId==null||path==null){
            return ServerResponse.createByErrorMessage(ResponseCode.PARAM_ERROR.getMsg());
        }
        String realpath=redisService.get(MiaoshaKey.getMiaoshaPath,""+user.getId() + "_"+ productId,String.class);
        if(realpath==null||!realpath.equals(path)){
            return ServerResponse.createByErrorMessage("地址不正确");
        }
        return ServerResponse.createBySuccess();
    }

    @Override
    public ServerResponse createMiaoshaPath(User user, Integer productId) {
        if(user == null || productId <=0) {
            return ServerResponse.createByErrorMessage(ResponseCode.PARAM_ERROR.getMsg());
        }
        String str = MD5Util.MD5EncodeUtf8(UUIDUtil.uuid()+"123456");
        redisService.set(MiaoshaKey.getMiaoshaPath, ""+user.getId() + "_"+ productId, str);
        return ServerResponse.createBySuccess(str);
    }

    @Override
    public ServerResponse createVerifyCode(User user, Integer productId) {
        if(user==null||productId==null){
            return ServerResponse.createByErrorMessage(ResponseCode.PARAM_ERROR.getMsg());
        }
        int width = 80;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中
        int rnd = calc(verifyCode);
        redisService.set(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+productId, rnd);
        //输出图片
        return ServerResponse.createBySuccess(image);
    }

    @Override
    public ServerResponse checkVerifyCode(User user, Integer productId, Integer verifyCode) {
        if(user==null||productId==null|| verifyCode==null)
            return ServerResponse.createByErrorMessage(ResponseCode.PARAM_ERROR.getMsg());
        Integer code=redisService.get(MiaoshaKey.getMiaoshaVerifyCode,user.getId()+","+productId,Integer.class);
        if(code==null||code!=verifyCode){
      return ServerResponse.createByErrorMessage("验证码错误");
        }
        redisService.delete(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+productId);
        return ServerResponse.createBySuccess();
    }

    private static int calc(String exp) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer)engine.eval(exp);
        }catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static char[] ops = new char[] {'+', '-', '*'};
    /**
     * + - *
     * */
    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = ""+ num1 + op1 + num2 + op2 + num3;
        return exp;
    }





}
