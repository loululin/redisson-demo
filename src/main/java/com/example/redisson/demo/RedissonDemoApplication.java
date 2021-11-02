package com.example.redisson.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
@EnableAsync
@SpringBootApplication
public class RedissonDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedissonDemoApplication.class, args);
    }

}
