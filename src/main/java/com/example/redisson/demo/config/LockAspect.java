package com.example.redisson.demo.config;

import com.example.redisson.demo.annotation.Lock;
import com.example.redisson.demo.constants.LockConstant;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * @program: redisson-demo
 * @description: LockAspect
 * @author: Lvlin.Lou
 * @create: 2021-10-29 16:20
 **/
@Slf4j
@Component
@Aspect
@Order(1)
public class LockAspect {

    @Resource
    private RedissonClient redisson;

    @Pointcut("@annotation(com.example.redisson.demo.annotation.Lock)")
    public void point() {}

    @Around("point()")
    public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
        Lock lock = getLock(joinPoint);
        // 目标方法参数
        Object[] args = joinPoint.getArgs();
//        RLock fairLock = redisson.getFairLock(LockConstant.format(lock.name()));
        RLock fairLock = redisson.getLock(LockConstant.format(lock.name()));
//        boolean isLock = fairLock.tryLock(lock.waitTime(), lock.leaseTime(), lock.unit());
        boolean isLock = fairLock.tryLock();
        if (isLock) {
            try {
                return joinPoint.proceed(args);
            } finally {
                fairLock.unlock();
            }
        }
        log.info("{}: 系统繁忙，请重试", Thread.currentThread().getName());
        // 这里为了方便查看日志，把下面一行注释掉了，实际还需要抛个异常就行
//        throw new RuntimeException("系统繁忙，请重试");
        return null;
    }

    /**
     * 获取 Lock 注解
     * @param joinPoint
     * @return
     */
    private Lock getLock(ProceedingJoinPoint joinPoint) {
        // 获得方法署名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获得目标方法
        Method method = signature.getMethod();
        // 拿到目标方法上的注解 Lock 并返回
        return method.getAnnotation(Lock.class);
    }
}
