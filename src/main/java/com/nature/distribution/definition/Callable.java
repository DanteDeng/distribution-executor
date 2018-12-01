package com.nature.distribution.definition;

/**
 * 执行逻辑
 * @author nature
 * @version 1.0.0
 * @since 2018/11/30 22:29
 */
@FunctionalInterface
public interface Callable<R> {
    /**
     * 执行
     * @return 执行结果
     */
    R call();
}
