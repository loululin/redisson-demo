package com.example.redisson.demo.queue;

import com.example.redisson.demo.consumer.RedisDelayQueueHandler;
import com.example.redisson.demo.enums.RedisDelayQueueEnum;
import com.example.redisson.demo.utils.SpringUtils;
import jodd.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @program: redisson-demo
 * @description: RedisDelayQueueRunner 启动延迟队列
 * @author: Lvlin.Lou
 * @create: 2021-10-14 17:00
 **/
@Slf4j
@Component
public class RedisDelayQueueRunner implements CommandLineRunner {

    @Resource
    private RedisDelayQueueUtil redisDelayQueueUtil;

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    ThreadPoolExecutor executorService = new ThreadPoolExecutor(3, 5, 30, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(1000),new ThreadFactoryBuilder().setNameFormat("order-delay-%d").get());

    @Override
    public void run(String... args) {
        threadPoolTaskExecutor.execute(() -> {
            while (true){
                try {
                    RedisDelayQueueEnum[] queueEnums = RedisDelayQueueEnum.values();
                    for (RedisDelayQueueEnum queueEnum : queueEnums) {
                        Object value = redisDelayQueueUtil.getDelayQueue(queueEnum.getCode());
                        if (value != null) {
                            RedisDelayQueueHandler<Object> redisDelayQueueHandler = SpringUtils.getBean(queueEnum.getBeanId());
                            executorService.execute(() -> {
                                redisDelayQueueHandler.execute(value);});
                        }
                    }
                } catch (InterruptedException e) {
                    log.error("(Redission延迟队列监测异常中断) {}", e.getMessage());
                }
            }
        });
        log.info("(Redission延迟队列监测启动成功)");
    }
}
