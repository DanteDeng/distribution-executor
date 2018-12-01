package com.nature.distribution.definition;

import com.nature.distribution.model.KeyAndPage;

/**
 * 分布式执行器
 * @author nature
 * @version 1.0.0
 * @since 2018/11/22 10:20
 */
public interface Executor {

    /**
     * 执行
     * @param work 参数
     */
    <P extends KeyAndPage, D> void execute(Work<P, D> work, P param);

    /**
     * 执行直到全部处理完成
     * @param work 参数
     */
    <P extends KeyAndPage, D> void executeUntilAllDone(Work<P, D> work, P param);

    /**
     * 停止任务
     * @param param 参数
     */
    <P extends KeyAndPage> void stop(P param);
}
