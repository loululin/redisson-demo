package com.example.redisson.demo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @program: redisson-demo
 * @description: Lock
 * @author: Lvlin.Lou
 * @create: 2021-10-29 16:16
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Lock {
    /**
     * 锁的名称
     * @return
     */
    String name() default "";

    /**
     * 等待获取锁的最长时间
     * @return
     */
    long waitTime() default 4000L;

    /**
     * 授予锁后持有锁的最长时间。该时间根据业务也行量而定
     * @return
     */
    long leaseTime() default 2000L;

    /**
     * 时间单位
     * @return
     */
    TimeUnit unit() default TimeUnit.SECONDS;
}
