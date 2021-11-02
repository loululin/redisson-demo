package com.example.redisson.demo.consumer;

/**
 * @program: redisson-demo
 * @description: RedisDelayQueueHandler
 * @author: Lvlin.Lou
 * @create: 2021-10-14 16:56
 **/
public interface RedisDelayQueueHandler<T> {

    /**
    * @Description: execute
    * @Param: [t]
    * @return: void
    * @Author: Lvlin.Lou
    * @Date: 2021/10/14 16:57
    */
    void execute(T t);
}
