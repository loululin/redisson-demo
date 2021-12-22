package com.example.redisson.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @program: redisson-demo
 * @description: RedisDelayQueueEnum
 * @author: Lvlin.Lou
 * @create: 2021-10-14 16:54
 **/
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum RedisDelayQueueEnum {

    /**
     * ORDER_PAYMENT_TIMEOUT
     */
    ORDER_PAYMENT_TIMEOUT("ORDER_PAYMENT_TIMEOUT_LOU","订单支付超时，自动取消订单", "orderPaymentTimeout"),
    /**
     * ORDER_TIMEOUT_NOT_EVALUATED
     */
    ORDER_TIMEOUT_NOT_EVALUATED("ORDER_TIMEOUT_NOT_EVALUATED_LOU", "订单超时未评价，系统默认好评", "orderTimeoutNotEvaluated");

    /**
     * 延迟队列 Redis Key
     */
    private String code;

    /**
     * 中文描述
     */
    private String name;

    /**
     * 延迟队列具体业务实现的 Bean
     * 可通过 Spring 的上下文获取
     */
    private String beanId;
}
