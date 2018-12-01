package com.nature.distribution.util;

/**
 * @author nature
 * @date 2018/10/29 14:51
 * @description 锁定key工具
 */
public class LockKeyUtil {

    private static final String PREFIX = "reconciliation:lock:";

    private static final String SEPARATOR = ":";

    private static final String OPERATION_IMPORT = "operation:import";

    private static final String TASK = "task";

    /**
     * 根据传入的参数生成锁定key
     * @param objects 传入的参数
     * @return 锁定key
     */
    public static String genLockKey(Object... objects) {
        String lockKey = PREFIX;
        int length = objects.length;
        for (int i = 0; i < length; i++) {
            Object object = objects[i];
            if (object != null) {
                lockKey = lockKey + object;
                if (i < length - 1) {
                    lockKey = lockKey + SEPARATOR;
                }
            }
        }
        return lockKey;
    }

    public static String genImportDataLockKey(String key) {

        return genLockKey(OPERATION_IMPORT, key);
    }

    public static String genTaskLockKey(String key) {

        return genLockKey(TASK, key);
    }

}
