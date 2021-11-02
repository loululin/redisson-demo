package com.example.redisson.demo.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @program: redisson-demo
 * @description: OrderTimeoutNotEvaluated
 * @author: Lvlin.Lou
 * @create: 2021-10-14 16:59
 **/
@Component
@Slf4j
public class OrderTimeoutNotEvaluated implements RedisDelayQueueHandler<Map> {
    /**
     * @Description: execute
     * @Param: [t]
     * @return: void
     * @Author: Lvlin.Lou
     * @Date: 2021/10/14 16:57
     */
    @Override
    @Async
    public void execute(Map map) {
        log.info("(收到订单超时未评价延迟消息) {}", map);
    }
}
