package com.example.redisson.demo.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @program: redisson-demo
 * @description: RabbitSender
 * @author: Lvlin.Lou
 * @create: 2022-03-07 14:26
 **/
@Component
@Slf4j
public class RabbitSender {

    @Value("${mq.queueBinding.exchange.name}")
    private String exchangeName;

    @Value("${mq.queueBinding.key}")
    private String key;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(String msg){
        log.info("RabbitSender.send() msg = {}",msg);
        rabbitTemplate.convertAndSend(exchangeName,key,msg);
    }



    private static final String ROUTE_KEY = "delayed_key";
    private static final String EXCHANGE_NAME = "delayed_exchange";
    /**
     * @param msg 消息
     * @param delay   延时时间，秒
     */
    public void send2(String msg,int delay){
        log.info("RabbitSender.send() msg = {}",msg);
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTE_KEY, msg, message ->{
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);  //消息持久化
            message.getMessageProperties().setDelay(delay * 1000);   // 单位为毫秒
            return message;
        });
    }

}
