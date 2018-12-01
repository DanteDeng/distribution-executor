package com.nature.distribution.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 应用上工具
 * @author nature
 * @version 1.0.0
 * @since 2018/11/22 10:12
 */
public class ApplicationUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationUtil.class);
    /**
     * 机器编号缓存key
     */
    private static final String MACHINE_NO = "payment:reconciliation:machine:no";
    /**
     * 机器编号前缀
     */
    private static final String PREFIX = "machine00";

    /**
     * 标记应用的唯一标识
     * 一个应用实例只生成一次并且保证唯一
     */
    private static String applicationUniqueKey;

    /**
     * 同步安全锁
     */
    private static Lock lock = new ReentrantLock();

    /**
     * 生成应用实例唯一标识
     * @return 应用实例唯一标识
     */
    public static String getApplicationUniqueKey() {
        if (applicationUniqueKey == null) {
            lock.lock();
            try {
                if (applicationUniqueKey == null) {
                    applicationUniqueKey = PREFIX + CacheUtil.incrementAndGet(MACHINE_NO, 1);
                    LOG.info(String.format("application unique key generated %s", applicationUniqueKey));
                }
            } finally {
                lock.unlock();
            }
        }
        return applicationUniqueKey;
    }
}
