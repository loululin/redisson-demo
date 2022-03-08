package com.example.redisson.demo.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @program: redisson-demo
 * @description: RabbitReceiver
 * @author: Lvlin.Lou
 * @create: 2022-03-07 14:28
 **/
@Component
@Slf4j
public class RabbitReceiver {

    //消费死信队列的消息
    @RabbitListener(queues = "${mq.queueBinding.dlQueue}")
    public void infoConsumption(String data) throws Exception {
        log.info("收到信息:{}",data);
        log.info("然后进行一系列逻辑处理 Thanks♪(･ω･)ﾉ");
    }

    @RabbitListener(queues = "delayed_queue")
    public void infoNewConsumption(String data) throws Exception {
        log.info("收到信息:{}",data);
        log.info("然后进行一系列逻辑处理 Thanks♪(･ω･)ﾉ");
    }
}
