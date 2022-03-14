package com.example.redisson.demo.controller;

import com.example.redisson.demo.enums.RedisDelayQueueEnum;
import com.example.redisson.demo.queue.RedisDelayQueueUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @program: redisson-demo
 * @description: RedisDelayQueueController
 * @author: Lvlin.Lou
 * @create: 2021-10-14 17:05
 **/
@RestController
public class RedisDelayQueueController {

    @Resource
    private RedisDelayQueueUtil redisDelayQueueUtil;

    public static AtomicLong count = new AtomicLong(0);


    /**
    * @Description: addQueue test
    * @Param: []
    * @return: void
    * @Author: Lvlin.Lou
    * @Date: 2021/10/28 11:35
    */
    @GetMapping("/addQueue")
    public void addQueue() {
        Map<String, String> map1 = new HashMap<>();
        map1.put("orderId", (100+count.getAndIncrement()) + "");
        map1.put("remark", "订单支付超时，自动取消订单");

        Map<String, String> map2 = new HashMap<>();
        map2.put("orderId", (200+count.getAndIncrement()) + "");
        map2.put("remark", "订单超时未评价，系统默认好评");

        // 添加订单支付超时，自动取消订单延迟队列。为了测试效果，延迟10秒钟
        redisDelayQueueUtil.addDelayQueue(count.getAndIncrement(), 50, TimeUnit.SECONDS, RedisDelayQueueEnum.ORDER_PAYMENT_TIMEOUT.getCode());
        // 订单超时未评价，系统默认好评。为了测试效果，延迟20秒钟
        redisDelayQueueUtil.addDelayQueue(count.getAndIncrement(), 2, TimeUnit.MINUTES, RedisDelayQueueEnum.ORDER_TIMEOUT_NOT_EVALUATED.getCode());

    }
}
