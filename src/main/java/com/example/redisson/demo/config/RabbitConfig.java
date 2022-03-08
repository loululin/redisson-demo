package com.example.redisson.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: redisson-demo
 * @description: RabbitConfig
 * @author: Lvlin.Lou
 * @create: 2022-03-07 14:17
 **/
@Configuration
public class RabbitConfig {


    //业务队列配置
    @Value("${mq.queueBinding.queue}")
    private String queueName;
    @Value("${mq.queueBinding.exchange.name}")
    private String exchangeMame;
    @Value("${mq.queueBinding.key}")
    private String key;
    //死信队列配置
    @Value("${mq.queueBinding.exchange.dlTopicExchange}")
    private String dlTopicExchange;
    @Value("${mq.queueBinding.dlRoutingKey}")
    private String dlRoutingKey;
    @Value("${mq.queueBinding.dlQueue}")
    private String dlQueue;
    //创建死信交换机
    @Bean
    public TopicExchange dlTopicExchange(){
        return new TopicExchange(dlTopicExchange,true,false);
    }
    //创建死信队列
    @Bean
    public Queue dlQueue(){
        return new Queue(dlQueue,true);
    }

    //死信队列与死信交换机进行绑定
    @Bean
    public Binding BindingErrorQueueAndExchange(Queue dlQueue, TopicExchange dlTopicExchange){
        return BindingBuilder.bind(dlQueue).to(dlTopicExchange).with(dlRoutingKey);
    }

    private final String dle = "x-dead-letter-exchange";
    private final String dlk = "x-dead-letter-routing-key";
    private final String ttl = "x-message-ttl";


    //创建业务队列
    @Bean
    public Queue payQueue(){
        Map<String,Object> params = new HashMap<>();
        //设置队列的过期时间
        params.put(ttl,10000);
        //声明当前队列绑定的死信交换机
        params.put(dle,dlTopicExchange);
        //声明当前队列的死信路由键
        params.put(dlk,dlRoutingKey);
        return QueueBuilder.durable(queueName).withArguments(params).build();
    }

    //创建业务交换机
    @Bean
    public TopicExchange payTopicExchange(){
        return new TopicExchange(exchangeMame,true,false);
    }

    //业务队列与业务交换机进行绑定
    @Bean
    public Binding BindingPayQueueAndPayTopicExchange(Queue payQueue, TopicExchange payTopicExchange){
        return BindingBuilder.bind(payQueue).to(payTopicExchange).with(key);
    }
}
