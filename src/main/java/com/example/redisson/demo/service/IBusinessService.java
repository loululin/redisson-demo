package com.example.redisson.demo.service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: redisson-demo
 * @description: IBusinessService
 * @author: Lvlin.Lou
 * @create: 2021-10-29 16:22
 **/
public interface IBusinessService {

    /**
     * 无锁业务
     */
    void businessWithoutLock();

    /**
     * 带锁业务
     */
    void businessWithLock();

    /**
     * 获取库存
     * @return
     */
    AtomicInteger getStock();

    /**
    * @Description: 补充库存
    * @Param: [increment]
    * @return: java.lang.Integer
    * @Author: Lvlin.Lou
    * @Date: 2021/11/1 17:43
    */
    Integer supplyStock(Integer increment);
}
