package com.isc.wxy.controller.portal;

import com.isc.wxy.access.UserContext;
import com.isc.wxy.domain.User;
import com.isc.wxy.enums.CartStatusEnum;
import com.isc.wxy.enums.ResponseCode;
import com.isc.wxy.service.CartService;
import com.isc.wxy.vo.CartVo;
import com.isc.wxy.vo.ServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by XY W on 2018/5/22.
 */
@Controller
@RequestMapping("/cart")
public class CartController {
    @Autowired
    CartService cartService;

    @RequestMapping("/list")
    @ResponseBody
    public ServerResponse<CartVo> list(){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        return  cartService.list(user.getId());
    }

    @RequestMapping("/add_product")
    @ResponseBody
    public ServerResponse<CartVo> addProduct(Integer count, Integer productId){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
            return  cartService.add(user.getId(),productId,count);
    }

    @RequestMapping("/update_product")
    @ResponseBody
    public ServerResponse<CartVo> updateProduct(Integer count, Integer productId){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        return  cartService.update(user.getId(),productId,count);
    }

    @RequestMapping("/delete_product")
    @ResponseBody
    public ServerResponse<CartVo> deleteProduct(String productIds){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        return  cartService.deleProduct(user.getId(),productIds);
    }

    @RequestMapping("/select_all")
    @ResponseBody
    public ServerResponse<CartVo>selectAll(){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        return  cartService.selectOrUnselect(user.getId(),null, CartStatusEnum.CHECKED.getCode());
    }

    @RequestMapping("/un_select_all")
    @ResponseBody
    public ServerResponse<CartVo>unSelectAll(){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        return  cartService.selectOrUnselect(user.getId(),null, CartStatusEnum.UNCHECKED.getCode());
    }

    @RequestMapping("/select_one")
    @ResponseBody
    public ServerResponse<CartVo>selectOne(Integer productId){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        return  cartService.selectOrUnselect(user.getId(),productId, CartStatusEnum.CHECKED.getCode());
    }

    @RequestMapping("/un_select_one")
    @ResponseBody
    public ServerResponse<CartVo>unSelectOne(Integer productId){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        return  cartService.selectOrUnselect(user.getId(),productId, CartStatusEnum.UNCHECKED.getCode());
    }

    @RequestMapping("/get_cart_product_count")
    @ResponseBody
    public ServerResponse<Integer>getCartProductCount(){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createBySuccess(0);
        }
        return  cartService.getCartProductCount(user.getId());
    }

}
