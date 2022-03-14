package com.example.redisson.demo.event;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

/**
 * @program: redisson-demo
 * @description: NoticeEvent
 * @author: Lvlin.Lou
 * @create: 2022-03-14 13:28
 **/
@Slf4j
@Getter
public class NoticeEvent extends ApplicationEvent {


    private String message;

    public NoticeEvent(String message) {
        super(message);

        this.message = message;

        log.info("add event message success,message is : {}",message);
    }
}
