package com.isc.wxy.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.isc.wxy.dao.*;
import com.isc.wxy.domain.*;
import com.isc.wxy.enums.*;
import com.isc.wxy.service.OrderService;
import com.isc.wxy.utils.BigDecimalUtil;
import com.isc.wxy.utils.DateTimeUtil;
import com.isc.wxy.utils.FTPUtil;
import com.isc.wxy.utils.PropertiesUtil;
import com.isc.wxy.vo.*;
import com.sun.org.apache.xpath.internal.operations.Or;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by XY W on 2018/5/24.
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    OrderDao orderDao;
    @Autowired
    OrderItemDao orderItemDao;
    @Autowired
    PayInfoDao payInfoDao;
    @Autowired
    CartDao cartDao;
    @Autowired
    ProductDao productDao;
    @Autowired
    ShippingDao shippingDao;

    private static final Logger log =LoggerFactory.getLogger(OrderServiceImpl.class);

    @Transactional
    public ServerResponse createOrder(Integer userId, Integer shippingId){
       //从购物车获取数据
        List<Cart> cartList=cartDao.selectCheckedCartByUserId(userId);

        ServerResponse serverResponse=getCartOrderItem(cartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        //计算总价
        List<OrderItem>orderItemList=(List<OrderItem>)serverResponse.getData();
        BigDecimal payment=getOrderTotalPrice(orderItemList);

        //生成订单
        Order order =assembleOrder(userId,shippingId,payment);
        if(order==null){
            return ServerResponse.createByErrorMessage("生成订单错误");
        }
       for (OrderItem orderItem:orderItemList){
            orderItem.setOrderNo(order.getOrderNo());
       }
        //mybatis 批量插入
        orderItemDao.batchInsert(orderItemList);

        //生成成功，减少库存
        reduceProductStock(orderItemList);

        //清空购物车
        cleanCart(cartList);

        //返回前端数据
        OrderVo orderVo=assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }
    @Transactional
    public ServerResponse createOrderByProductId(Integer userId, Integer productId,Integer quantity){

       Product product=productDao.selectByPrimaryKey(productId);
        //校验产品状态
        if(!product.getStatus().equals(ProductStatusEnum.UP.getCode())){
            return ServerResponse.createByErrorMessage("产品"+product.getName()+"已下架");
        }
        //校验产品库存
        if(quantity>product.getStock()){
            return ServerResponse.createByErrorMessage("产品"+product.getName()+"库存不足");
        }
        //计算总价

        BigDecimal payment=BigDecimalUtil.mul(product.getPrice().doubleValue(),quantity.doubleValue());

        //生成订单
        Order order =assembleOrder(userId,payment);

        OrderItem orderItem=createOrderItem(userId,product,order,quantity);

        //mybatis 插入
        orderItemDao.insert(orderItem);

        //生成成功，减少库存
        product.setStock(product.getStock()-quantity);
        productDao.updateByPrimaryKeySelective(product);

        return ServerResponse.createBySuccess(order);
    }

    public ServerResponse<String> cancel(Integer userId,Long orderNo){
        Order order =orderDao.selectByUserIdAndOrderNo(userId,orderNo);
        if(order==null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        if(order.getStatus()>OrderStatusEnum.No_PAY.getCode()){
            return ServerResponse.createByErrorMessage("无法取消订单");
        }
        Order newOrder=new Order();
        newOrder.setStatus(OrderStatusEnum.CANCEL.getCode());
        newOrder.setId(order.getId());
        int count=orderDao.updateByPrimaryKeySelective(newOrder);
        if(count>0){
            return ServerResponse.createBySuccess("取消成功");
        }
        return ServerResponse.createBySuccess("取消失败");
    }

    public ServerResponse getOrderCartProduct(Integer userId){
        OrderProductVo orderProductVo=new OrderProductVo();
        List<Cart>cartList=cartDao.selectCheckedCartByUserId(userId);
        ServerResponse serverResponse= getCartOrderItem(cartList);
        if(!serverResponse.isSuccess()){
            return  serverResponse;
        }
        List<OrderItem> orderItemList=(List<OrderItem>)serverResponse.getData();
        BigDecimal payment=getOrderTotalPrice(orderItemList);
        List<OrderItemVo> orderItemVoList=Lists.newArrayList();
        for (OrderItem orderItem:orderItemList){
            OrderItemVo orderItemVo=assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServerResponse.createBySuccess(orderProductVo);
    }

    public  ServerResponse<OrderVo> detail(Integer userId,Long orderNo){
        Order order=orderDao.selectByUserIdAndOrderNo(userId,orderNo);
        if(order==null)
        {
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        List<OrderItem> orderItemList=orderItemDao.getByOrderNoUserId(orderNo, userId);
        OrderVo orderVo=assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    public ServerResponse<PageInfo>list(Integer userId,Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList =orderDao.selectByUserId(userId);
        List<OrderVo>orderVoList=assembleOrderVoList(orderList,userId);
        PageInfo pageInfo=new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse<PageInfo>manageList(Integer userId,Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList =orderDao.selectAllOrder();
        List<OrderVo>orderVoList=assembleOrderVoList(orderList,null);
        PageInfo pageInfo=new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse<OrderVo> manageDetail(Long orderNo){
        Order order=orderDao.selectByOrderNo(orderNo);
        if(order==null)
        {
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        List<OrderItem>orderItemList =orderItemDao.getByOrderNo(orderNo);
        OrderVo orderVo=assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    public ServerResponse<PageInfo> manageSerach(Long orderNo,Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        Order order=orderDao.selectByOrderNo(orderNo);
        if(order==null)
        {
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        List<OrderItem>orderItemList =orderItemDao.getByOrderNo(orderNo);
        PageInfo pageInfo=new PageInfo(Lists.newArrayList(order));
        OrderVo orderVo=assembleOrderVo(order,orderItemList);
        pageInfo.setList(Lists.newArrayList(orderVo));
        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse<String> sendGood(Long orderNo){
        Order order=orderDao.selectByOrderNo(orderNo);
        if(order==null)
        {
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        if(order.getStatus()==OrderStatusEnum.PAID.getCode()){
            order.setStatus(OrderStatusEnum.SHIPPED.getCode());
            order.setSendTime(new Date());
            orderDao.updateByPrimaryKey(order);
            return ServerResponse.createBySuccess("发货成功");
        }
        return ServerResponse.createBySuccess("发货失败,订单状态不正确");
    }

    private List<OrderVo> assembleOrderVoList(List<Order>orderList,Integer userId){
        List<OrderVo>orderVoList=Lists.newArrayList();
        for (Order order:orderList){
            List<OrderItem>orderItemList;
            //管理员不需要userId
            if(userId==null){
                orderItemList =orderItemDao.getByOrderNo(order.getOrderNo());
            }else{
                orderItemList=orderItemDao.getByOrderNoUserId(order.getOrderNo(),userId);
            }
            OrderVo orderVo=  assembleOrderVo(order,orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    private OrderVo assembleOrderVo(Order order,List<OrderItem>orderItemList){
        OrderVo orderVo=new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(PayTypeEnum.codeOf(order.getPaymentType()).getMsg());
        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(OrderStatusEnum.codeOf(order.getStatus()).getMsg());
        orderVo.setShippingId(order.getShippingId());
        Shipping shipping=shippingDao.selectByPrimaryKey(order.getShippingId());
        if(shipping!=null){
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShipping(shipping));
        }
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));


        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        for(OrderItem orderItem : orderItemList){
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }

    private OrderItemVo assembleOrderItemVo(OrderItem orderItem){
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());
        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVo;
    }

    private ShippingVo assembleShipping(Shipping shipping){
        ShippingVo shippingVo =new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shippingVo.getReceiverPhone());
        return shippingVo;
    }

    private Order assembleOrder(Integer userId,Integer shippingId,BigDecimal payment){
        Order order =new Order();
        long orderNo =generateOrderNo();
        order.setOrderNo(orderNo);
        order.setPayment(payment);
        order.setStatus(OrderStatusEnum.No_PAY.getCode());
        order.setUserId(userId);
        order.setPaymentType(PayTypeEnum.ONLINE_PAY.getCode());
        order.setShippingId(shippingId);
        order.setPostage(0);
        int count= orderDao.insert(order);
        if(count>0){
            return order;
        }
        return null;
    }

    private Order assembleOrder(Integer userId,BigDecimal payment){
        Order order =new Order();
        long orderNo =generateOrderNo();
        order.setOrderNo(orderNo);
        order.setPayment(payment);
        order.setStatus(OrderStatusEnum.No_PAY.getCode());
        order.setUserId(userId);
        order.setPaymentType(PayTypeEnum.ONLINE_PAY.getCode());
        order.setPostage(0);
        int count= orderDao.insert(order);
        if(count>0){
            return order;
        }
        return null;
    }

    private  void cleanCart( List<Cart> cartList){
        for (Cart cart:cartList){
            if(cart.getChecked()==1){
                cartDao.deleteByPrimaryKey(cart.getId());
            }
        }
    }

    private  void reduceProductStock(List<OrderItem>orderItemlist){
        for (OrderItem orderItem:orderItemlist){
            Product product=productDao.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock()-orderItem.getQuantity());
            productDao.updateByPrimaryKeySelective(product);
        }
        }

    private long generateOrderNo(){
        long currentTime=System.currentTimeMillis();
        return currentTime+new Random().nextInt(100);
    }

    private BigDecimal getOrderTotalPrice(List<OrderItem>orderItemList){
        BigDecimal payment=new BigDecimal("0");
        for (OrderItem orderItem:orderItemList){
          payment=  BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    private ServerResponse<List<OrderItem>>  getCartOrderItem(List<Cart>cartList){
            List<OrderItem> orderItemList =Lists.newArrayList();
        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        for (Cart cart:cartList) {
            OrderItem orderItem =new OrderItem();
            Product product=productDao.selectByPrimaryKey(cart.getProductId());
            //校验产品状态
            if(!product.getStatus().equals(ProductStatusEnum.UP.getCode())){
                return ServerResponse.createByErrorMessage("产品"+product.getName()+"已下架");
            }
            //校验产品库存
            if(cart.getQuantity()>product.getStock()){
                return ServerResponse.createByErrorMessage("产品"+product.getName()+"库存不足");
            }
            orderItem.setUserId(cart.getUserId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cart.getQuantity()));
             orderItemList.add(orderItem);
        }
        return  ServerResponse.createBySuccess(orderItemList);
    }

    private  OrderItem createOrderItem(Integer userId,Product product,Order order,Integer quantity){
        OrderItem orderItem=new OrderItem();
        orderItem.setOrderNo(order.getOrderNo());
        orderItem.setQuantity(quantity);
        orderItem.setCurrentUnitPrice(product.getPrice());
        orderItem.setTotalPrice(order.getPayment());
        orderItem.setUserId(userId);
        orderItem.setProductName(product.getName());
        orderItem.setCreateTime(order.getCreateTime());
        orderItem.setProductImage(product.getMainImage());
        return orderItem;
    }








        public ServerResponse pay(long orderNo,Integer userId,String path){
            Map<String,String> resultMap= Maps.newHashMap();
        Order order=orderDao.selectByUserIdAndOrderNo(userId,orderNo);
        if(order==null){
            return ServerResponse.createByErrorMessage("用户没有该订单");
        }
        resultMap.put("orderNo",String.valueOf(order.getOrderNo()));
            // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
            // 需保证商户系统端不能重复，建议通过数据库sequence生成，
            String outTradeNo = order.getOrderNo().toString();

            // (必填) 订单标题，粗略描述用户的支付目的。如""xxx品牌xxx门店当面付扫码消费”
            String subject = new StringBuilder().append("iscmall扫码支付，订单号:").append(outTradeNo).toString();

            // (必填) 订单总金额，单位为元，不能超过1亿元
            // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
            String totalAmount = order.getPayment().toString();

            // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
            // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
            String undiscountableAmount = "0";

            // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
            // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
            String sellerId = "";

            // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
            String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共").append(totalAmount).append("元").toString();

            // 商户操作员编号，添加此参数可以为商户操作员做销售统计
            String operatorId = "test_operator_id";

            // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
            String storeId = "test_store_id";

            // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
            ExtendParams extendParams = new ExtendParams();
            extendParams.setSysServiceProviderId("2088100200300400500");

            // 支付超时，定义为120分钟
            String timeoutExpress = "120m";

            // 商品明细列表，需填写购买商品详细信息，
            List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
            List<OrderItem> orderItemList =orderItemDao.getByOrderNo(orderNo);
            for (OrderItem orderItem:orderItemList) {
                GoodsDetail goodsDetail= GoodsDetail.newInstance(orderItem.getProductId().toString(),orderItem.getProductName(),
                        BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(),new Double(100).doubleValue()).longValue(),orderItem.getQuantity());
                    goodsDetailList.add(goodsDetail);
            }
            // 创建扫码支付请求builder，设置请求参数
            AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                    .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                    .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                    .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                    .setTimeoutExpress(timeoutExpress)
                    .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                    .setGoodsDetailList(goodsDetailList);

            /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
             *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
             */
            Configs.init("zfbinfo.properties");

            /** 使用Configs提供的默认参数
             *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
             */
            AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
            AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
            switch (result.getTradeStatus()) {
                case SUCCESS:
                    log.info("支付宝预下单成功: )");
                    AlipayTradePrecreateResponse response = result.getResponse();
                    dumpResponse(response);

                    File folder=new File (path);
                    if(!folder.exists()){
                        folder.setWritable(true);
                        folder.mkdirs();
                    }
                    // 需要修改为运行机器上的路径
                    String qrPath = String.format(path+"/qr-%s.png", response.getOutTradeNo());
                    String qrFileName= String.format("qr-%s.png", response.getOutTradeNo());
                    log.info("qrPath:" + qrPath);
                    ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);
                    File targetFile =new File(path,qrFileName);
                    try {
                        FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                    } catch (IOException e) {
                        log.info("上传二维码异常",e);
                    }
                    String qrUrl=PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName();
                    targetFile.delete();
                    resultMap.put("qrUrl",qrUrl);
                    return  ServerResponse.createBySuccess(resultMap);
                case FAILED:
                    log.error("支付宝预下单失败!!!");
                    return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");
                case UNKNOWN:
                    log.error("系统异常，预下单状态未知!!!");
                    return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");
                default:
                    log.error("不支持的交易状态，交易返回异常!!!");
                    return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
            }
        }

        public ServerResponse alipayCallback(Map<String,String>params){
            Long orderNo=Long.valueOf(params.get("out_trade_no"));
            String tradeNo=params.get("trade_no");
            String tradeStatus=params.get("trade_status");
            Order order=orderDao.selectByOrderNo(orderNo);
            if(order==null||order.getPayment()!=BigDecimal.valueOf(Double.valueOf(params.get("total_amount")))){
                return ServerResponse.createBySuccessMessage("非商城订单，忽略");
            }
            if(order.getStatus()>= OrderStatusEnum.PAID.getCode()){
                return ServerResponse.createBySuccessMessage("支付宝重复调用");
            }
            if(tradeStatus.equals(AlipayCallbackEnum.TRADE_STATUS_TRADE_SUCCESS.getMsg())){
                order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
                order.setStatus(OrderStatusEnum.PAID.getCode());
                orderDao.updateByPrimaryKey(order);
            }
            PayInfo payInfo =new PayInfo();
            payInfo.setUserId(order.getUserId());
            payInfo.setPayPlatform(PayPlatformEnum.ALI_PAY.getCode());
            payInfo.setOrderNo(orderNo);
            payInfo.setPlatformNumber(tradeNo);
            payInfo.setPlatformStatus(tradeStatus);
            payInfoDao.insert(payInfo);
            return ServerResponse.createBySuccess();
        }

        public ServerResponse queryOrderPayStaus(Integer userId ,Long orderNo){
            Order order =orderDao.selectByUserIdAndOrderNo(userId,orderNo);
            if(order==null){
                return ServerResponse.createByErrorMessage("用户没有该订单");
            }
            if(order.getStatus()>= OrderStatusEnum.PAID.getCode()&&order.getStatus()< OrderStatusEnum.ORDER_CLOSE.getCode()){
                return ServerResponse.createBySuccess();
            }
            return ServerResponse.createByError();
        }



    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }
}
