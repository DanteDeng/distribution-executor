package com.nature.distribution.model;

import java.util.Date;

/**
 * 机器信息
 * @author nature
 * @version 1.0.0
 * @since 2018/11/22 10:17
 */
public class MachineInfo extends BaseModel {

    public MachineInfo(String machineNo, Date lastHeartbeatTime) {
        this.machineNo = machineNo;
        this.lastHeartbeatTime = lastHeartbeatTime;
    }

    /**
     * 机器唯一标识
     */
    private String machineNo;
    /**
     * 上次心跳时间
     */
    private Date lastHeartbeatTime;

    public String getMachineNo() {
        return machineNo;
    }

    public void setMachineNo(String machineNo) {
        this.machineNo = machineNo;
    }

    public Date getLastHeartbeatTime() {
        return lastHeartbeatTime;
    }

    public void setLastHeartbeatTime(Date lastHeartbeatTime) {
        this.lastHeartbeatTime = lastHeartbeatTime;
    }
}
