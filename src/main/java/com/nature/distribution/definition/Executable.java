package com.nature.distribution.definition;

/**
 * 执行逻辑
 * @author nature
 * @version 1.0.0
 * @since 2018/11/30 21:56
 */
@FunctionalInterface
public interface Executable<P> {

    /**
     * 根据入参处理
     * @param param 任务编号
     */
    void execute(P param);
}
