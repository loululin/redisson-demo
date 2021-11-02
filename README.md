# redisson-demo
redis 延迟队列和分布式锁
##  redisson配置及使用（集群版）

### 1. pom

> springboot版本依赖关系参考https://github.com/redisson/redisson/tree/master/redisson-spring-boot-starter  
>
> 我的springboot版本为2.3.2.RELEASE  故选择redisson-spring-data-23

```xml
<redisson.version>3.16.1</redisson.version>


        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson-spring-boot-starter</artifactId>
            <version>${redisson.version}</version>
            <exclusions>
                <exclusion>
                    <groupId>org.redisson</groupId>
                    <artifactId>redisson-spring-data-25</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson-spring-data-23</artifactId>
            <version>${redisson.version}</version>
        </dependency>
```



### 2. nacos

> 记得修改password和nodeAddresses中的值
>
> 参考：https://github.com/redisson/redisson/tree/master/redisson-spring-boot-starter

```yaml
spring:
  redis:
    database: 1
    cluster:
      nodes:
        - 10.113.75.151:26379
    password: 123456
    timeout: 10000
    lettuce:
      pool:
        max-idle: 10
        min-idle: 0
        max-active: 10
        max-wait: -1
    redisson:
      file: classpath:redisson.yaml
      config: |
        clusterServersConfig:
          idleConnectionTimeout: 10000
          connectTimeout: 10000
          timeout: 3000
          retryAttempts: 3
          retryInterval: 1500
          failedSlaveReconnectionInterval: 3000
          failedSlaveCheckInterval: 60000
          password: 123456
          subscriptionsPerConnection: 5
          clientName: null
          loadBalancer: !<org.redisson.connection.balancer.RoundRobinLoadBalancer> {}
          subscriptionConnectionMinimumIdleSize: 1
          subscriptionConnectionPoolSize: 50
          slaveConnectionMinimumIdleSize: 24
          slaveConnectionPoolSize: 64
          masterConnectionMinimumIdleSize: 24
          masterConnectionPoolSize: 64
          readMode: "SLAVE"
          subscriptionMode: "SLAVE"
          nodeAddresses:
          - "redis://10.113.75.151:26379"
          scanInterval: 2000
          pingConnectionInterval: 1000
          keepAlive: false
          tcpNoDelay: false
        threads: 16
        nettyThreads: 32
        codec: !<org.redisson.codec.MarshallingCodec> {}
        transportMode: "NIO" 
redis:
  cache:
    expire:
      minutes: 60
```

### 3.分布式锁

```java
package com.vulcan.service.designer.controller;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @program: vulcan-project
 * @description: RedissonController
 * @author: Lvlin.Lou
 * @create: 2021-09-14 08:51
 **/
@Slf4j
@RestController
@RequestMapping("/redisson")
public class RedissonController {

    @Resource
    private RedissonClient redissonClient;

    @GetMapping(value = "/test/{key}")
    public String redissonTest(@PathVariable("key") String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            lock.lock();
            Thread.sleep(10000);
        } catch (Exception e) {
            log.error("redissonTest get error",e);
        } finally {
            lock.unlock();
        }
        return "已解锁";
    }
}

```

### 4.延迟队列

#### 4.1 工具类封装

