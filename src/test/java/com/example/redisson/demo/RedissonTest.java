package com.example.redisson.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.connection.CRC16;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
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
}
