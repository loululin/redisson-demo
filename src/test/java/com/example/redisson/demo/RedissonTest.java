package com.example.redisson.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.connection.CRC16;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @program: redisson-demo
 * @description: RedissonTest
 * @author: Lvlin.Lou
 * @create: 2021-11-01 14:19
 **/
@SpringBootTest
@Slf4j
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    public void fairLockTest() throws InterruptedException {
        RLock fairLock = redissonClient.getFairLock("anyLock");
        boolean isLock = fairLock.tryLock(100,10, TimeUnit.SECONDS);
        if (isLock) {
            Thread.sleep(4000);
            fairLock.unlock();
        }
    }


    @Test
    public void testHashSlot(){
        String key = "test";
        int slot = CRC16.crc16(key.getBytes(StandardCharsets.UTF_8)) % 16384;
        log.info("key test slot is : {}",slot);

    }

    @Test
    public void countDownTest(){
        final CountDownLatch latch = new CountDownLatch(2);
        System.out.println("主线程开始执行…… ……");

        //第一个子线程执行
        ExecutorService es1 = Executors.newSingleThreadExecutor();

        es1.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    System.out.println("子线程："+Thread.currentThread().getName()+"执行");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                latch.countDown();
            }
        });

        es1.shutdown();

        ExecutorService es2 = Executors.newSingleThreadExecutor();

        es2.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    System.out.println("子线程："+Thread.currentThread().getName()+"执行");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                latch.countDown();
            }
        });


        es2.shutdown();
        System.out.println("等待两个线程执行完毕…… ……");
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("两个子线程都执行完毕，继续执行主线程");

    }
}
