package com.example.redisson.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @program: redisson-demo
 * @description: RedisUtilsTest
 * @author: Lvlin.Lou
 * @create: 2022-01-10 10:16
 **/
@SpringBootTest
@Slf4j
public class RedisUtilsTest {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;



    // 计算BigDecimal数据：计算规则：1.0 -（time时间戳小数点右移time时间戳长度的double数据）
    private BigDecimal getScoreByTime(long time) {
        return new BigDecimal("1.0").subtract(new BigDecimal(time*Math.pow(10, Math.negateExact(String.valueOf(time).length()))));
    }


    @Test
    public void setTest() throws InterruptedException {

        redisTemplate.opsForZSet().add("mytest","12",getScoreByTime(System.currentTimeMillis()).add(new BigDecimal(5)).doubleValue());
        redisTemplate.opsForZSet().add("test","12",new BigDecimal(5).doubleValue());
        redisTemplate.opsForZSet().add("test","15",new BigDecimal(6).doubleValue());

        Thread.sleep(3000);
        redisTemplate.opsForZSet().add("mytest","13",getScoreByTime(System.currentTimeMillis()).add(new BigDecimal(5)).doubleValue());
        redisTemplate.opsForZSet().add("test","13",new BigDecimal(5).doubleValue());
        Thread.sleep(3000);
        redisTemplate.opsForZSet().add("mytest","14",getScoreByTime(System.currentTimeMillis()).add(new BigDecimal(5)).doubleValue());
        redisTemplate.opsForZSet().add("test","14",new BigDecimal(5).doubleValue());
        redisTemplate.opsForZSet().add("mytest","15",getScoreByTime(System.currentTimeMillis()).add(new BigDecimal(6)).doubleValue());

    }






}