```java
package com.example.redisson.demo.queue;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @program: redisson-demo
 * @description: RedisDelayQueueUtil
 * @author: Lvlin.Lou
 * @create: 2021-10-14 16:37
 **/
@Slf4j
@Component
public class RedisDelayQueueUtil {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 添加延迟队列
     * @param value     队列值
     * @param delay     延迟时间
     * @param timeUnit  时间单位
     * @param queueCode 队列键
     * @param <T>
     */
    public <T> boolean addDelayQueue(@NonNull T value, @NonNull long delay, @NonNull TimeUnit timeUnit, @NonNull String queueCode) {
        if (StringUtils.isBlank(queueCode) || Objects.isNull(value)) {
            return false;
        }
        try {
            RBlockingDeque<Object> blockingDeque = redissonClient.getBlockingDeque(queueCode);
            RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
            delayedQueue.offer(value, delay, timeUnit);
            //delayedQueue.destroy();
            log.info("(添加延时队列成功) 队列键：{}，队列值：{}，延迟时间：{}", queueCode, value, timeUnit.toSeconds(delay) + "秒");
        } catch (Exception e) {
            log.error("(添加延时队列失败) {}", e.getMessage());
            throw new RuntimeException("(添加延时队列失败)");
        }
        return true;
    }

    /**
     * 获取延迟队列
     * @param queueCode
     * @param <T>
     */
    public <T> T getDelayQueue(@NonNull String queueCode) throws InterruptedException {
        if (StringUtils.isBlank(queueCode)) {
            return null;
        }
        RBlockingDeque<Map> blockingDeque = redissonClient.getBlockingDeque(queueCode);
        T value = (T) blockingDeque.poll();
        return value;
    }

    /**
     * 删除指定队列中的消息
     *
     * @param o         指定删除的消息对象队列值(同队列需保证唯一性)
     * @param queueCode 指定队列键
     */
    public boolean removeDelayedQueue(@NonNull Object o, @NonNull String queueCode) {
        if (StringUtils.isBlank(queueCode) || Objects.isNull(o)) {
            return false;
        }
        RBlockingDeque<Object> blockingDeque = redissonClient.getBlockingDeque(queueCode);
        RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
        boolean flag = delayedQueue.remove(o);
        //delayedQueue.destroy();
        return flag;
    }

}

```

#### 4.2 定义延迟队列执行器

```java
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
```

#### 4.3 实现延迟队列执行器

- OrderPaymentTimeout：订单支付超时延迟队列处理类
- 启动类开启异步处理注解：@EnableAsync

```java
package com.example.redisson.demo.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @program: redisson-demo
 * @description: OrderPaymentTimeout
 * @author: Lvlin.Lou
 * @create: 2021-10-14 16:58
 **/
@Component
@Slf4j
public class OrderPaymentTimeout implements RedisDelayQueueHandler<Map> {
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
        log.info("(收到订单支付超时延迟消息) {}", map);
    }
}

```



- OrderTimeoutNotEvaluated：订单超时未评价延迟队列处理类

```java
package com.example.redisson.demo.consumer;

import lombok.extern.slf4j.Slf4j;
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
```

#### 4.4  创建延迟队列业务枚举

```java
package com.example.redisson.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @program: redisson-demo
 * @description: RedisDelayQueueEnum
 * @author: Lvlin.Lou
 * @create: 2021-10-14 16:54
 **/
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum RedisDelayQueueEnum {

    /**
     * ORDER_PAYMENT_TIMEOUT
     */
    ORDER_PAYMENT_TIMEOUT("ORDER_PAYMENT_TIMEOUT","订单支付超时，自动取消订单", "orderPaymentTimeout"),
    /**
     * ORDER_TIMEOUT_NOT_EVALUATED
     */
    ORDER_TIMEOUT_NOT_EVALUATED("ORDER_TIMEOUT_NOT_EVALUATED", "订单超时未评价，系统默认好评", "orderTimeoutNotEvaluated");

    /**
     * 延迟队列 Redis Key
     */
    private String code;

    /**
     * 中文描述
     */
    private String name;

    /**
     * 延迟队列具体业务实现的 Bean
     * 可通过 Spring 的上下文获取
     */
    private String beanId;
}

```

#### 4.5 创建延迟队列消费线程，项目启动完成后开启

```java
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


```

SpringUtils

