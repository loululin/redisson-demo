package com.example.redisson.demo.controller;

import com.example.redisson.demo.service.IBusinessService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: redisson-demo
 * @description: OversoldController
 * @author: Lvlin.Lou
 * @create: 2021-11-01 14:13
 **/
@RestController
public class OversoldController {

    private IBusinessService businessService;

    public OversoldController(IBusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping("/withoutLock")
    public void withoutLock() {
        businessService.businessWithoutLock();
    }

    @GetMapping("/withLock")
    public void withLock() {
        businessService.businessWithLock();
    }

    @GetMapping("/query")
    public String query() {
        return "剩余库存：" + businessService.getStock().get();
    }

    @GetMapping("/supply/{increment}")
    public Integer supply(@PathVariable(value = "increment") Integer increment) {
        return businessService.supplyStock(increment);
    }
}
