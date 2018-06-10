package com.isc.wxy.service.impl;

import com.google.common.collect.Lists;
import com.isc.wxy.dao.CartDao;
import com.isc.wxy.dao.ProductDao;
import com.isc.wxy.domain.Cart;
import com.isc.wxy.domain.Product;
import com.isc.wxy.enums.CartStatusEnum;
import com.isc.wxy.enums.ResponseCode;
import com.isc.wxy.service.CartService;
import com.isc.wxy.utils.BigDecimalUtil;
import com.isc.wxy.utils.PropertiesUtil;
import com.isc.wxy.vo.CartProductVo;
import com.isc.wxy.vo.CartVo;
import com.isc.wxy.vo.ServerResponse;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.List;

/**
 * Created by XY W on 2018/5/22.
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartDao cartDao;
    @Autowired
    ProductDao productDao;

    public ServerResponse<CartVo> add(Integer userId,Integer productId,Integer count){
           if(productId==null||count==null)
           return  ServerResponse.createByErrorCodeMessage(ResponseCode.PARAM_ERROR.getCode(),ResponseCode.PARAM_ERROR.getMsg());
            Cart cart = cartDao.selectCartByUserIdProductId(userId,productId);
            if(cart==null){
                //产品不在购物车里，需要新增这和个产品的记录
                Cart cartItem=new Cart();
                cartItem.setQuantity(count);
                cartItem.setUserId(userId);
                cartItem.setProductId(productId);
                cartItem.setChecked(CartStatusEnum.CHECKED.getCode());
                cartDao.insert(cartItem);
            }else {
                //产品已经在购物车里了
                count=cart.getQuantity()+count;
                cart.setQuantity(count);
                cartDao.updateByPrimaryKeySelective(cart);
            }
            CartVo cartVo=getCartVoLimit(userId);
            return  ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo>update(Integer userId,Integer productId,Integer count){
        if(productId==null||count==null)
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.PARAM_ERROR.getCode(),ResponseCode.PARAM_ERROR.getMsg());
        Cart cart = cartDao.selectCartByUserIdProductId(userId,productId);
        if(cart!=null){
            cart.setQuantity(count);
            cartDao.updateByPrimaryKeySelective(cart);
        }
        CartVo cartVo =getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo>deleProduct(Integer userId,String productIds){
        if(productIds==null)
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.PARAM_ERROR.getCode(),ResponseCode.PARAM_ERROR.getMsg());
            String products[]=productIds.split(",");
            List<String> productList=Lists.newArrayList();
            for (int i=0;i<products.length;i++) {
                productList.add(products[i]);
            }
            if(CollectionUtils.isEmpty(productList)){
                return  ServerResponse.createByErrorCodeMessage(ResponseCode.PARAM_ERROR.getCode(),ResponseCode.PARAM_ERROR.getMsg());
            }
            cartDao.deleteByUserIdProductIds(userId,productList);
        CartVo cartVo=getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo>selectOrUnselect(Integer userId,Integer checked,Integer productId){
        cartDao.checkedOrUncheckedProduct(userId,productId,checked);
        CartVo cartVo =getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<Integer> getCartProductCount(Integer userId){
        if(userId==null){
            return ServerResponse.createBySuccess(0);
        }
            return ServerResponse.createBySuccess(cartDao.selectCartProductCount(userId));
    }


    public ServerResponse<CartVo>list(Integer userId){
            CartVo cartVo=getCartVoLimit(userId);
            return ServerResponse.createBySuccess(cartVo);
    }

    private CartVo getCartVoLimit(Integer userId){
        CartVo cartVo=new CartVo();
        List<Cart> cartList=cartDao.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList= Lists.newArrayList();
        BigDecimal cartTotalPrice=new BigDecimal("0");
        if(CollectionUtils.isNotEmpty(cartList)){
            for (Cart cartItem:cartList){
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());
                Product product = productDao.selectByPrimaryKey(cartItem.getProductId());
                if(product != null){
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    //判断库存
                    int buyLimitCount=0;
                    if(product.getStock()>=cartItem.getQuantity()){
                        buyLimitCount=cartProductVo.getQuantity();
                        cartProductVo.setLimitQuantity(CartStatusEnum.LIMIT_NUM_SUCCESS.getMsg());
                    }else{
                        buyLimitCount=product.getStock();
                        cartProductVo.setLimitQuantity(CartStatusEnum.LIMIT_NUM_FAIL.getMsg());
                        //购物车中更新有效库存
                        Cart cartForQuantity=new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartDao.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    //计算总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVo.getQuantity().doubleValue()));
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }
                if(cartItem.getChecked()==CartStatusEnum.CHECKED.getCode()){
                    //如果勾选 ，增加到总价中
                    cartTotalPrice=BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
            cartVo.setCartProductVoList(cartProductVoList);
            cartVo.setCartTotalPrice(cartTotalPrice);
            cartVo.setAllChecked(getAllCheckedStatus(userId));
            cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
            return cartVo;
    }

    private boolean getAllCheckedStatus(Integer userId){
        if(userId==null){
            return false;
        }
        return cartDao.selectCartProductCheckedStatusByUserId(userId)==0;
    }

}
