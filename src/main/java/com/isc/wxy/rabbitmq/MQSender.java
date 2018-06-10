package com.isc.wxy.rabbitmq;

import com.isc.wxy.redis.RedisService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by XY W on 2018/5/26.
 */
@Service
public class MQSender {
    @Autowired
    AmqpTemplate amqpTemplate;

    public void sendMiaoshaMessage(MiaoshaMessage miaoshaMessage){
        String msg=RedisService.beanToString(miaoshaMessage);
        amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE,msg);
    }
}
