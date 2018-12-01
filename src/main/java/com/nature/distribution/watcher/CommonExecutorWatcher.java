package com.nature.distribution.watcher;

import com.nature.distribution.definition.ExecutorWatcher;
import com.nature.distribution.model.Keyable;
import com.nature.distribution.model.MachineInfo;
import com.nature.distribution.model.TaskInfo;
import com.nature.distribution.util.ApplicationUtil;
import com.nature.distribution.util.CacheUtil;
import com.nature.distribution.util.TaskKeyUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 通用执行器监控器
 * @author nature
 * @version 1.0.0
 * @since 2018/11/22 10:04
 */
public class CommonExecutorWatcher implements ExecutorWatcher {

    /**
     * 实例
     */
    private static ExecutorWatcher executorWatcher;
    /**
     * 频率
     */
    private int rate = 3;
    /**
     * 定时器
     */
    private ScheduledThreadPoolExecutor scheduler;
    /**
     * 锁
     */
    private Lock lock = new ReentrantLock();

    /**
     * 心跳状态标记
     */
    private boolean heartBeating;

    private CommonExecutorWatcher() {
    }

    /**
     * 获取单例
     * @return 实例
     */
    public static ExecutorWatcher getInstance() {
        if (executorWatcher == null) {    // 实例化多次无影响不做多余控制
            executorWatcher = new CommonExecutorWatcher();
        }
        return executorWatcher;
    }

    /**
     * 查询数据总数
     * @param param 参数
     * @return 数据总数
     */
    @Override
    public int selectDataTotal(Keyable param) {
        String dataTotalKey = TaskKeyUtil.dataTotal(param);
        Integer dataTotal = CacheUtil.get(dataTotalKey, Integer.class);
        return dataTotal == null ? 0 : dataTotal;
    }

    /**
     * 查询任务总数
     * @param param 参数
     * @return 任务总数
     */
    @Override
    public int selectTaskTotal(Keyable param) {
        String totalKey = TaskKeyUtil.taskTotal(param);
        Integer taskTotal = CacheUtil.get(totalKey, Integer.class);
        return taskTotal == null ? 0 : taskTotal;
    }

    /**
     * 查询任务信息列表
     * @param param 参数
     * @return 任务信息列表
     */
    @Override
    public List<TaskInfo> selectTaskList(Keyable param) {
        List<Object> objects = CacheUtil.getMapValues(TaskKeyUtil.taskInfoMap(param));
        List<TaskInfo> tasks = new ArrayList<>();
        for (Object object : objects) {
            TaskInfo taskInfo = (TaskInfo) object;
            int status = taskInfo.getStatus();
            if (status != TaskInfo.STATUS_FINISH) { // 未完成任务查询进度
                String normalIndexKey = TaskKeyUtil.normalIndexSet(param, taskInfo.getTaskNo());
                String errorIndexKey = TaskKeyUtil.errorIndexSet(param, taskInfo.getTaskNo());
                int finish = CacheUtil.getSetSize(normalIndexKey);
                int errorTotal = CacheUtil.getSetSize(errorIndexKey);
                taskInfo.setFinish(finish);
                taskInfo.setErrorTotal(errorTotal);
            }
            tasks.add(taskInfo);
        }
        tasks.sort(Comparator.comparingInt(TaskInfo::getTaskNo));
        return tasks;
    }

    /**
     * 查询任务是否全部已完成
     * @param param 参数
     * @return 任务是否全部已完成
     */
    @Override
    public boolean isAllTaskDone(Keyable param) {
        boolean done = true;
        List<Object> objects = CacheUtil.getMapValues(TaskKeyUtil.taskInfoMap(param));
        if (objects.isEmpty()) {  // 说明完全还未开始处理
            done = false;
        } else {
            int taskTotal = selectTaskTotal(param);
            if (taskTotal > objects.size()) {
                done = false;
            } else {
                for (Object object : objects) {
                    int status = ((TaskInfo) object).getStatus();
                    if (status != TaskInfo.STATUS_FINISH) { // 未完成任务查询进度
                        done = false;
                        break;
                    }
                }
            }
        }
        return done;
    }

    /**
     * 设置心跳频率
     * @param rate 频率
     */
    @Override
    public void setHeartbeatRate(int rate) {
        if (rate <= 0) {
            throw new RuntimeException("频率必须大于0");
        } else {
            this.rate = rate;
        }
        lock.lock();
        try {
            if (heartBeating) { // 根据是否心跳中判断是否是重置操作
                this.stopHeartbeat();
                this.startHeartbeat();
            } else {
                this.startHeartbeat();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取全部活跃的机器信息
     * @return 活跃的机器信息
     */
    @Override
    public List<MachineInfo> getActiveMachines() {
        String mapKey = TaskKeyUtil.machineInfoMap();
        List<Object> mapValues = CacheUtil.getMapValues(mapKey);
        List<MachineInfo> machines = new ArrayList<>();
        for (Object v : mapValues) {
            MachineInfo machineInfo = (MachineInfo) v;
            Date heartbeatTime = machineInfo.getLastHeartbeatTime();
            if ((new Date().getTime() - heartbeatTime.getTime()) / (rate * 1000) > 2) {
                // 上次心跳距离当前时间已超间隔两倍，说明机器已经宕机,宕机机器移除
                CacheUtil.deleteHash(mapKey, machineInfo.getMachineNo());
                continue;
            }
            machines.add(machineInfo);
        }

        return machines;
    }

    /**
     * 判断指定机器编号的机器是否活动中
     * @param machineNo 机器编号
     * @return 是否活动中
     */
    @Override
    public boolean isActive(String machineNo) {
        List<MachineInfo> machines = getActiveMachines();
        for (MachineInfo machine : machines) {
            String no = machine.getMachineNo();
            if (machineNo.equals(no)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 开始心跳
     */
    private void startHeartbeat() {
        heartBeating = true;
        String mapKey = TaskKeyUtil.machineInfoMap();
        String machineNo = ApplicationUtil.getApplicationUniqueKey();
        scheduler = new ScheduledThreadPoolExecutor(1);
        scheduler.scheduleAtFixedRate(() -> {
            MachineInfo machineInfo = new MachineInfo(machineNo, new Date());
            CacheUtil.setHash(mapKey, machineNo, machineInfo);  // 机器编号信息存入缓存
        }, rate, rate, TimeUnit.SECONDS);
    }

    /**
     * 结束心跳
     */
    private void stopHeartbeat() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        heartBeating = false;
    }
}
