package com.nature.distribution.util;

import java.util.concurrent.TimeUnit;

/**
 * 休眠工具类
 * @author nature
 * @version 1.0.0
 * @since 2018/11/22 10:05
 */
public class SleepUtil {

    /**
     * 睡眠指定毫秒数
     * @param millis 毫秒
     */
    public static void sleepMillis(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 睡眠指定秒数
     * @param seconds 秒
     */
    public static void sleepSeconds(long seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
