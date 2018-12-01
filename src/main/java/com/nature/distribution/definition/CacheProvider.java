package com.nature.distribution.definition;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 缓存提供者接口
 * @author nature
 * @version 1.0.0
 * @since 2018/11/22 10:21
 */
public interface CacheProvider {

    /**
     * 放入缓存
     * @param key   键
     * @param value 值
     */
    void set(String key, Object value);

    /**
     * 获取放在cache中的内容
     * @param key 键
     * @return 值
     */
    Object get(String key);

    /**
     * 按匹配规则查询全部key
     * @param pattern 匹配规则
     * @return keys
     */
    Set<String> keys(String pattern);

    /**
     * 批量清除key
     * @param keys key集合
     */
    void delete(Set<String> keys);

    /**
     * 锁定指定时间
     * @param lockKey     锁定key
     * @param lockSeconds 锁定时间（秒）
     * @return 锁定结果
     */
    boolean lock(String lockKey, Long lockSeconds);

    /**
     * 设置过期时间
     * @param key        键
     * @param expireTime 过期时间
     */
    void expire(String key, Long expireTime);

    /**
     * 判断是否存在key
     * @param key key
     * @return 结果
     */
    boolean hasKey(String key);

    /**
     * 增加增量
     * @param key   key
     * @param value 增量
     * @return 增量后结果
     */
    Integer incrementAndGet(String key, long value);

    /**
     * 指定map中指定hashKey的值设置
     * @param key     map对应的key
     * @param hashKey map中指定的key
     * @param value   值
     */
    void setHash(String key, String hashKey, Object value);

    /**
     * 获取map中指定hashKey的值
     * @param key     map对应的key
     * @param hashKey map中指定的key
     * @return 值
     */
    Object getHash(String key, String hashKey);

    /**
     * 清除cache中对应的值
     * @param key 键
     */
    void remove(String key);

    /**
     * 获取哈希值
     * @param key     map对应key
     * @param hashKey map中的哈希key
     * @param vClass  值类型
     * @return 值
     */
    <V> V getHash(String key, String hashKey, Class<V> vClass);

    /**
     * 删除hash中某个key
     * @param key     key
     * @param hashKey hashKey
     */
    void deleteHash(String key, String hashKey);

    /**
     * 获取整个map
     * @param key 键
     * @return map
     */
    Map<String, Object> getMap(String key);

    /**
     * 获取整个map的key集合
     * @param key 键
     * @return set
     */
    Set<String> getMapKeys(String key);

    /**
     * 添加到set
     * @param key   键
     * @param value 值
     */
    void addToSet(String key, Object value);

    /**
     * set中是否存在
     * @param key   键
     * @param value 值
     * @return true：存在
     */
    boolean hasInSet(String key, Object value);

    /**
     * 获取set size
     * @param key 键
     * @return size
     */
    int getSetSize(String key);

    /**
     * 从缓存中取出set
     * @param key    键
     * @param vClass 值类
     * @param <V>    值类
     * @return set
     */
    <V> Set<V> getSet(String key, Class<V> vClass);

    /**
     * 获取指定map的全部值集合
     * @param key key
     * @return 指定map的全部值集合
     */
    List<Object> getMapValues(String key);

    /**
     * 添加到list
     * @param key   key
     * @param datum 数据
     */
    void addToList(String key, Object datum);

    /**
     * 全部添加到list
     * @param key     key
     * @param objects 集合
     */
    void addAllToList(String key, Collection<Object> objects);

    /**
     * 从list中弹出
     * @param key    key
     * @param vClass 数据类型
     * @param <V>    类型
     * @return 数据
     */
    <V> V popFromList(String key, Class<V> vClass);

    /**
     * 获取list的size
     * @param key key
     * @return size
     */
    Integer getListSize(String key);

    /**
     * 获取list指定位置的子集
     * @param key   key
     * @param start 开始位置
     * @param end   结束位置
     * @return 子集
     */
    List<Object> sublist(String key, int start, int end);

    /**
     * 获取list
     * @param key key
     * @return list
     */
    List<Object> getList(String key);

}
