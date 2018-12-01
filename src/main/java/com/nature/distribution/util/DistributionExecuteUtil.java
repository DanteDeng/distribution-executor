package com.nature.distribution.util;

import com.nature.distribution.definition.Callable;
import com.nature.distribution.definition.Executable;
import com.nature.distribution.definition.Invokable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 分布式执行器
 * @author nature
 * @version 1.0.0
 * @since 2018/11/22 10:05
 */
public class DistributionExecuteUtil {

    private static final Logger LOG = LoggerFactory.getLogger(DistributionExecuteUtil.class);

    /**
     * 分布式同步执行
     * @param lockKey 锁定key
     * @param task    执行逻辑
     * @param <R>     结果类型
     * @return 执行返回结果
     */
    public static <R> R synchronouslyExecute(String lockKey, Callable<R> task) {
        return synchronouslyExecute(lockKey, null, null, task);
    }

    /**
     * 分布式同步执行
     * @param lockKey 锁定key
     * @param task    执行逻辑
     */
    public static void synchronouslyExecute(String lockKey, Invokable task) {
        synchronouslyExecute(lockKey, null, null, task);
    }

    /**
     * 分布式同步执行
     * @param lockKey  锁定key
     * @param lockTime 锁定时间（秒）
     * @param retry    重试次数
     * @param task     执行逻辑
     */
    public static void synchronouslyExecute(String lockKey, Long lockTime, Integer retry, Invokable task) {
        LOG.debug(String.format("synchronously execute start lock key %s time %s retry %s", lockKey, lockTime, retry));
        if (lockKey == null || lockKey.isEmpty() ||
                (lockTime != null && lockTime <= 0) || (retry != null && retry < 0)) {
            throw new RuntimeException(String.format("synchronously execute param illegal lock key %s time %s", lockKey, lockTime));
        }
        boolean lock;
        int tryCount = 0;
        if (lockTime == null) {
            lockTime = 60L;
        }
        do {
            lock = CacheUtil.lock(lockKey, lockTime);
            tryCount++;
            SleepUtil.sleepMillis(300L);
        } while (!lock && (retry == null || tryCount <= retry));

        if (!lock) {
            return;
        }
        long period = lockTime / 2;
        Timer timer = null;
        if (period > 0) {
            timer = refreshLockTime(lockKey, lockTime, period);
        }
        try {
            task.invoke();
            LOG.debug(String.format("synchronously execute end lock key %s time %s retry %s", lockKey, lockTime, retry));

        } finally {
            if (timer != null) {
                timer.cancel();
            }
            CacheUtil.unlock(lockKey);
        }
    }

    /**
     * 分布式同步执行
     * @param lockKey  锁定key
     * @param lockTime 锁定时间（秒）
     * @param retry    重试次数
     * @param task     执行逻辑
     * @param <R>      结果类型
     * @return 执行返回结果
     */
    public static <R> R synchronouslyExecute(String lockKey, Long lockTime, Integer retry, Callable<R> task) {
        LOG.debug(String.format("synchronously execute start lock key %s time %s retry %s", lockKey, lockTime, retry));
        if (lockKey == null || lockKey.isEmpty() ||
                (lockTime != null && lockTime <= 0) || (retry != null && retry < 0)) {
            throw new RuntimeException(String.format("synchronously execute param illegal lock key %s time %s", lockKey, lockTime));
        }
        boolean lock;
        int tryCount = 0;
        if (lockTime == null) {
            lockTime = 60L;
        }
        do {
            lock = CacheUtil.lock(lockKey, lockTime);
            tryCount++;
            SleepUtil.sleepMillis(300L);
        } while (!lock && (retry == null || tryCount <= retry));

        if (!lock) {
            return null;
        }
        long period = lockTime / 2;
        Timer timer = null;
        if (period > 0) {
            timer = refreshLockTime(lockKey, lockTime, period);
        }
        try {
            R r = task.call();
            LOG.debug(String.format("synchronously execute end lock key %s time %s retry %s result %s", lockKey, lockTime, retry, r));
            return r;
        } finally {
            if (timer != null) {
                timer.cancel();
            }
            CacheUtil.unlock(lockKey);
        }
    }

