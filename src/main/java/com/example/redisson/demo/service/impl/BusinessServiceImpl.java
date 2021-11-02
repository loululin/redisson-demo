package com.example.redisson.demo.service.impl;

import com.example.redisson.demo.annotation.Lock;
import com.example.redisson.demo.service.IBusinessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: redisson-demo
 * @description: BusinessServiceImpl
 * @author: Lvlin.Lou
 * @create: 2021-11-01 14:08
 **/
@Service
@Slf4j
public class BusinessServiceImpl implements IBusinessService {

    // 设置库存为10，AtomicInteger的所有方法都具有原子性
    private AtomicInteger stock = new AtomicInteger(30);

    @Resource
    private BusinessServiceImpl self;
    /**
     * 无锁业务
     */
    @Override
    public void businessWithoutLock() {
        if (stock.get() > 0) {
            try {
                // 1ms 秒执行业务逻辑。当然这是假设，1ms肯定执行不完的。以此为例子，就算是业务很快完成，也会出现超卖
                Thread.sleep(1);
                stock.decrementAndGet();
                log.info("{}: 成功购买", Thread.currentThread().getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            log.info("{}: 抱歉，库存不足", Thread.currentThread().getName());
        }

    }

    /**
     * 带锁业务
     */
    @Override
    @Lock(name = "buckleInventoryLock")
    public void businessWithLock() {
        self.businessWithoutLock();
    }

    /**
     * 获取库存
     *
     * @return
     */
    @Override
    public AtomicInteger getStock() {
        return this.stock;
    }

    /**
     * @param increment
     * @Description: 补充库存
     * @Param: [increment]
     * @return: java.lang.Integer
     * @Author: Lvlin.Lou
     * @Date: 2021/11/1 14:07
     */
    @Override
    public Integer supplyStock(Integer increment) {
        return this.stock.addAndGet(increment);
    }
}
