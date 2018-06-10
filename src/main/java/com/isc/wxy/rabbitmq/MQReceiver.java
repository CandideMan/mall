package com.isc.wxy.rabbitmq;

import com.isc.wxy.dao.ProductDao;
import com.isc.wxy.domain.Product;
import com.isc.wxy.redis.RedisService;
import com.isc.wxy.service.MiaoshaService;
import com.isc.wxy.service.ProductService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by XY W on 2018/5/26.
 */
@Service
public class MQReceiver {
   @Autowired
    MiaoshaService miaoshaService;
   @Autowired
    ProductDao productDao;

   @RabbitListener(queues =MQConfig.MIAOSHA_QUEUE)
    public void receive(String msg){
      MiaoshaMessage miaoshaMessage= RedisService.stringToBean(msg,MiaoshaMessage.class);
      miaoshaService.miaosha(miaoshaMessage.getUser(),miaoshaMessage.getProductId());
    }
}
