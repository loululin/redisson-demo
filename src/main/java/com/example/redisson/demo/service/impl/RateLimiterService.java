package com.example.redisson.demo.service.impl;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @program: redisson-demo
 * @description: RateLimiter
 * @author: Lvlin.Lou
 * @create: 2021-11-10 11:16
 **/
@Slf4j
@Service
public class RateLimiterService {


    @Resource
    private RedissonClient redisson;

    public void sendMsg(String phone) {
        if (StringUtils.isNotBlank(phone)) {
            RRateLimiter rateLimiter =
                    redisson.getRateLimiter("rateLimiter:" + phone);
            //每10秒产生1个令牌
            rateLimiter.trySetRate(RateType.OVERALL, 3, 10,
                    RateIntervalUnit.MINUTES);

            if (rateLimiter.tryAcquire(1)) {
                log.info("向手机:{}发送短信", phone);
            } else {
                log.info("10内不能重复向同一手机:{}发消息",phone);
            }
        }
    }

}