    /**
     * 分布式同步执行直到获取需要的结果（分为查询与执行两个部分，查询的结果是通过执行进行设置，查询的逻辑任意机器均可执行，
     * 但是执行的逻辑则需要保证只有一台机器执行，通过这样的方式达到节约机器性能，信息多机共享的目的）
     * @param lockKey       锁定key
     * @param periodSeconds 间隔秒数
     * @param query         查询逻辑
     * @param execute       执行修改的逻辑
     * @param <R>           返回类型
     * @return 结果
     */
    public static <R> R synchronouslyExecute(String lockKey, int periodSeconds, Callable<R> query, Invokable execute) {
        LOG.debug(String.format("synchronously execute start lock key %s period %s seconds", lockKey, periodSeconds));
        R result;
        do {
            result = query.call(); // 执行查询结果查到结果则返回
            if (result == null) {
                boolean lock = CacheUtil.lock(lockKey, null);
                if (lock) {
                    Timer timer = refreshLockTime(lockKey, 60L, 50L);
                    try {
                        result = query.call();  // 执行查询结果查到结果则返回
                        if (result != null) {
                            break;
                        } else {
                            execute.invoke();  // 查询无结果执行处理逻辑以设置结果
                        }
                    } finally {
                        timer.cancel();
                        CacheUtil.unlock(lockKey);
                    }
                } else {    // 竞争锁失败则休眠指定时间后再行获取
                    SleepUtil.sleepSeconds(periodSeconds);
                }
            }
        } while (result == null);
        LOG.debug(String.format("synchronously execute end lock key %s period %s seconds result %s", lockKey, periodSeconds, result));
        return result;
    }

    /**
     * 分布异步执行（包括三个部分逻辑）
     * 1.查询处理结果（true表示已经处理完成）
     * 2.计算任务编号（任务编号不为null表示有需要执行的编号）
     * 3.执行任务（按任务编号执行处理任务）
     * @param lockKey      锁定key
     * @param sleepSeconds 间隔秒数
     * @param query        查询逻辑
     * @param calculate    计算任务编号
     * @param execute      执行修改的逻辑
     */
    public static void asynchronouslyExecute(String lockKey, int sleepSeconds, Callable<Boolean> query,
                                             Callable<Integer> calculate, Executable<Integer> execute) {
        LOG.debug(String.format("asynchronously execute start lock key %s sleep seconds %s", lockKey, sleepSeconds));
        Boolean result;
        do {
            result = query.call(); // 执行查询结果查到结果则返回
            if (result == null || !result) {
                Integer taskNo = null;
                boolean lock = CacheUtil.lock(lockKey, 60L);
                if (lock) {
                    Timer timer = refreshLockTime(lockKey, 60L, 50L);
                    try {
                        taskNo = calculate.call();
                    } finally {
                        timer.cancel();
                        CacheUtil.unlock(lockKey);
                    }
                } else {    // 竞争锁失败则休眠指定时间后再行获取
                    SleepUtil.sleepSeconds(sleepSeconds);
                }
                if (taskNo != null) {
                    execute.execute(taskNo);
                } else {
                    break;
                }
            }
        } while (result != null && !result);
        LOG.debug(String.format("asynchronously execute end lock key %s sleep seconds %s", lockKey, sleepSeconds));
    }


    /**
     * 刷新锁的过期时间
     * @param lockKey  锁定key
     * @param lockTime 锁定时间
     * @param period   刷新周期
     * @return 定时器
     */
    private static Timer refreshLockTime(String lockKey, long lockTime, long period) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                CacheUtil.expire(lockKey, lockTime);
            }
        }, period, period);
        return timer;
    }

}
