package com.isc.wxy.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by XY W on 2018/5/26.
 */
@Configuration
public class MQConfig {
    public static final String MIAOSHA_QUEUE = "miaosha.queue";
    public static final String TOPIC_EXCHANGE = "topicExchage";

    @Bean
    public Queue queue() {
        return new Queue(MIAOSHA_QUEUE, true);
    }




    @Bean
    public TopicExchange topicExchage(){
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    @Bean
    public Binding topicBinding1() {
        return BindingBuilder.bind(queue()).to(topicExchage()).with(MIAOSHA_QUEUE);
    }
}
