package com.example.redisson.demo.controller;

import com.example.redisson.demo.rabbit.RabbitSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: redisson-demo
 * @description: RabbitMqController
 * @author: Lvlin.Lou
 * @create: 2022-03-07 14:29
 **/
@RestController
public class RabbitMqController {

    @Autowired
    private RabbitSender rabbitSender;


    @GetMapping
    public void test(@RequestParam String msg){
        rabbitSender.send(msg);
    }

    @GetMapping("/test2/{msg}/{delay}")
    public void test2(@PathVariable("msg") String msg, @PathVariable("delay")int delay){
        rabbitSender.send2(msg,delay);
    }
}
