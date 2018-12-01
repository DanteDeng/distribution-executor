package com.nature.distribution.util;

import com.nature.distribution.model.Keyable;

/**
 * 生成规则：平台->金额类型->产品编号->账户编号
 * 体现形式：名称包括数据类型，顾名思义~~~
 * @author nature
 * @version 1.0.0
 * @since 2018/11/22 10:04
 */
public class TaskKeyUtil {

    /**
     * 分隔符
     */
    private static final String SEPARATOR = ":";
    /**
     * 基础前缀
     */
    private static final String BASE_PREFIX = "reconciliation:";
    /**
     * 锁定key前缀
     */
    private static final String LOCK_PREFIX = "lock:";

    /**
     * 数据总数
     */
    private static final String DATA_TOTAL = "data:total:";

    /**
     * 任务总数
     */
    private static final String TASK_TOTAL = "task:total:";

    /**
     * 任务编号
     */
    private static final String TASK_NO = "task:no:";

    /**
     * 停止标记
     */
    private static final String STOP_FLAG = "stop:flag:";

    /**
     * 正常处理的数据下标
     */
    private static final String NORMAL_INDEX_SET = "set:normal:index:";

    /**
     * 异常处理的数据下标
     */
    private static final String ERROR_INDEX_SET = "set:error:index:";

    /**
     * 任务信息map
     */
    private static final String TASK_INFO_MAP = "map:task:info:";

    /**
     * 机器信息map
     */
    private static final String MACHINE_INFO_MAP = "map:machine:info:";

    /**
     * 数据总数key
     * @param keyable 可转换为key的参数
     * @return 数据总数key
     */
    public static String dataTotal(Keyable keyable) {
        return BASE_PREFIX + DATA_TOTAL + keyable.genKey();
    }

    /**
     * 任务总数key
     * @param keyable 可转换为key的参数
     * @return 任务总数key
     */
    public static String taskTotal(Keyable keyable) {
        return BASE_PREFIX + TASK_TOTAL + keyable.genKey();
    }

    /**
     * 任务编号key
     * @param keyable 可转换为key的参数
     * @return 任务编号key
     */
    public static String taskNo(Keyable keyable) {
        return BASE_PREFIX + TASK_NO + keyable.genKey();
    }

    /**
     * 停止标记key
     * @param keyable 可转换为key的参数
     * @return 停止标记key
     */
    public static String stopFlag(Keyable keyable) {
        return BASE_PREFIX + STOP_FLAG + keyable.genKey();
    }

    /**
     * 正常处理的数据下标集合
     * @param keyable 可转换为key的参数
     * @param taskNo  任务编号
     * @return 正常处理的数据下标集合
     */
    public static String normalIndexSet(Keyable keyable, int taskNo) {
        return BASE_PREFIX + NORMAL_INDEX_SET + keyable.genKey() + SEPARATOR + taskNo;
    }

    /**
     * 异常处理的数据下标集合
     * @param keyable 可转换为key的参数
     * @param taskNo  任务编号
     * @return 异常处理的数据下标集合
     */
    public static String errorIndexSet(Keyable keyable, int taskNo) {
        return BASE_PREFIX + ERROR_INDEX_SET + keyable.genKey() + SEPARATOR + taskNo;
    }

    /**
     * 任务信息map
     * @param keyable 可转换为key的参数
     * @return 任务信息map
     */
    public static String taskInfoMap(Keyable keyable) {
        return BASE_PREFIX + TASK_INFO_MAP + keyable.genKey();
    }

    /**
     * 机器信息map
     * @return 机器信息map
     */
    public static String machineInfoMap() {
        return BASE_PREFIX + MACHINE_INFO_MAP;
    }

    /**
     * 根据传入的参数生成锁定key
     * @param objects 传入的参数
     * @return 锁定key
     */
    public static String genLockKey(Object... objects) {
        StringBuilder lockKey = new StringBuilder(BASE_PREFIX + LOCK_PREFIX);
        int length = objects.length;
        for (int i = 0; i < length; i++) {
            Object object = objects[i];
            if (object != null) {
                lockKey.append(object);
                if (i < length - 1) {
                    lockKey.append(SEPARATOR);
                }
            }
        }
        return lockKey.toString();
    }

}