```java
package com.example.redisson.demo.utils;

import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * spring工具类 方便在非spring管理环境中获取bean
 * 
 * @author jcy
 */
@Component
public final class SpringUtils implements BeanFactoryPostProcessor
{
    /** Spring应用上下文环境 */
    private static ConfigurableListableBeanFactory beanFactory;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
    {
        SpringUtils.beanFactory = beanFactory;
    }

    /**
     * 获取对象
     *
     * @param name
     * @return Object 一个以所给名字注册的bean的实例
     * @throws BeansException
     *
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) throws BeansException
    {
        return (T) beanFactory.getBean(name);
    }

    /**
     * 获取类型为requiredType的对象
     *
     * @param clz
     * @return
     * @throws BeansException
     *
     */
    public static <T> T getBean(Class<T> clz) throws BeansException
    {
        T result = (T) beanFactory.getBean(clz);
        return result;
    }

    /**
     * 如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true
     *
     * @param name
     * @return boolean
     */
    public static boolean containsBean(String name)
    {
        return beanFactory.containsBean(name);
    }

    /**
     * 判断以给定名字注册的bean定义是一个singleton还是一个prototype。 如果与给定名字相应的bean定义没有被找到，将会抛出一个异常（NoSuchBeanDefinitionException）
     *
     * @param name
     * @return boolean
     * @throws NoSuchBeanDefinitionException
     *
     */
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException
    {
        return beanFactory.isSingleton(name);
    }

    /**
     * @param name
     * @return Class 注册对象的类型
     * @throws NoSuchBeanDefinitionException
     *
     */
    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException
    {
        return beanFactory.getType(name);
    }

    /**
     * 如果给定的bean名字在bean定义中有别名，则返回这些别名
     *
     * @param name
     * @return
     * @throws NoSuchBeanDefinitionException
     *
     */
    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException
    {
        return beanFactory.getAliases(name);
    }

    /**
     * 获取aop代理对象
     * 
     * @param invoker
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAopProxy(T invoker)
    {
        return (T) AopContext.currentProxy();
    }
}

```



#### 4.6 测试接口，模拟添加延迟队列

```java
package com.example.redisson.demo.controller;

import com.example.redisson.demo.enums.RedisDelayQueueEnum;
import com.example.redisson.demo.queue.RedisDelayQueueUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    
    /**
    * @Description: addQueue test
    * @Param: []
    * @return: void
    * @Author: Lvlin.Lou
    * @Date: 2021/10/28 11:35
    */
    @PostMapping("/addQueue")
    public void addQueue() {
        Map<String, String> map1 = new HashMap<>();
        map1.put("orderId", "100");
        map1.put("remark", "订单支付超时，自动取消订单");

        Map<String, String> map2 = new HashMap<>();
        map2.put("orderId", "200");
        map2.put("remark", "订单超时未评价，系统默认好评");

        // 添加订单支付超时，自动取消订单延迟队列。为了测试效果，延迟10秒钟
        redisDelayQueueUtil.addDelayQueue(map1, 10, TimeUnit.SECONDS, RedisDelayQueueEnum.ORDER_PAYMENT_TIMEOUT.getCode());
        // 订单超时未评价，系统默认好评。为了测试效果，延迟20秒钟
        redisDelayQueueUtil.addDelayQueue(map2, 20, TimeUnit.SECONDS, RedisDelayQueueEnum.ORDER_TIMEOUT_NOT_EVALUATED.getCode());

    }
}

```

#### 4.7 测试

请求：POST http://localhost:10001/addQueue

结果：

```properties
2021-11-02 10:00:47.331  INFO 21264 --- [io-10001-exec-2] c.e.r.demo.queue.RedisDelayQueueUtil     : (添加延时队列成功) 队列键：ORDER_PAYMENT_TIMEOUT，队列值：{orderId=100, remark=订单支付超时，自动取消订单}，延迟时间：10秒2021-11-02 10:00:47.363  INFO 21264 --- [io-10001-exec-2] c.e.r.demo.queue.RedisDelayQueueUtil     : (添加延时队列成功) 队列键：ORDER_TIMEOUT_NOT_EVALUATED，队列值：{orderId=200, remark=订单超时未评价，系统默认好评}，延迟时间：20秒2021-11-02 10:00:57.422  INFO 21264 --- [         task-2] c.e.r.demo.consumer.OrderPaymentTimeout  : (收到订单支付超时延迟消息) {orderId=100, remark=订单支付超时，自动取消订单}2021-11-02 10:01:07.375  INFO 21264 --- [         task-3] c.e.r.d.c.OrderTimeoutNotEvaluated       : (收到订单超时未评价延迟消息) {orderId=200, remark=订单超时未评价，系统默认好评}
```



#### 附application.yml

```yaml
# Spring
spring:
  application:
    # 应用名称
    name: redisson-demo
  profiles:
    # 环境配置
    active: test
  main:
    allow-bean-definition-overriding: true

server:
  port: 10001
#  port: ${random.int[10000,19999]}

```

### 5 分页式锁解决超卖

#### 5.1 自定义Lock注解

```java
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

```

