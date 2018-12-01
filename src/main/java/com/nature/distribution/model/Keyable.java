package com.nature.distribution.model;

/**
 * 可生成key的
 * @author nature
 * @version 1.0.0
 * @since 2018/11/22 10:16
 */
public interface Keyable {

    /**
     * 生成key
     * @return key
     */
    String genKey();
}
