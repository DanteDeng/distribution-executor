package com.nature.distribution.executor;

import com.nature.distribution.definition.Executor;
import com.nature.distribution.definition.ExecutorWatcher;
import com.nature.distribution.definition.Work;
import com.nature.distribution.model.KeyAndPage;
import com.nature.distribution.model.TaskInfo;
import com.nature.distribution.util.*;
import com.nature.distribution.watcher.CommonExecutorWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 分布式任务执行器模板
 * @author nature
 * @version 1.0.0
 * @since 2018/11/22 10:18
 */
public class DistributionExecutor implements Executor {

    private static final Logger LOG = LoggerFactory.getLogger(DistributionExecutor.class);

    private Set<String> works = new HashSet<>();

    /**
     * 任务查询间隔，这个时间应该比每一批任务执行时间较短
     */
    private static final int SLEEP_SECONDS = 1;

    /**
     * 线程池（后续改成使用容器管理的）
     */
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(40, 80, 0,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    /**
     * 锁
     */
    private Lock lock = new ReentrantLock();

    /**
     * 执行任务
     * @param work  任务
     * @param param 参数
     */
    @Override
    public <P extends KeyAndPage, D> void execute(Work<P, D> work, P param) {
        String key = param.genKey();
        makeSureSingletonRunning(key);                                  //  确保只有一个实例执行（防止重复提交等问题）
        try {
            CacheUtil.delete(TaskKeyUtil.stopFlag(param));              // 开始先删除停止标记

            String lockKey = TaskKeyUtil.genLockKey(key);               // 操作锁key
            DistributionExecuteUtil.asynchronouslyExecute(lockKey, SLEEP_SECONDS,
                    () -> isCompleted(work, param),                            // 查询任务是否已经处理完成
                    () -> calculateTaskNo(work, param),                        // 计算任务编号
                    (taskNo) -> {                                       // 按任务编号分批执行
                        param.setPageNum(taskNo);
                        doBatchTask(work, param);
                    }
            );
        } finally {
            works.remove(key);                                          // 执行器标记为非执行中
        }
    }

    /**
     * 执行直到全部处理完成
     * @param work 参数
     */
    @Override
    public <P extends KeyAndPage, D> void executeUntilAllDone(Work<P, D> work, P param) {
        // 1.执行
        execute(work, param);
        // 2.执行完成后等待直到所有机器处理完成
        waitUntilAllDone(param);
    }

    /**
     * 停止任务处理
     * @param param 参数
     */
    @Override
    public <P extends KeyAndPage> void stop(P param) {
        // 缓存中增加任务停止标记
        CacheUtil.set(TaskKeyUtil.stopFlag(param), true);
    }

    /**
     * 等待直到全部执行完成
     * @param param 参数
     */
    private <P extends KeyAndPage> void waitUntilAllDone(P param) {
        String lockKey = TaskKeyUtil.genLockKey(param.genKey());
        while (true) {
            AtomicBoolean done = new AtomicBoolean(true);           // 标记是否处理完成
            DistributionExecuteUtil.synchronouslyExecute(lockKey, () -> {
                // 查询任务停止标记
                Boolean stopFlag = CacheUtil.get(TaskKeyUtil.stopFlag(param), boolean.class);
                if (stopFlag != null && stopFlag) {
                    done.set(true);
                    return;
                }
                List<Object> objects = CacheUtil.getMapValues(TaskKeyUtil.taskInfoMap(param));  // 获取全部任务数据
                for (Object object : objects) {
                    int status = ((TaskInfo) object).getStatus();
                    if (status != TaskInfo.STATUS_FINISH) { // 未完成任务查询进度
                        done.set(false);
                        break;
                    }
                }
            });
            if (done.get()) {
                break;
            } else {
                SleepUtil.sleepSeconds(SLEEP_SECONDS);
            }
        }
    }

    /**
     * 批量执行
     * @param work  任务
     * @param param 参数
     * @param <P>   参数类型
     * @param <D>   数据
     */
    private <P extends KeyAndPage, D> void doBatchTask(Work<P, D> work, P param) {
        initTaskInfoToCache(param);      // 1.初始化任务信息至缓存
        int pageSize = param.getPageSize();
        LOG.info(String.format("批次执行参数 %s", param));

        String normalIndexKey = TaskKeyUtil.normalIndexSet(param, param.getPageNum());
        String errorIndexKey = TaskKeyUtil.errorIndexSet(param, param.getPageNum());
        List<D> data = work.selectDataList(param);  // 查询获取本批次需要处理的全部数据
        boolean isRestart = false;
        if (!CacheUtil.hasKey(normalIndexKey)) {   // 判断是否任务重启
            isRestart = true;
        }
        int dataTotal = data.size();

        updateTaskInfoToCache(param, dataTotal); // 2.更新缓存中的任务信息
        List<Future<?>> futures = new ArrayList<>(pageSize);    // 阻塞线程使用
        int counter = 0;
        for (D datum : data) {
            final int index = (++counter);
            if (isRestart) {
                boolean hasInSet = CacheUtil.hasInSet(normalIndexKey, index);
                if (hasInSet) {   // 已处理过的任务不在处理
                    continue;
                }
            }
            Future<?> future = executor.submit(() -> {
                try {
                    work.handleDatum(param, datum);  // 处理逻辑需要支持重试，如果不支持可能因为重试导致数据不准确
                } catch (Throwable t) {
                    CacheUtil.addToSet(errorIndexKey, index);      // 记录异常数据
                    throw t;                    // 异常继续传递，以便于后续日志打印
                }
                CacheUtil.addToSet(normalIndexKey, index);    // 处理完数据进行记录
            });
            futures.add(future);
        }

        blockMainThread(futures);    // 3.阻塞主线程
        finishThisBatch(param);  // 4.任务完成状态更新入缓存
    }

    /**
     * 初始化任务信息至缓存
     * @param param 执行参数
     */
    private <P extends KeyAndPage> void initTaskInfoToCache(P param) {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setStartTime(new Date());
        taskInfo.setTaskNo(param.getPageNum());
        taskInfo.setMachineNo(ApplicationUtil.getApplicationUniqueKey());
        String key = TaskKeyUtil.taskInfoMap(param);
        CacheUtil.setHash(key, String.valueOf(param.getPageNum()), taskInfo);
    }

    /**
     * 批次开始处理更新任务状态为处理中
     * @param param 执行参数
     * @param total 批次处理数据总数
     */
    private <P extends KeyAndPage> void updateTaskInfoToCache(P param, int total) {
        String taskInfoMapKey = TaskKeyUtil.taskInfoMap(param);
        String taskNo = String.valueOf(param.getPageNum());
        TaskInfo taskInfo = CacheUtil.getHash(taskInfoMapKey, taskNo, TaskInfo.class);
        taskInfo.setStatus(TaskInfo.STATUS_HANDLING);
        taskInfo.setTotal(total);
        taskInfo.setMachineNo(ApplicationUtil.getApplicationUniqueKey());
        CacheUtil.setHash(taskInfoMapKey, String.valueOf(param.getPageNum()), taskInfo);
    }

    /**
     * 任务完成状态更新入缓存
     * @param param 参数
     * @param <P>   参数类型
     */
    private <P extends KeyAndPage> void finishThisBatch(P param) {
        String normalIndexKey = TaskKeyUtil.normalIndexSet(param, param.getPageNum());
        String errorIndexKey = TaskKeyUtil.errorIndexSet(param, param.getPageNum());
        String taskInfoMapKey = TaskKeyUtil.taskInfoMap(param);
        String taskNo = String.valueOf(param.getPageNum());
        TaskInfo taskInfo = CacheUtil.getHash(taskInfoMapKey, taskNo, TaskInfo.class);
        taskInfo.setFinishTime(new Date());
        taskInfo.setStatus(TaskInfo.STATUS_FINISH);
        taskInfo.setFinish(CacheUtil.getSetSize(normalIndexKey));       // 已完成设置为正常处理的数据总量
        taskInfo.setErrorTotal(CacheUtil.getSetSize(errorIndexKey));    // 异常总数设置为处理异常的数据总量
        taskInfo.setMachineNo(ApplicationUtil.getApplicationUniqueKey());
        CacheUtil.setHash(taskInfoMapKey, taskNo, taskInfo);
        // 清除已完成任务数据
        CacheUtil.delete(normalIndexKey);
        CacheUtil.delete(errorIndexKey);
    }

    /**
     * 阻塞主线程
     * @param futures 子线程futures
     */
    private void blockMainThread(List<Future<?>> futures) {
        for (Future<?> future : futures) {  // 阻塞线程
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("获取操作结果错误", e);
            }
        }
    }

