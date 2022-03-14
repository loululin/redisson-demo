package com.example.redisson.demo.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @program: redisson-demo
 * @description: NoticeListener
 * @author: Lvlin.Lou
 * @create: 2022-03-14 13:32
 **/
@Slf4j
@Component
public class NoticeListener implements ApplicationListener<NoticeEvent> {


    @Async
    @Override
    public void onApplicationEvent(NoticeEvent noticeEvent) {

        log.info("listener get event, sleep 2 second...");


        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error("NoticeListener get error: ",e);
        }

        log.info("event message is : {}", noticeEvent.getMessage());
    }
}