#### 5.2 Lock注解切面
> 注释比较重要

```java
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

```

#### 5.3 LockConstant

```java
package com.example.redisson.demo.constants;

/**
 * @program: redisson-demo
 * @description: LockConstant
 * @author: Lvlin.Lou
 * @create: 2021-10-29 10:44
 **/
public interface LockConstant {
    /**
     * 锁前缀
     */
    String LOCK_PREFIX = "redisson_fair_lock:%s";

    /**
     * 格式化
     * @param lockName
     * @return
     */
    static String format(String lockName) {
        return String.format(LockConstant.LOCK_PREFIX, lockName);
    }
}

```

#### 5.4 service

**service**

```java
package com.example.redisson.demo.service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: redisson-demo
 * @description: IBusinessService
 * @author: Lvlin.Lou
 * @create: 2021-10-29 16:22
 **/
public interface IBusinessService {

    /**
     * 无锁业务
     */
    void businessWithoutLock();

    /**
     * 带锁业务
     */
    void businessWithLock();

    /**
     * 获取库存
     * @return
     */
    AtomicInteger getStock();

    /**
    * @Description: 补充库存
    * @Param: [increment]
    * @return: java.lang.Integer
    * @Author: Lvlin.Lou
    * @Date: 2021/11/1 17:43
    */
    Integer supplyStock(Integer increment);
}

```

**serviceImpl**

```java
package com.example.redisson.demo.service.impl;

import com.example.redisson.demo.annotation.Lock;
import com.example.redisson.demo.service.IBusinessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: redisson-demo
 * @description: BusinessServiceImpl
 * @author: Lvlin.Lou
 * @create: 2021-11-01 14:08
 **/
@Service
@Slf4j
public class BusinessServiceImpl implements IBusinessService {

    // 设置库存为10，AtomicInteger的所有方法都具有原子性
    private AtomicInteger stock = new AtomicInteger(30);

    @Resource
    private BusinessServiceImpl self;
    /**
     * 无锁业务
     */
    @Override
    public void businessWithoutLock() {
        if (stock.get() > 0) {
            try {
                // 1ms 秒执行业务逻辑。当然这是假设，1ms肯定执行不完的。以此为例子，就算是业务很快完成，也会出现超卖
                Thread.sleep(1);
                stock.decrementAndGet();
                log.info("{}: 成功购买", Thread.currentThread().getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            log.info("{}: 抱歉，库存不足", Thread.currentThread().getName());
        }

    }

    /**
     * 带锁业务
     */
    @Override
    @Lock(name = "buckleInventoryLock")
    public void businessWithLock() {
        self.businessWithoutLock();
    }

    /**
     * 获取库存
     *
     * @return
     */
    @Override
    public AtomicInteger getStock() {
        return this.stock;
    }

    /**
     * @param increment
     * @Description: 补充库存
     * @Param: [increment]
     * @return: java.lang.Integer
     * @Author: Lvlin.Lou
     * @Date: 2021/11/1 14:07
     */
    @Override
    public Integer supplyStock(Integer increment) {
        return this.stock.addAndGet(increment);
    }
}

```

#### 5.5 controller

```java
package com.example.redisson.demo.controller;

import com.example.redisson.demo.service.IBusinessService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: redisson-demo
 * @description: OversoldController
 * @author: Lvlin.Lou
 * @create: 2021-11-01 14:13
 **/
@RestController
public class OversoldController {

    private IBusinessService businessService;

    public OversoldController(IBusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping("/withoutLock")
    public void withoutLock() {
        businessService.businessWithoutLock();
    }

    @GetMapping("/withLock")
    public void withLock() {
        businessService.businessWithLock();
    }

    @GetMapping("/query")
    public String query() {
        return "剩余库存：" + businessService.getStock().get();
    }

    @GetMapping("/supply/{increment}")
    public Integer supply(@PathVariable(value = "increment") Integer increment) {
        return businessService.supplyStock(increment);
    }
}

```

#### 5.6 测试

查询库存：http://localhost:10001/query

ab压力测试：

```yaml
ab -n 1000 -c 1000 http://localhost:10001/withoutLock  # 发生超卖
ab -n 1000 -c 1000 http://localhost:10001/withLock   # 正常销售
```

