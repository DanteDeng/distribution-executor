package com.nature.distribution.definition;

import com.nature.distribution.model.Keyable;
import com.nature.distribution.model.MachineInfo;
import com.nature.distribution.model.TaskInfo;

import java.util.List;

/**
 * 执行器监控器
 * @author nature
 * @version 1.0.0
 * @since 2018/11/22 10:20
 */
public interface ExecutorWatcher {

    /**
     * 查询数据总数
     * @param param 参数
     * @return 数据总数
     */
    int selectDataTotal(Keyable param);

    /**
     * 查询任务总数
     * @param param 参数
     * @return 任务总数
     */
    int selectTaskTotal(Keyable param);

    /**
     * 查询任务列表
     * @param param 参数
     * @return 任务列表
     */
    List<TaskInfo> selectTaskList(Keyable param);

    /**
     * 查询是否全部任务已完成
     * @param param 参数
     * @return 否全部任务已完成
     */
    boolean isAllTaskDone(Keyable param);

    /**
     * 心跳间隔设置
     * @param rate 频率
     */
    void setHeartbeatRate(int rate);

    /**
     * 获取全部活跃的机器信息
     * @return 活跃的机器信息
     */
    List<MachineInfo> getActiveMachines();

    /**
     * 判断指定机器编号的机器是否活动中
     * @param machineNo 机器编号
     * @return 是否活动中
     */
    boolean isActive(String machineNo);

}