    /**
     * 计算任务编号
     * @param work  任务
     * @param param 参数
     * @param <P>   参数类型
     * @param <D>   数据类型
     * @return 任务编号
     */
    private <P extends KeyAndPage, D> Integer calculateTaskNo(Work<P, D> work, P param) {
        // 查询目前已执行任务数
        String taskNoKey = TaskKeyUtil.taskNo(param);
        Integer taskNo = CacheUtil.get(taskNoKey, Integer.class);
        if (taskNo == null) {       // 未查到已执行任务编号说明首次执行
            int taskTotal = this.selectTaskTotal(work, param);
            if (taskTotal > 0) {    // 首次执行查询任务总数是否大于0，大于0说明有任务需要执行
                taskNo = 1;
                CacheUtil.set(taskNoKey, taskNo);
            }
        } else { // 非首次执行
            int taskTotal = this.selectTaskTotal(work, param);
            if (taskTotal > taskNo) {   // 如果任务编号小于总任务数量则任务编号+1并写入缓存
                taskNo++;
                CacheUtil.set(taskNoKey, taskNo);
            } else { // 否则查询是否有机器宕机
                ExecutorWatcher executorWatcher = CommonExecutorWatcher.getInstance();
                List<TaskInfo> list = executorWatcher.selectTaskList(param);
                for (TaskInfo taskInfo : list) {
                    if (taskInfo.getStatus() != TaskInfo.STATUS_FINISH) {   // 未完成任务查询机器是否宕机
                        boolean active = executorWatcher.isActive(taskInfo.getMachineNo());
                        if (!active) {
                            taskNo = taskInfo.getTaskNo();
                            break;
                        }
                    }
                }
            }
        }
        LOG.info(String.format("任务编号 %s", taskNo));
        return taskNo;
    }

