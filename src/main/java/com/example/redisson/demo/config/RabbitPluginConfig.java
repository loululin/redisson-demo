package com.example.redisson.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: redisson-demo
 * @description: RabbitPluginConfig
 * @author: Lvlin.Lou
 * @create: 2022-03-07 14:59
 **/
@Configuration
public class RabbitPluginConfig {

    private static final String EXCHANGE_NAME = "delayed_exchange";
    private static final String QUEUE_NAME = "delayed_queue";
    private static final String ROUTE_KEY = "delayed_key";
    /**
     * 交换机
     */
    @Bean
    CustomExchange exchange() {
        //通过x-delayed-type参数设置fanout /direct / topic / header 类型
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "topic");
        return new CustomExchange(EXCHANGE_NAME, "x-delayed-message",true, false,args);
    }

    /**
     * 队列
     */
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME,true,false,false);
    }

    /**
     * 将队列绑定到交换机
     */
    @Bean
    public Binding binding(CustomExchange exchange, Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(ROUTE_KEY)
                .noargs();
    }
}
