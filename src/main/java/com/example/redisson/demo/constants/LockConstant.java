package com.example.redisson.demo.constants;

/**
 * @program: redisson-demo
 * @description: LockConstant
 * @author: Lvlin.Lou
 * @create: 2021-10-29 10:44
 **/
public interface LockConstant {
    /**
     * 锁前缀
     */
    String LOCK_PREFIX = "redisson_fair_lock:%s";

    /**
     * 格式化
     * @param lockName
     * @return
     */
    static String format(String lockName) {
        return String.format(LockConstant.LOCK_PREFIX, lockName);
    }
}
