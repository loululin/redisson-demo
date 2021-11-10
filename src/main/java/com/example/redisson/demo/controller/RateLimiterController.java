package com.example.redisson.demo.controller;

import com.example.redisson.demo.service.impl.RateLimiterService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @program: redisson-demo
 * @description: RateLimiterController
 * @author: Lvlin.Lou
 * @create: 2021-11-10 11:20
 **/
@RestController
public class RateLimiterController {

    @Resource
    private RateLimiterService rateLimiterService;


    @GetMapping("/send-msg/{phone}")
    public void sendMsg(@PathVariable("phone") String phone) {
            rateLimiterService.sendMsg(phone);
    }

}
