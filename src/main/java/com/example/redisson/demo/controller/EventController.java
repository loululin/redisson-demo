package com.example.redisson.demo.controller;

import com.example.redisson.demo.event.NoticeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @program: redisson-demo
 * @description: EventController
 * @author: Lvlin.Lou
 * @create: 2022-03-14 13:35
 **/
@RestController
@RequestMapping("/event")
@Slf4j
public class EventController {
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @GetMapping("/notice/{message}")
    public void notice(@PathVariable(name = "message") String message) {
        log.info("begin >>>>>>");
        applicationEventPublisher.publishEvent(new NoticeEvent(message));
        log.info("end <<<<<<");
    }

}