    /**
     * 任务是否已经执行完成
     * @param work  任务
     * @param param 参数
     * @param <P>   参数类型
     * @param <D>   数据类型
     * @return true：已完成
     */
    private <P extends KeyAndPage, D> Boolean isCompleted(Work<P, D> work, P param) {
        // 查询任务停止标记
        Boolean stopFlag = CacheUtil.get(TaskKeyUtil.stopFlag(param), boolean.class);
        if (stopFlag != null && stopFlag) {
            LOG.info(String.format("任务暂停 %s", param));
            return true;
        }
        int taskTotal = this.selectTaskTotal(work, param);    // 查询任务总数
        boolean result = true;  // 标记是否已处理完成true表示已完成
        if (taskTotal != 0) {
            Integer taskNo = CacheUtil.get(TaskKeyUtil.taskNo(param), Integer.class);
            if (taskNo == null || taskNo < taskTotal) {
                result = false;
            }
        }
        if (result) { // 如果任务编号已经执行完，判断任务是否都已经处理完成
            result = CommonExecutorWatcher.getInstance().isAllTaskDone(param);
        }
        LOG.info(String.format("执行完成？ %s", result));
        return result;
    }

    /**
     * 查询获取任务总数
     * @param work  任务操作
     * @param param 参数
     * @param <P>   参数类型
     * @param <D>   数据类型
     * @return 数据总量
     */
    private <P extends KeyAndPage, D> int selectTaskTotal(Work<P, D> work, P param) {
        String taskTotalKey = TaskKeyUtil.taskTotal(param);
        String dataTotalKey = TaskKeyUtil.dataTotal(param);
        String lockKey = TaskKeyUtil.genLockKey(param.genKey());
        // 只需要一台机器执行实际逻辑
        Integer total = DistributionExecuteUtil.synchronouslyExecute(lockKey, SLEEP_SECONDS, () -> {
            // 1.从缓存查询
            return CacheUtil.get(taskTotalKey, Integer.class);
        }, () -> {
            int dataTotal = work.selectDataTotal(param);
            CacheUtil.set(dataTotalKey, dataTotal);     // 数据总数放入缓存
            int taskTotal = dataTotal / param.getPageSize() + (dataTotal % param.getPageSize() == 0 ? 0 : 1);
            CacheUtil.set(taskTotalKey, taskTotal);     // 任务总数放入缓存
        });

        LOG.info(String.format("任务总数 %s，param %s", total, param));
        return total;
    }

    /**
     * 确保只有一个实例执行
     * @param key 锁定的key
     */
    private void makeSureSingletonRunning(String key) {
        lock.lock();
        try {
            if (!works.contains(key)) {
                works.add(key);
            } else {
                throw new RuntimeException("同一应用中同一任务只允许一个实例执行");
            }
        } finally {
            lock.unlock();
        }
    }

}