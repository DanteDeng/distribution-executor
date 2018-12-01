package com.nature.distribution.model;

import java.util.Date;

/**
 * 任务信息
 * @author nature
 * @version 1.0.0
 * @since 2018/11/22 10:17
 */
public class TaskInfo extends BaseModel {

    /**
     * 状态：处理中
     */
    public static final int STATUS_HANDLING = 1;
    /**
     * 状态：已完成
     */
    public static final int STATUS_FINISH = 2;
    /**
     * 任务编号
     */
    private int taskNo;
    /**
     * 机器唯一标识
     */
    private String machineNo;
    /**
     * 任务处理数据数量
     */
    private int total;
    /**
     * 已完成
     */
    private int finish;
    /**
     * 任务处理状态
     */
    private int status;
    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 结束时间
     */
    private Date finishTime;
    /**
     * 处理异常总数
     */
    private int errorTotal;

    public int getTaskNo() {
        return taskNo;
    }

    public void setTaskNo(int taskNo) {
        this.taskNo = taskNo;
    }

    public String getMachineNo() {
        return machineNo;
    }

    public void setMachineNo(String machineNo) {
        this.machineNo = machineNo;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getFinish() {
        return finish;
    }

    public void setFinish(int finish) {
        this.finish = finish;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public int getErrorTotal() {
        return errorTotal;
    }

    public void setErrorTotal(int errorTotal) {
        this.errorTotal = errorTotal;
    }

}
